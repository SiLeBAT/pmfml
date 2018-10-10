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
import de.bund.bfr.pmfml.model.OneStepTertiaryModel;
import de.bund.bfr.pmfml.numl.NuMLDocument;
import de.bund.bfr.pmfml.sbml.DataSourceNode;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import org.jdom2.Element;
import org.jdom2.JDOMException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Case 3b: File with tertiary model generated with 1-step fit approach.
 *
 * @author Miguel Alba
 */
public class OneStepTertiaryModelFile {

    private static Logger LOGGER = Logger.getLogger("OneStepTertiaryModelFile");

    /**
     * @deprecated use {@link OneStepTertiaryModelFile#read(Path)} instead
     */
    @Deprecated
    public static List<OneStepTertiaryModel> readPMF(final File file) throws CombineArchiveException {
        return read(file, URIS.sbml);
    }

    /**
     * @deprecated use {@link OneStepTertiaryModelFile#read(Path)} instead
     */
    @Deprecated
    public static List<OneStepTertiaryModel> readPMFX(final File file) throws CombineArchiveException {
        return read(file, URIS.pmf);
    }

    /**
     * @deprecated use {@link OneStepTertiaryModelFile#write(Path, List)} instead
     */
    @Deprecated
    public static void writePMF(final String dir, final String filename,
                                final List<OneStepTertiaryModel> models) throws CombineArchiveException {
        String caName = dir + "/" + filename + ".pmf";
        write(new File(caName), URIS.sbml, models);
    }

    /**
     * @deprecated use {@link OneStepTertiaryModelFile#write(Path, List)} instead
     */
    @Deprecated
    public static void writePMFX(String dir, final String filename,
                                 final List<OneStepTertiaryModel> models) throws CombineArchiveException {
        String caName = dir + "/" + filename + ".pmfx";
        write(new File(caName), URIS.pmf, models);
    }

    /**
     * Reads one step tertiary models from a PMF or PMFX file. Faulty models are skipped.
     *
     * @param file
     * @param modelUri
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     * @deprecated use {@link OneStepTertiaryModelFile#read(Path)} instead
     */
    @Deprecated
    private static List<OneStepTertiaryModel> read(File file, URI modelUri) throws CombineArchiveException {

        Map<String, SBMLDocument> tertDocs = new HashMap<>();
        Map<String, SBMLDocument> secDocs = new HashMap<>();
        Map<String, NuMLDocument> dataDocs = new HashMap<>();

        try (CombineArchive ca = new CombineArchive(file)) {

            // Gets data entries
            for (ArchiveEntry entry : ca.getEntriesWithFormat(URIS.numl)) {
                String dataDocName = entry.getFileName();
                try {
                    NuMLDocument dataDoc = CombineArchiveUtil.readData(entry.getPath());
                    dataDocs.put(dataDocName, dataDoc);
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    LOGGER.warning(dataDocName + " could not be read");
                    e.printStackTrace();
                }
            }

            // Classify models into tertiary or secondary models

            Element metaParent = ca.getDescriptions().get(0).getXmlDescription();
            Set<String> masterFiles = new PMFMetadataNode(metaParent).masterFiles;

            for (ArchiveEntry entry : ca.getEntriesWithFormat(modelUri)) {
                String docName = entry.getFileName();

                try {
                    SBMLDocument doc = CombineArchiveUtil.readModel(entry.getPath());
                    if (masterFiles.contains(docName)) {
                        tertDocs.put(docName, doc);
                    } else {
                        secDocs.put(docName, doc);
                    }
                } catch (IOException | XMLStreamException e) {
                    LOGGER.warning(docName + " could not be read");
                    e.printStackTrace();
                }
            }
        } catch (IOException | JDOMException | ParseException error) {
            throw new CombineArchiveException(file.getName() + " could not be opened");
        }


        List<OneStepTertiaryModel> models = new ArrayList<>();
        for (Map.Entry<String, SBMLDocument> entry : tertDocs.entrySet()) {
            String tertDocName = entry.getKey();
            SBMLDocument tertDoc = entry.getValue();

            List<String> secModelNames = new ArrayList<>();
            List<SBMLDocument> secModels = new ArrayList<>();

            // Gets secondary models
            CompSBMLDocumentPlugin tertPlugin = (CompSBMLDocumentPlugin) tertDoc.getPlugin(CompConstants.shortLabel);
            for (ExternalModelDefinition emd : tertPlugin.getListOfExternalModelDefinitions()) {
                String secModelName = emd.getSource();
                secModelNames.add(secModelName);

                SBMLDocument secDoc = secDocs.get(secModelName);
                secModels.add(secDoc);
            }

            // Gets data files from the tertiary model document
            List<String> numlDocNames = new ArrayList<>();
            List<NuMLDocument> numlDocs = new ArrayList<>();

            XMLNode tertAnnot = tertDoc.getModel().getAnnotation().getNonRDFannotation();
            XMLNode tertAnnotMetadata = tertAnnot.getChildElement("metadata", "");
            for (XMLNode node : tertAnnotMetadata.getChildElements(DataSourceNode.TAG, "")) {
                String numlDocName = new DataSourceNode(node).getFile();
                numlDocNames.add(numlDocName);
                numlDocs.add(dataDocs.get(numlDocName));
            }

            models.add(new OneStepTertiaryModel(tertDocName, tertDoc, secModelNames, secModels, numlDocNames,
                    numlDocs));
        }

        return models;
    }

