/***************************************************************************************************
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
 **************************************************************************************************/
package de.bund.bfr.pmfml.file;

import de.bund.bfr.pmfml.ModelType;
import de.bund.bfr.pmfml.file.uri.UriFactory;
import de.bund.bfr.pmfml.model.PrimaryModelWData;
import de.bund.bfr.pmfml.model.TwoStepTertiaryModel;
import de.bund.bfr.pmfml.numl.NuMLDocument;
import de.bund.bfr.pmfml.sbml.DataSourceNode;
import de.bund.bfr.pmfml.sbml.PrimaryModelNode;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
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
 * Case 3a: File with tertiary model generated with 2-step fit approach.
 *
 * @author Miguel Alba
 */
public class TwoStepTertiaryModelFile {

    private static final Logger LOGGER = Logger.getLogger("TwoStepTertiaryModelFile");

    private static final URI SBML_URI = UriFactory.createSBMLURI();
    private static final URI PMF_URI = UriFactory.createPMFURI();
    private static final URI NUML_URI = UriFactory.createNuMLURI();

    public static List<TwoStepTertiaryModel> readPMF(final File file) throws CombineArchiveException {
        return read(file, SBML_URI);
    }

    public static List<TwoStepTertiaryModel> readPMFX(final File file) throws CombineArchiveException {
        return read(file, PMF_URI);
    }

    /**
     */
    public static void writePMF(final String dir, final String filename, final List<TwoStepTertiaryModel> models)
            throws CombineArchiveException {
        String caName = dir + "/" + filename + ".pmf";
        write(new File(caName), SBML_URI, models);
    }

    public static void writePMFX(final String dir, final String filename, final List<TwoStepTertiaryModel> models)
            throws CombineArchiveException {
        String caName = dir + "/" + filename + ".pmfx";
        write(new File(caName), PMF_URI, models);
    }

    private static List<TwoStepTertiaryModel> read(File file, URI modelUri)
            throws CombineArchiveException {

        List<TwoStepTertiaryModel> models = new ArrayList<>();

        Map<String, NuMLDocument> dataDocMap = new HashMap<>();
        Map<String, SBMLDocument> tertDocs = new HashMap<>();
        Map<String, SBMLDocument> primDocs = new HashMap<>();
        Map<String, SBMLDocument> secDocs = new HashMap<>();

        try (CombineArchive ca = new CombineArchive(file)) {

            // Gets data documents
            for (ArchiveEntry entry : ca.getEntriesWithFormat(NUML_URI)) {
                String docName = entry.getFileName();
                try {
                    NuMLDocument doc = CombineArchiveUtil.readData(entry.getPath());
                    dataDocMap.put(docName, doc);
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    LOGGER.warning(docName + " could not be read");
                    e.printStackTrace();
                }
            }

            Element metaParent = ca.getDescriptions().get(0).getXmlDescription();
            Set<String> masterFiles = new PMFMetadataNode(metaParent).masterFiles;

            // Classify models into tertiary or secondary models
            for (ArchiveEntry entry : ca.getEntriesWithFormat(modelUri)) {
                String docName = entry.getFileName();

                try {
                    SBMLDocument doc = CombineArchiveUtil.readModel(entry.getPath());

                    if (masterFiles.contains(docName)) {
                        tertDocs.put(docName, doc);
                    } else if (doc.getModel().getListOfSpecies().size() == 0) {
                        secDocs.put(docName, doc);
                    } else {
                        primDocs.put(docName, doc);
                    }
                } catch (IOException | XMLStreamException e) {
                    LOGGER.warning(docName + " could not be read");
                    e.printStackTrace();
                }
            }

        } catch (IOException | JDOMException | ParseException e) {
            throw new CombineArchiveException(file.getName() + " could not be opened");
        }

        for (Map.Entry<String, SBMLDocument> entry : tertDocs.entrySet()) {
            String tertDocName = entry.getKey();
            SBMLDocument tertDoc = entry.getValue();

            List<String> secModelNames = new ArrayList<>();
            List<SBMLDocument> secModels = new ArrayList<>();
            List<PrimaryModelWData> primModels = new ArrayList<>();

            CompSBMLDocumentPlugin secCompPlugin = (CompSBMLDocumentPlugin) tertDoc.getPlugin(CompConstants.shortLabel);

            // Gets secondary model documents
            for (ExternalModelDefinition emd : secCompPlugin.getListOfExternalModelDefinitions()) {
                String secModelName = emd.getSource();
                secModelNames.add(secModelName);

                SBMLDocument secModel = secDocs.get(secModelName);
                secModels.add(secModel);
            }

            /*
             * All the secondary models of a two step tertiary model are linked to the same primary models. Thus
             * these primary models can be retrieved from the first secondary model.
             */
            Model md = secModels.get(0).getModel();
            XMLNode metadata = md.getAnnotation().getNonRDFannotation().getChildElement("metadata", "");
            for (XMLNode pmNode : metadata.getChildElements(PrimaryModelNode.TAG, "")) {
                // Gets model name from annotation
                String mdName = pmNode.getChild(0).getCharacters();
                // Gets primary model
                SBMLDocument mdDoc = primDocs.get(mdName);
                // Gets data source annotation of the primary model
                XMLNode mdDocMetadata = mdDoc.getModel().getAnnotation().getNonRDFannotation().getChildElement
                        ("metadata", "");
                XMLNode node = mdDocMetadata.getChildElement("dataSource", "");
                // Gets data name from this annotation
                String dataName = new DataSourceNode(node).getFile();
                // Gets data file
                NuMLDocument dataDoc = dataDocMap.get(dataName);

                primModels.add(new PrimaryModelWData(mdName, mdDoc, dataName, dataDoc));
            }

            models.add(new TwoStepTertiaryModel(tertDocName, tertDoc, primModels, secModelNames, secModels));
        }

        return models;
    }

