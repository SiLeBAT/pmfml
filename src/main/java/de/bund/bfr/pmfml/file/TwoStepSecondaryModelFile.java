/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Contributors: Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.pmfml.file;

import de.bund.bfr.pmfml.ModelType;
import de.bund.bfr.pmfml.file.uri.UriFactory;
import de.bund.bfr.pmfml.model.PrimaryModelWData;
import de.bund.bfr.pmfml.model.TwoStepSecondaryModel;
import de.bund.bfr.pmfml.numl.NuMLDocument;
import de.bund.bfr.pmfml.sbml.DataSourceNode;
import de.bund.bfr.pmfml.sbml.PrimaryModelNode;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import de.unirostock.sems.cbarchive.meta.MetaDataObject;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.xml.XMLNode;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Case 2a: Two step secondary model file. Secondary models generated with the classical 2-step
 * approach from primary models.
 *
 * @author Miguel Alba
 */
public class TwoStepSecondaryModelFile {

    private static final Logger LOGGER = Logger.getLogger("TwoStepSecondaryModelFile");

    private static final URI SBML_URI = UriFactory.createSBMLURI();
    private static final URI PMF_URI = UriFactory.createPMFURI();
    private static final URI NUML_URI = UriFactory.createNuMLURI();

    public static List<TwoStepSecondaryModel> readPMF(final File file) throws CombineArchiveException {
        return read(file, SBML_URI);
    }

    public static List<TwoStepSecondaryModel> readPMFX(final File file) throws CombineArchiveException {
        return read(file, PMF_URI);
    }

    /**
     */
    public static void writePMF(final String dir, final String filename, final List<TwoStepSecondaryModel> models)
            throws CombineArchiveException {
        String caName = dir + "/" + filename + ".pmf";
        write(new File(caName), SBML_URI, models);
    }

    public static void writePMFX(final String dir, final String filename, final List<TwoStepSecondaryModel> models)
            throws CombineArchiveException {
        String caName = dir + "/" + filename + ".pmfx";
        write(new File(caName), PMF_URI, models);
    }

