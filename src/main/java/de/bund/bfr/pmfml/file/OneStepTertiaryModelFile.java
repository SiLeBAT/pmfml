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
import de.bund.bfr.pmfml.model.OneStepTertiaryModel;
import de.bund.bfr.pmfml.numl.NuMLDocument;
import de.bund.bfr.pmfml.sbml.DataSourceNode;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.xml.XMLNode;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.*;

/**
 * Case 3b: File with tertiary model generated with 1-step fit approach.
 *
 * @author Miguel Alba
 */
public class OneStepTertiaryModelFile {

    private static final URI SBML_URI = UriFactory.createSBMLURI();
    private static final URI PMF_URI = UriFactory.createPMFURI();
    private static final URI NuML_URI = UriFactory.createNuMLURI();

    private static final SBMLWriter WRITER = new SBMLWriter();

    public static List<OneStepTertiaryModel> readPMF(final File file) throws Exception {
        return read(file, SBML_URI);
    }

    public static List<OneStepTertiaryModel> readPMFX(final File file) throws Exception {
        return read(file, PMF_URI);
    }

    public static void writePMF(final String dir, final String filename,
                                final List<OneStepTertiaryModel> models) throws Exception {
        String caName = dir + "/" + filename + ".pmf";
        write(new File(caName), SBML_URI, models);
    }

    public static void writePMFX(String dir, final String filename,
                                 final List<OneStepTertiaryModel> models) throws Exception {
        String caName = dir + "/" + filename + ".pmfx";
        write(new File(caName), PMF_URI, models);
    }