    /**
     * Writes two step tertiary models to a PMF or PMFX file. Faulty models are skipped. Existent
     * files are overwritten.
     *
     * @param file
     * @param modelUri
     * @param models
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    private static void write(File file, URI modelUri, List<TwoStepTertiaryModel> models)
            throws CombineArchiveException {

        // Remove if existent file
        if (file.exists()) {
            file.delete();
        }

        // Creates COMBINE archive
        try (CombineArchive ca = new CombineArchive(file)) {
            Set<String> masterFiles = new HashSet<>(models.size());

            // Adds models and data
            for (TwoStepTertiaryModel model : models) {

                List<ArchiveEntry> addedEntries = new ArrayList<>();

                for (PrimaryModelWData pm : model.getPrimModels()) {
                    try {
                        addedEntries.add(CombineArchiveUtil.writeData(ca, pm.getDataDoc(), pm.getDataDocName()));
                        addedEntries.add(CombineArchiveUtil.writeModel(ca, pm.getModelDoc(), pm.getModelDocName(),
                                modelUri));
                    } catch (IOException | TransformerException | ParserConfigurationException | XMLStreamException e) {
                        LOGGER.warning(pm.getModelDocName() + " could not be saved");
                        e.printStackTrace();
                    }
                }

                for (int i = 0; i < model.getSecDocs().size(); i++) {
                    String secDocName = model.getSecDocNames().get(i);
                    SBMLDocument secDoc = model.getSecDocs().get(i);

                    try {
                        CombineArchiveUtil.writeModel(ca, secDoc, secDocName, modelUri);
                    } catch (IOException | SBMLException | XMLStreamException e) {
                        LOGGER.warning(secDocName + " could not be saved");
                        e.printStackTrace();
                    }
                }

                // Tertiary model
                try {
                    ArchiveEntry masterEntry = CombineArchiveUtil.writeModel(ca, model.getTertDoc(), model
                            .getTertDocName(), modelUri);
                    masterFiles.add(masterEntry.getPath().getFileName().toString());
                } catch (IOException | SBMLException | XMLStreamException e) {
                    LOGGER.warning(model.getTertDocName() + " : could not be saved");
                    e.printStackTrace();
                }
            }

            // Adds description with model type
            Element annot = new PMFMetadataNode(ModelType.TWO_STEP_TERTIARY_MODEL, masterFiles).node;
            ca.addDescription(new DefaultMetaDataObject(annot));

            ca.pack();
        } catch (IOException | JDOMException | ParseException | TransformerException e) {
            throw new CombineArchiveException(file.getName() + " could not be opened");
        }
    }
}
