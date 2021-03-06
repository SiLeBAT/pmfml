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
import de.bund.bfr.pmfml.model.OneStepSecondaryModel;
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
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbml.jsbml.xml.XMLNode;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Case 2b: One step secondary model file. Secondary models generated "implicitly" during 1-step
 * fitting of tertiary models.
 *
 * @author Miguel Alba
 */
public class OneStepSecondaryModelFile {

    private static final Logger LOGGER = Logger.getLogger("OneStepSecondaryModelFile");

    /**
     * @deprecated use {@link OneStepSecondaryModelFile#read(Path)} instead
     */
    @Deprecated
    public static List<OneStepSecondaryModel> readPMF(final File file) throws CombineArchiveException {
        return read(file, URIS.sbml);
    }

    /**
     * @deprecated use {@link OneStepSecondaryModelFile#read(Path)} instead
     */
    @Deprecated
    public static List<OneStepSecondaryModel> readPMFX(final File file) throws CombineArchiveException {
        return read(file, URIS.pmf);
    }

    /**
     * @deprecated use {@link OneStepSecondaryModelFile#write(Path, List)} instead
     */
    @Deprecated
    public static void writePMF(final String dir, final String filename,
                                final List<OneStepSecondaryModel> models) throws CombineArchiveException {

        // Creates CombineArchive name
        String caName = dir + "/" + filename + ".pmf";
        write(new File(caName), URIS.sbml, models);
    }

    /**
     * @deprecated use {@link OneStepSecondaryModelFile#write(Path, List)} instead
     */
    @Deprecated
    public static void writePMFX(final String dir, final String filename,
                                 final List<OneStepSecondaryModel> models) throws CombineArchiveException {

        // Creates CombineArchive name
        String caName = dir + "/" + filename + ".pmfx";
        write(new File(caName), URIS.pmf, models);
    }

    /**
     * Reads {@link OneStepSecondaryModel}(s) from a PMF or PMFX file. Faulty models are skipped.
     *
     * @param file
     * @param modelUri
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     * @deprecated use {@link OneStepSecondaryModelFile#read(Path)} instead
     */
    @Deprecated
    private static List<OneStepSecondaryModel> read(File file, URI modelUri) throws CombineArchiveException {

        try (CombineArchive ca = new CombineArchive(file)) {

            List<OneStepSecondaryModel> models = new ArrayList<>();

            // Gets data entries
            Map<String, NuMLDocument> dataEntryMap = new HashMap<>();
            for (ArchiveEntry entry : ca.getEntriesWithFormat(URIS.numl)) {
                try {
                    NuMLDocument doc = CombineArchiveUtil.readData(entry.getPath());
                    dataEntryMap.put(entry.getFileName(), doc);
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    LOGGER.warning(entry.getFileName() + ": could not be read");
                }
            }

            for (ArchiveEntry entry : ca.getEntriesWithFormat(modelUri)) {
                String modelName = entry.getFileName();

                SBMLDocument modelDoc;
                try {
                    modelDoc = CombineArchiveUtil.readModel(entry.getPath());
                } catch (IOException | SBMLException | XMLStreamException e) {
                    LOGGER.warning(entry.getFileName() + ": could not be read");
                    continue;
                }

                // Looks for DataSourceNode
                CompSBMLDocumentPlugin secCompPlugin = (CompSBMLDocumentPlugin) modelDoc.getPlugin(CompConstants
                        .shortLabel);
                ModelDefinition md = secCompPlugin.getModelDefinition(0);
                XMLNode m2Annot = md.getAnnotation().getNonRDFannotation();
                XMLNode metadata = m2Annot.getChildElement("metadata", "");

                List<String> numlDocNames = new ArrayList<>();
                List<NuMLDocument> numlDocs = new ArrayList<>();

                for (XMLNode node : metadata.getChildElements("dataSource", "")) {
                    DataSourceNode dsn = new DataSourceNode(node);
                    String dataFileName = dsn.getFile();

                    numlDocNames.add(dataFileName);
                    numlDocs.add(dataEntryMap.get(dataFileName));
                }

                models.add(new OneStepSecondaryModel(modelName, modelDoc, numlDocNames, numlDocs));
            }

            return models;

        } catch (IOException | ParseException | JDOMException e) {
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }
    }