    /**
     * Reads one step tertiary models from a PMF or PMFX file. Faulty models are skipped.
     *
     * @param file
     * @param modelURI
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    private static List<OneStepTertiaryModel> read(final File file, final URI modelURI)
            throws CombineArchiveException {

        // Creates CombineArchive
        CombineArchive combineArchive;
        try {
            combineArchive = new CombineArchive(file);
        } catch (IOException | JDOMException | ParseException error) {
            throw new CombineArchiveException(file.getName() + " could not be opened");
        }

        final List<OneStepTertiaryModel> models = new LinkedList<>();

        // Get data entries
        final List<ArchiveEntry> dataEntriesList = combineArchive.getEntriesWithFormat(NuML_URI);
        final Map<String, NuMLDocument> dataEntriesMap = new HashMap<>(dataEntriesList.size());
        for (final ArchiveEntry entry : dataEntriesList) {
            final String dataDocName = entry.getFileName();
            try {
                final NuMLDocument dataDoc = CombineArchiveUtil.readData(entry.getPath());
                dataEntriesMap.put(dataDocName, dataDoc);
            } catch (IOException | ParserConfigurationException | SAXException e) {
                System.err.println(dataDocName + " could not be read");
                e.printStackTrace();
            }
        }

        // Classify models into tertiary or secondary models
        final Map<String, SBMLDocument> tertDocs = new HashMap<>();
        final Map<String, SBMLDocument> secDocs = new HashMap<>();

        final Element metaParent = combineArchive.getDescriptions().get(0).getXmlDescription();
        final Set<String> masterFiles = new PMFMetadataNode(metaParent).masterFiles;

        for (final ArchiveEntry entry : combineArchive.getEntriesWithFormat(modelURI)) {
            final String docName = entry.getFileName();
            try {
                final SBMLDocument doc = CombineArchiveUtil.readModel(entry.getPath());
                if (masterFiles.contains(docName)) {
                    tertDocs.put(docName, doc);
                } else {
                    secDocs.put(docName, doc);
                }
            } catch (IOException | XMLStreamException e) {
                System.err.println(docName + " could not be read");
                e.printStackTrace();
            }
        }

        CombineArchiveUtil.close(combineArchive);

        for (final Map.Entry<String, SBMLDocument> entry : tertDocs.entrySet()) {
            final String tertDocName = entry.getKey();
            final SBMLDocument tertDoc = entry.getValue();

            final List<SBMLDocument> secModels = new LinkedList<>();
            final List<String> secModelNames = new LinkedList<>();

            final CompSBMLDocumentPlugin tertPlugin =
                    (CompSBMLDocumentPlugin) tertDoc.getPlugin(CompConstants.shortLabel);
            // Gets secondary model ids
            final ListOf<ExternalModelDefinition> emds = tertPlugin.getListOfExternalModelDefinitions();
            for (final ExternalModelDefinition emd : emds) {
                final String secModelName = emd.getSource();
                secModelNames.add(secModelName);

                final SBMLDocument secDoc = secDocs.get(secModelName);
                secModels.add(secDoc);
            }

            // Gets data files from the tertiary model document
            final List<String> numlDocNames = new LinkedList<>();
            final List<NuMLDocument> numlDocs = new LinkedList<>();

            final XMLNode tertAnnot = tertDoc.getModel().getAnnotation().getNonRDFannotation();
            final XMLNode tertAnnotMetadata = tertAnnot.getChildElement("metadata", "");
            for (final XMLNode node : tertAnnotMetadata.getChildElements(DataSourceNode.TAG, "")) {
                final String numlDocName = new DataSourceNode(node).getFile();
                numlDocNames.add(numlDocName);

                final NuMLDocument numlDoc = dataEntriesMap.get(numlDocName);
                numlDocs.add(numlDoc);
            }
            models.add(new OneStepTertiaryModel(tertDocName, tertDoc, secModelNames, secModels,
                    numlDocNames, numlDocs));
        }

        return models;
    }

    /**
     * Writes one step tertiary models to a PMF or PMFX files. Faulty models are skipped. Existent
     * files are overwritten.
     *
     * @param file
     * @param modelURI
     * @param models
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    private static void write(final File file, final URI modelURI,
                              final List<OneStepTertiaryModel> models) throws CombineArchiveException {

        // Remove if existent file
        if (file.exists()) {
            file.delete();
        }

        // Creates new CombineArchive
        final CombineArchive combineArchive;
        try {
            combineArchive = new CombineArchive(file);
        } catch (IOException | JDOMException | ParseException error) {
            throw new CombineArchiveException(file.getName() + " could not be opened");
        }

        final Set<String> masterFiles = new HashSet<>(models.size());

        // Adds models
        for (final OneStepTertiaryModel model : models) {

            for (int i = 0; i < model.getDataDocs().size(); i++) {
                final String numlDocName = model.getDataDocNames().get(i);
                final NuMLDocument numlDoc = model.getDataDocs().get(i);
                try {
                    CombineArchiveUtil.writeData(combineArchive, numlDoc, numlDocName);
                } catch (IOException | TransformerFactoryConfigurationError | TransformerException
                        | ParserConfigurationException e) {
                    System.err.println(numlDocName + " could not be saved");
                    e.printStackTrace();
                }
            }

            // Creates tmp file for the tertiary model
            try {
                final File tertTmp = File.createTempFile("tert", "");
                tertTmp.deleteOnExit();

                // Writes tertiary model to tertTmp and adds it to the model
                WRITER.write(model.getTertiaryDoc(), tertTmp);
                final ArchiveEntry masterEntry =
                        combineArchive.addEntry(tertTmp, model.getTertiaryDocName(), modelURI);
                masterFiles.add(masterEntry.getPath().getFileName().toString());
            } catch (IOException | SBMLException | XMLStreamException e) {
                System.err.println(model.getTertiaryDocName() + " could not be saved");
                e.printStackTrace();
                continue;
            }

            for (int i = 0; i < model.getSecDocs().size(); i++) {
                final String secDocName = model.getSecDocNames().get(i);
                final SBMLDocument secDoc = model.getSecDocs().get(i);

                try {
                    CombineArchiveUtil.writeModel(combineArchive, secDoc, secDocName, modelURI);
                } catch (IOException | SBMLException | XMLStreamException e) {
                    System.err.println(secDocName + " could not be saved");
                    e.printStackTrace();
                }
            }
        }

        // Adds description with model type
        final ModelType modelType = ModelType.ONE_STEP_TERTIARY_MODEL;
        final Element metadataAnnotation = new PMFMetadataNode(modelType, masterFiles).node;
        combineArchive.addDescription(new DefaultMetaDataObject(metadataAnnotation));

        CombineArchiveUtil.pack(combineArchive);
        CombineArchiveUtil.close(combineArchive);
    }
}
