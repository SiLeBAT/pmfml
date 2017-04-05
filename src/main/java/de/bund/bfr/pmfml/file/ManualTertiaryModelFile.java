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
import de.bund.bfr.pmfml.model.ManualTertiaryModel;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import de.unirostock.sems.cbarchive.meta.MetaDataObject;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Case 2c: Manual secondary models. Secondary models generated manually.
 *
 * @author Miguel Alba
 */
public class ManualTertiaryModelFile {

    private static final Logger LOGGER = Logger.getLogger("ManualTertiaryModelFile");

    /**
     * @deprecated use {@link ManualTertiaryModelFile#read(Path)} instead
     */
    @Deprecated
    public static List<ManualTertiaryModel> readPMF(final File file) throws CombineArchiveException {
        return read(file, URIS.sbml);
    }

    /**
     * @deprecated use {@link ManualTertiaryModelFile#read(Path)} instead
     */
    @Deprecated
    public static List<ManualTertiaryModel> readPMFX(final File file) throws CombineArchiveException {
        return read(file, URIS.pmf);
    }

    /**
     * @deprecated use {@link ManualTertiaryModelFile#write(Path, List)} instead
     */
    @Deprecated
    public static void writePMF(final String dir, final String filename,
                                final List<ManualTertiaryModel> models) throws CombineArchiveException {
        // Creates CombineArchive name
        String caName = dir + "/" + filename + ".pmf";
        write(new File(caName), URIS.sbml, models);
    }

    /**
     * @deprecated use {@link ManualTertiaryModelFile#write(Path, List)} instead
     */
    @Deprecated
    public static void writePMFX(final String dir, final String filename,
                                 final List<ManualTertiaryModel> models) throws CombineArchiveException {
        // Creates CombineArchive name
        String caName = dir + "/" + filename + ".pmfx";
        write(new File(caName), URIS.pmf, models);
    }

    /**
     * Reads manual tertiary models from a PMF or PFMX file. Faulty models are skipped.
     *
     * @param file
     * @param modelUri
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     * @deprecated use {@link ManualTertiaryModelFile#read(Path)} instead
     */
    @Deprecated
    private static List<ManualTertiaryModel> read(File file, URI modelUri)
        throws CombineArchiveException {

        Map<String, SBMLDocument> tertDocMap = new HashMap<>();
        Map<String, SBMLDocument> secDocMap = new HashMap<>();

        try (CombineArchive ca = new CombineArchive(file)) {

            MetaDataObject mdo = ca.getDescriptions().get(0);
            Element metaParent = mdo.getXmlDescription();
            PMFMetadataNode metadataAnnotation = new PMFMetadataNode(metaParent);
            Set<String> masterFiles = metadataAnnotation.masterFiles;

            // Classify models into tertiary or secondary models
            for (ArchiveEntry entry : ca.getEntriesWithFormat(modelUri)) {
                String docName = entry.getFileName();
                try {
                    SBMLDocument doc = CombineArchiveUtil.readModel(entry.getPath());
                    if (masterFiles.contains(docName)) {
                        tertDocMap.put(docName, doc);
                    } else {
                        secDocMap.put(docName, doc);
                    }
                } catch (IOException | XMLStreamException e) {
                    LOGGER.warning(docName + " could not be read");
                }
            }
        } catch (IOException | JDOMException | ParseException error) {
            throw new CombineArchiveException(file.getName() + " could not be opened");
        }

        List<ManualTertiaryModel> models = new ArrayList<>();
        for (Map.Entry<String, SBMLDocument> entry : tertDocMap.entrySet()) {
            String tertDocName = entry.getKey();
            SBMLDocument tertDoc = entry.getValue();

            List<String> secModelNames = new ArrayList<>();
            List<SBMLDocument> secModels = new ArrayList<>();
            CompSBMLDocumentPlugin plugin = (CompSBMLDocumentPlugin) tertDoc.getPlugin(CompConstants.shortLabel);

            // Gets secondary models
            for (ExternalModelDefinition emd : plugin.getListOfExternalModelDefinitions()) {
                String secModelName = emd.getSource();
                secModelNames.add(secModelName);

                SBMLDocument secModel = secDocMap.get(secModelName);
                secModels.add(secModel);
            }

            models.add(new ManualTertiaryModel(tertDocName, tertDoc, secModelNames, secModels));
        }

        return models;
    }