    /**
     * Reads {@link OneStepSecondaryModel}(s) from a file. Faulty models are skipped.
     *
     * @param path
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    public static List<OneStepSecondaryModel> read(Path path) throws CombineArchiveException {

        URI modelUri = CombineArchiveUtil.getModelURI(path);

        try (CombineArchive ca = new CombineArchive(path.toFile())) {

            List<OneStepSecondaryModel> models = new ArrayList<>();

            // Gets data entries
            Map<String, NuMLDocument> dataEntryMap = new HashMap<>();
            for (ArchiveEntry entry : ca.getEntriesWithFormat(URIS.numl)) {
                try {
                    NuMLDocument doc = CombineArchiveUtil.readData(entry.getPath());
                    dataEntryMap.put(entry.getFileName(), doc);
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    LOGGER.warning(entry.getFileName() + ": could not be read");
                }
            }

            for (ArchiveEntry entry : ca.getEntriesWithFormat(modelUri)) {
                String modelName = entry.getFileName();

                SBMLDocument modelDoc;
                try {
                    modelDoc = CombineArchiveUtil.readModel(entry.getPath());
                } catch (IOException | SBMLException | XMLStreamException e) {
                    LOGGER.warning(entry.getFileName() + ": could not be read");
                    continue;
                }

                // Looks for DataSourceNode
                CompSBMLDocumentPlugin secCompPlugin = (CompSBMLDocumentPlugin) modelDoc.getPlugin(CompConstants
                        .shortLabel);
                ModelDefinition md = secCompPlugin.getModelDefinition(0);
                XMLNode m2Annot = md.getAnnotation().getNonRDFannotation();
                XMLNode metadata = m2Annot.getChildElement("metadata", "");

                List<String> numlDocNames = new ArrayList<>();
                List<NuMLDocument> numlDocs = new ArrayList<>();

                for (XMLNode node : metadata.getChildElements("dataSource", "")) {
                    DataSourceNode dsn = new DataSourceNode(node);
                    String dataFileName = dsn.getFile();

                    numlDocNames.add(dataFileName);
                    numlDocs.add(dataEntryMap.get(dataFileName));
                }

                models.add(new OneStepSecondaryModel(modelName, modelDoc, numlDocNames, numlDocs));
            }

            return models;

        } catch (IOException | ParseException | JDOMException e) {
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }
    }

    /**
     * Writes one step secondary models to a PMF or PMFX file. Faulty models are skipped. Existent
     * files are overwritten.
     *
     * @param file
     * @param modelUri
     * @param models
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     * @deprecated use {@link OneStepSecondaryModelFile#write(Path, List)} instead
     */
    @Deprecated
    private static void write(File file, URI modelUri, List<OneStepSecondaryModel> models)
        throws CombineArchiveException {

        // Remove if existent file
        if (file.exists()) {
            file.delete();
        }

        // Creates COMBINE archive
        try (CombineArchive ca = new CombineArchive(file)) {

            for (OneStepSecondaryModel model : models) {
                List<ArchiveEntry> addedEntries = new ArrayList<>();

                try {
                    for (int i = 0; i < model.getDataDocs().size(); i++) {
                        String dataDocName = model.getDataDocNames().get(i);
                        NuMLDocument dataDoc = model.getDataDocs().get(i);
                        addedEntries.add(CombineArchiveUtil.writeData(ca, dataDoc, dataDocName));
                    }

                    addedEntries.add(CombineArchiveUtil.writeModel(ca, model.getModelDoc(), model.getModelDocName(),
                            modelUri));
                } catch (IOException | TransformerFactoryConfigurationError | TransformerException |
                        ParserConfigurationException | SBMLException | XMLStreamException e) {
                    LOGGER.warning(model.getModelDocName() + ": could not be read");
                    for (ArchiveEntry entry : addedEntries) {
                        ca.removeEntry(entry);
                    }
                }
            }

            Element annot = new PMFMetadataNode(ModelType.ONE_STEP_SECONDARY_MODEL, Collections.emptySet()).node;
            ca.addDescription(new DefaultMetaDataObject(annot));

            CombineArchiveUtil.addReadme(ca);

            ca.pack();

        } catch (Exception e) {
            file.delete();  // Removes faulty file
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }
    }

    /**
     * Writes one step secondary models to a PMF or PMFX file. Faulty models are skipped. Existent
     * files are overwritten.
     *
     * @param path
     * @param models
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    public static void write(Path path, List<OneStepSecondaryModel> models)
            throws CombineArchiveException, IOException {

        URI modelUri = CombineArchiveUtil.getModelURI(path);

        // Remove if existent file
        Files.deleteIfExists(path);

        // Creates COMBINE archive
        try (CombineArchive ca = new CombineArchive(path.toFile())) {

            for (OneStepSecondaryModel model : models) {
                List<ArchiveEntry> addedEntries = new ArrayList<>();

                try {
                    for (int i = 0; i < model.getDataDocs().size(); i++) {
                        String dataDocName = model.getDataDocNames().get(i);
                        NuMLDocument dataDoc = model.getDataDocs().get(i);
                        addedEntries.add(CombineArchiveUtil.writeData(ca, dataDoc, dataDocName));
                    }

                    addedEntries.add(CombineArchiveUtil.writeModel(ca, model.getModelDoc(), model.getModelDocName(),
                            modelUri));
                } catch (IOException | TransformerFactoryConfigurationError | TransformerException |
                        ParserConfigurationException | SBMLException | XMLStreamException e) {
                    LOGGER.warning(model.getModelDocName() + ": could not be read");
                    for (ArchiveEntry entry : addedEntries) {
                        ca.removeEntry(entry);
                    }
                }
            }

            Element annot = new PMFMetadataNode(ModelType.ONE_STEP_SECONDARY_MODEL, Collections.emptySet()).node;
            ca.addDescription(new DefaultMetaDataObject(annot));

            CombineArchiveUtil.addReadme(ca);

            ca.pack();

        } catch (Exception e) {
            Files.delete(path);  // Removes faulty file
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }
    }
}