    /**
     * Reads one step tertiary models from a file. Faulty models are skipped.
     *
     * @param path
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    public static List<OneStepTertiaryModel> read(Path path) throws CombineArchiveException {

        URI modelUri = CombineArchiveUtil.getModelURI(path);

        Map<String, SBMLDocument> tertDocs = new HashMap<>();
        Map<String, SBMLDocument> secDocs = new HashMap<>();
        Map<String, NuMLDocument> dataDocs = new HashMap<>();

        try (CombineArchive ca = new CombineArchive(path.toFile())) {

            // Gets data entries
            for (ArchiveEntry entry : ca.getEntriesWithFormat(URIS.numl)) {
                String dataDocName = entry.getFileName();
                try {
                    NuMLDocument dataDoc = CombineArchiveUtil.readData(entry.getPath());
                    dataDocs.put(dataDocName, dataDoc);
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    LOGGER.warning(dataDocName + " could not be read");
                    e.printStackTrace();
                }
            }

            // Classify models into tertiary or secondary models

            Element metaParent = ca.getDescriptions().get(0).getXmlDescription();
            Set<String> masterFiles = new PMFMetadataNode(metaParent).masterFiles;

            for (ArchiveEntry entry : ca.getEntriesWithFormat(modelUri)) {
                String docName = entry.getFileName();

                try {
                    SBMLDocument doc = CombineArchiveUtil.readModel(entry.getPath());
                    if (masterFiles.contains(docName)) {
                        tertDocs.put(docName, doc);
                    } else {
                        secDocs.put(docName, doc);
                    }
                } catch (IOException | XMLStreamException e) {
                    LOGGER.warning(docName + " could not be read");
                    e.printStackTrace();
                }
            }
        } catch (IOException | JDOMException | ParseException error) {
            throw new CombineArchiveException(path.getFileName() + " could not be opened");
        }


        List<OneStepTertiaryModel> models = new ArrayList<>();
        for (Map.Entry<String, SBMLDocument> entry : tertDocs.entrySet()) {
            String tertDocName = entry.getKey();
            SBMLDocument tertDoc = entry.getValue();

            List<String> secModelNames = new ArrayList<>();
            List<SBMLDocument> secModels = new ArrayList<>();

            // Gets secondary models
            CompSBMLDocumentPlugin tertPlugin = (CompSBMLDocumentPlugin) tertDoc.getPlugin(CompConstants.shortLabel);
            for (ExternalModelDefinition emd : tertPlugin.getListOfExternalModelDefinitions()) {
                String secModelName = emd.getSource();
                secModelNames.add(secModelName);

                SBMLDocument secDoc = secDocs.get(secModelName);
                secModels.add(secDoc);
            }

            // Gets data files from the tertiary model document
            List<String> numlDocNames = new ArrayList<>();
            List<NuMLDocument> numlDocs = new ArrayList<>();

            XMLNode tertAnnot = tertDoc.getModel().getAnnotation().getNonRDFannotation();
            XMLNode tertAnnotMetadata = tertAnnot.getChildElement("metadata", "");
            for (XMLNode node : tertAnnotMetadata.getChildElements(DataSourceNode.TAG, "")) {
                String numlDocName = new DataSourceNode(node).getFile();
                numlDocNames.add(numlDocName);
                numlDocs.add(dataDocs.get(numlDocName));
            }

            models.add(new OneStepTertiaryModel(tertDocName, tertDoc, secModelNames, secModels, numlDocNames,
                    numlDocs));
        }

        return models;
    }

    /**
     * Writes one step tertiary models to a PMF or PMFX files. Faulty models are skipped. Existent
     * files are overwritten.
     *
     * @param file
     * @param modelUri
     * @param models
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     * @deprecated use {@link OneStepTertiaryModelFile#write(Path, List)} instead
     */
    @Deprecated
    private static void write(File file, URI modelUri, List<OneStepTertiaryModel> models)
            throws CombineArchiveException {
        // Remove if existent file
        if (file.exists()) {
            file.delete();
        }

        // Creates COMBINE archive
        try (CombineArchive ca = new CombineArchive(file)) {
            Set<String> masterFiles = new HashSet<>(models.size());

            // Adds models
            for (OneStepTertiaryModel model : models) {
                for (int i = 0; i < model.getDataDocs().size(); i++) {
                    String numlDocName = model.getDataDocNames().get(i);
                    NuMLDocument numlDoc = model.getDataDocs().get(i);

                    try {
                        CombineArchiveUtil.writeData(ca, numlDoc, numlDocName);
                    } catch (IOException | TransformerException | ParserConfigurationException
                            e) {
                        LOGGER.warning(numlDocName + " could not be saved");
                        e.printStackTrace();
                    }
                }

                // Tertiary model
                try {
                    ArchiveEntry masterEntry = CombineArchiveUtil.writeModel(ca, model.getTertiaryDoc(), model
                            .getTertiaryDocName(), modelUri);
                    masterFiles.add(masterEntry.getPath().getFileName().toString());
                } catch (IOException | SBMLException | XMLStreamException e) {
                    LOGGER.warning(model.getTertiaryDocName() + " could not be saved");
                    e.printStackTrace();
                    continue;
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
            }

            // Adds description with model type
            Element annot = new PMFMetadataNode(ModelType.ONE_STEP_TERTIARY_MODEL, masterFiles).node;
            ca.addDescription(new DefaultMetaDataObject(annot));

            CombineArchiveUtil.addReadme(ca);

            ca.pack();

        } catch (Exception e) {
            file.delete();  // Removes faulty file
            e.printStackTrace();
            throw new CombineArchiveException(file.getName() + " could not be opened");
        }
    }

    /**
     * Writes one step tertiary models to a file. Faulty models are skipped. Existent
     * files are overwritten.
     *
     * @param path
     * @param models
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    public static void write(Path path, List<OneStepTertiaryModel> models)
            throws CombineArchiveException, IOException {

        URI modelUri = CombineArchiveUtil.getModelURI(path);

        // Remove if existent file
        Files.deleteIfExists(path);

        // Creates COMBINE archive
        try (CombineArchive ca = new CombineArchive(path.toFile())) {
            Set<String> masterFiles = new HashSet<>(models.size());

            // Adds models
            for (OneStepTertiaryModel model : models) {
                for (int i = 0; i < model.getDataDocs().size(); i++) {
                    String numlDocName = model.getDataDocNames().get(i);
                    NuMLDocument numlDoc = model.getDataDocs().get(i);

                    try {
                        CombineArchiveUtil.writeData(ca, numlDoc, numlDocName);
                    } catch (IOException | TransformerException | ParserConfigurationException
                            e) {
                        LOGGER.warning(numlDocName + " could not be saved");
                        e.printStackTrace();
                    }
                }

                // Tertiary model
                try {
                    ArchiveEntry masterEntry = CombineArchiveUtil.writeModel(ca, model.getTertiaryDoc(), model
                            .getTertiaryDocName(), modelUri);
                    masterFiles.add(masterEntry.getPath().getFileName().toString());
                } catch (IOException | SBMLException | XMLStreamException e) {
                    LOGGER.warning(model.getTertiaryDocName() + " could not be saved");
                    e.printStackTrace();
                    continue;
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
            }

            // Adds description with model type
            Element annot = new PMFMetadataNode(ModelType.ONE_STEP_TERTIARY_MODEL, masterFiles).node;
            ca.addDescription(new DefaultMetaDataObject(annot));

            CombineArchiveUtil.addReadme(ca);

            ca.pack();

        } catch (Exception e) {
            Files.delete(path);  // Remove faulty file
            e.printStackTrace();
            throw new CombineArchiveException(path.getFileName() + " could not be opened");
        }
    }
}