    /**
     * Reads manual tertiary models from a file. Faulty models are skipped.
     *
     * @param path
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    public static List<ManualTertiaryModel> read(Path path) throws CombineArchiveException {

        URI modelUri = CombineArchiveUtil.getModelURI(path);

        Map<String, SBMLDocument> tertDocMap = new HashMap<>();
        Map<String, SBMLDocument> secDocMap = new HashMap<>();

        try (CombineArchive ca = new CombineArchive(path.toFile())) {

            MetaDataObject mdo = ca.getDescriptions().get(0);
            Element metaParent = mdo.getXmlDescription();
            PMFMetadataNode metadataAnnotation = new PMFMetadataNode(metaParent);
            Set<String> masterFiles = metadataAnnotation.masterFiles;

            // Classify models into tertiary or secondary models
            for (ArchiveEntry entry : ca.getEntriesWithFormat(modelUri)) {
                String docName = entry.getFileName();
                try {
                    SBMLDocument doc = CombineArchiveUtil.readModel(entry.getPath());
                    if (masterFiles.contains(docName)) {
                        tertDocMap.put(docName, doc);
                    } else {
                        secDocMap.put(docName, doc);
                    }
                } catch (IOException | XMLStreamException e) {
                    LOGGER.warning(docName + " could not be read");
                }
            }
        } catch (IOException | JDOMException | ParseException error) {
            throw new CombineArchiveException(path.getFileName() + " could not be opened");
        }

        List<ManualTertiaryModel> models = new ArrayList<>();
        for (Map.Entry<String, SBMLDocument> entry : tertDocMap.entrySet()) {
            String tertDocName = entry.getKey();
            SBMLDocument tertDoc = entry.getValue();

            List<String> secModelNames = new ArrayList<>();
            List<SBMLDocument> secModels = new ArrayList<>();
            CompSBMLDocumentPlugin plugin = (CompSBMLDocumentPlugin) tertDoc.getPlugin(CompConstants.shortLabel);

            // Gets secondary models
            for (ExternalModelDefinition emd : plugin.getListOfExternalModelDefinitions()) {
                String secModelName = emd.getSource();
                secModelNames.add(secModelName);

                SBMLDocument secModel = secDocMap.get(secModelName);
                secModels.add(secModel);
            }

            models.add(new ManualTertiaryModel(tertDocName, tertDoc, secModelNames, secModels));
        }

        return models;
    }

    /**
     * Writes manual tertiary model to a PMF or PMFX file. Faulty models are skipped. Existent files
     * are overwritten.
     *
     * @param file
     * @param modelUri
     * @param models
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     * @deprecated use {@link ManualTertiaryModelFile#write(Path, List)} instead
     */
    @Deprecated
    private static void write(File file, URI modelUri, List<ManualTertiaryModel> models)
        throws CombineArchiveException {
        // Remove if existent file
        if (file.exists()) {
            file.delete();
        }

        // Creates COMBINE archive
        try (CombineArchive ca = new CombineArchive(file)) {

            Set<String> masterFiles = new HashSet<>(models.size());

            // Adds models and data
            for (ManualTertiaryModel model : models) {
                try {
                    ArchiveEntry masterEntry = CombineArchiveUtil.writeModel(ca, model.getTertiaryDoc(), model
                            .getTertiaryDocName(), modelUri);
                    masterFiles.add(masterEntry.getPath().getFileName().toString());
                } catch (IOException | SBMLException | XMLStreamException e) {
                    LOGGER.warning(model.getTertiaryDocName() + " could not be saved");
                    continue;
                }

                for (int i = 0; i < model.getSecDocs().size(); i++) {
                    SBMLDocument secDoc = model.getSecDocs().get(i);
                    String secDocName = model.getSecDocNames().get(i);

                    try {
                        CombineArchiveUtil.writeModel(ca, secDoc, secDocName, modelUri);
                    } catch (IOException | SBMLException | XMLStreamException e) {
                        LOGGER.warning(secDocName + " could not be saved");
                    }
                }
            }

            Element annot = new PMFMetadataNode(ModelType.MANUAL_TERTIARY_MODEL, masterFiles).node;
            ca.addDescription(new DefaultMetaDataObject(annot));

            ca.pack();

        } catch (Exception e) {
            file.delete();  // Removes faulty file
            e.printStackTrace();
            throw new CombineArchiveException(file.getName() + " could not be opened");
        }
    }

    /**
     * Writes manual tertiary model to a file. Faulty models are skipped. Existent files
     * are overwritten.
     *
     * @param path
     * @param models
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    public static void write(Path path, List<ManualTertiaryModel> models) throws CombineArchiveException, IOException {
        URI modelUri = CombineArchiveUtil.getModelURI(path);

        // Remove if existent file
        Files.deleteIfExists(path);

        // Creates COMBINE archive
        try (CombineArchive ca = new CombineArchive(path.toFile())) {

            Set<String> masterFiles = new HashSet<>(models.size());

            // Adds models and data
            for (ManualTertiaryModel model : models) {
                try {
                    ArchiveEntry masterEntry = CombineArchiveUtil.writeModel(ca, model.getTertiaryDoc(), model
                            .getTertiaryDocName(), modelUri);
                    masterFiles.add(masterEntry.getPath().getFileName().toString());
                } catch (IOException | SBMLException | XMLStreamException e) {
                    LOGGER.warning(model.getTertiaryDocName() + " could not be saved");
                    continue;
                }

                for (int i = 0; i < model.getSecDocs().size(); i++) {
                    SBMLDocument secDoc = model.getSecDocs().get(i);
                    String secDocName = model.getSecDocNames().get(i);

                    try {
                        CombineArchiveUtil.writeModel(ca, secDoc, secDocName, modelUri);
                    } catch (IOException | SBMLException | XMLStreamException e) {
                        LOGGER.warning(secDocName + " could not be saved");
                    }
                }
            }

            Element annot = new PMFMetadataNode(ModelType.MANUAL_TERTIARY_MODEL, masterFiles).node;
            ca.addDescription(new DefaultMetaDataObject(annot));

            ca.pack();

        } catch (Exception e) {
            Files.delete(path);  // Removes faulty file
            e.printStackTrace();
            throw new CombineArchiveException(path.getFileName() + " could not be opened");
        }
    }
}