    /**
     * Reads {@link TwoStepSecondaryModel}(s) from a PMF or PMFX file. Faulty models are skipped.
     *
     * @param file
     * @param modelUri
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    private static List<TwoStepSecondaryModel> read(File file, URI modelUri) throws CombineArchiveException {

        Map<String, NuMLDocument> dataEntryMap = new HashMap<>();
        Map<String, SBMLDocument> secModelMap = new HashMap<>();
        Map<String, SBMLDocument> primModelMap = new HashMap<>();

        try (CombineArchive ca = new CombineArchive(file)) {
            // Gets data entries
            for (ArchiveEntry entry : ca.getEntriesWithFormat(NUML_URI)) {
                String dataDocName = entry.getFileName();
                try {
                    NuMLDocument dataDoc = CombineArchiveUtil.readData(entry.getPath());
                    dataEntryMap.put(dataDocName, dataDoc);
                } catch (ParserConfigurationException | SAXException e) {
                    LOGGER.warning(entry.getFileName() + ": could not be retrieved. Skipping entry.");
                }
            }

            // Gets master files
            MetaDataObject mdo = ca.getDescriptions().get(0);
            Element metaParent = mdo.getXmlDescription();
            PMFMetadataNode annot = new PMFMetadataNode(metaParent);
            Set<String> masterFiles = annot.masterFiles;

            // Classify models into primary or secondary models
            for (ArchiveEntry entry : ca.getEntriesWithFormat(modelUri)) {
                String docName = entry.getFileName();

                try {
                    SBMLDocument doc = CombineArchiveUtil.readModel(entry.getPath());
                    if (masterFiles.contains(docName)) {
                        secModelMap.put(docName, doc);
                    } else {
                        primModelMap.put(docName, doc);
                    }
                } catch (XMLStreamException | IOException e) {
                    LOGGER.warning(docName + ": could not be read. Skipping entry.");
                }
            }


        } catch (IOException | ParseException | JDOMException e) {
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }

        List<TwoStepSecondaryModel> models = new ArrayList<>();

        for (Map.Entry<String, SBMLDocument> entry : secModelMap.entrySet()) {
            String secModelName = entry.getKey();
            SBMLDocument secModelDoc = entry.getValue();

            Model md = secModelDoc.getModel();

            Annotation m2Annot = md.getAnnotation();
            if (!m2Annot.isSetNonRDFannotation()) {
                continue;
            }

            XMLNode m2MetaData = m2Annot.getNonRDFannotation().getChildElement("metadata", "");
            List<XMLNode> refs = m2MetaData.getChildElements(PrimaryModelNode.TAG, "");
            List<PrimaryModelWData> pms = new ArrayList<>(refs.size());

            for (XMLNode ref : refs) {
                String primModelName = ref.getChild(0).getCharacters();
                SBMLDocument primModelDoc = primModelMap.get(primModelName);

                // Looks for DataSourceNode
                XMLNode m1Annot = primModelDoc.getModel().getAnnotation().getNonRDFannotation();
                XMLNode m1MetaData = m1Annot.getChildElement("metadata", "");
                XMLNode dataSourceRawNode = m1MetaData.getChildElement("dataSource", "");

                if (dataSourceRawNode == null) {
                    continue;
                }

                String dataFileName = new DataSourceNode(dataSourceRawNode).getFile();
                NuMLDocument numlDoc = dataEntryMap.get(dataFileName);

                pms.add(new PrimaryModelWData(primModelName, primModelDoc, dataFileName, numlDoc));
            }

            models.add(new TwoStepSecondaryModel(secModelName, secModelDoc, pms));
        }

        return models;
    }

    /**
     * Writes two step secondary models to a PMF or PMFX file. Faulty models are skipped. Existent
     * files are overwritten.
     *
     * @param file
     * @param modelUri
     * @param models
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */

    private static void write(File file, URI modelUri, List<TwoStepSecondaryModel> models)
            throws CombineArchiveException {

        // Remove if existent file
        if (file.exists()) {
            file.delete();
        }

        // Creates a COMBINE archive
        try (CombineArchive ca = new CombineArchive(file)) {

            Set<String> masterFiles = new HashSet<>();

            for (TwoStepSecondaryModel model : models) {

                // Utility list with the entries added with the current model
                // If an error occurs this it may be used for cleaning the archive
                List<ArchiveEntry> addedEntries = new ArrayList<>();

                // Write sec model
                // Creates temporary file for the model
                try {
                    ArchiveEntry entry = CombineArchiveUtil.writeModel(ca, model.getSecDoc(), model.getSecDocName(),
                            modelUri);

                    addedEntries.add(entry);
                    masterFiles.add(entry.getPath().getFileName().toString());

                    for (PrimaryModelWData primModel : model.getPrimModels()) {
                        ArchiveEntry dataEntry = CombineArchiveUtil.writeData(ca, primModel.getDataDoc(), primModel
                                .getDataDocName());
                        addedEntries.add(dataEntry);

                        // Write model
                        CombineArchiveUtil.writeModel(ca, primModel.getModelDoc(), primModel.getModelDocName(),
                                modelUri);
                    }
                } catch (XMLStreamException | ParserConfigurationException | TransformerException e) {
                    LOGGER.warning(model.getSecDocName() + ": could not be read");
                    for (ArchiveEntry entry : addedEntries) {
                        ca.removeEntry(entry);
                    }
                }
            }

            // Adds description with model type
            Element annot = new PMFMetadataNode(ModelType.TWO_STEP_SECONDARY_MODEL, masterFiles).node;
            ca.addDescription(new DefaultMetaDataObject(annot));

            ca.pack();

        } catch (Exception e) {
            file.delete();  // Removes faulty file
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }
    }
}
