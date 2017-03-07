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
import de.bund.bfr.pmfml.model.ManualSecondaryModel;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Case 2c: Manual secondary models. Secondary models generated manually.
 *
 * @author Miguel Alba
 */
public class ManualSecondaryModelFile {

    private static final Logger LOGGER = Logger.getLogger("ManualSecondaryModelFile");

    private static final URI SBML_URI = UriFactory.createSBMLURI();
    private static final URI PMF_URI = UriFactory.createPMFURI();

    /**
     * @deprecated use {@link ManualSecondaryModelFile#read(Path)} instead
     */
    @Deprecated
    public static List<ManualSecondaryModel> readPMF(final File file) throws CombineArchiveException {
        return read(file, SBML_URI);
    }

    /**
     * @deprecated use {@link ManualSecondaryModelFile#read(Path)} instead
     */
    @Deprecated
    public static List<ManualSecondaryModel> readPMFX(final File file) throws CombineArchiveException {
        return read(file, PMF_URI);
    }

    /**
     * @deprecated use {@link ManualSecondaryModelFile#write(Path, List)} instead
     */
    @Deprecated
    public static void writePMF(final String dir, final String filename,
                                final List<ManualSecondaryModel> models) throws CombineArchiveException {
        // Creates CombineArchive name
        String caName = dir + "/" + filename + ".pmf";
        write(new File(caName), SBML_URI, models);
    }

    /**
     * @deprecated use {@link ManualSecondaryModelFile#write(Path, List)} instead
     */
    @Deprecated
    public static void writePMFX(final String dir, final String filename,
                                 final List<ManualSecondaryModel> models) throws CombineArchiveException {

        // Creates CombineArchive name
        String caName = dir + "/" + filename + ".pmfx";
        write(new File(caName), PMF_URI, models);
    }

    /**
     * Reads manual secondary models from a PMF or PMFX file. Faulty models are skipped.
     *
     * @param file
     * @param modelUri
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     * @deprecated use {@Link ManualSecondaryModelFile#read(Path)} instead
     */
    @Deprecated
    private static List<ManualSecondaryModel> read(File file, URI modelUri) throws CombineArchiveException {
        try (CombineArchive ca = new CombineArchive(file)) {

            List<ManualSecondaryModel> models = new ArrayList<>();

            for (ArchiveEntry entry : ca.getEntriesWithFormat(modelUri)) {
                String docName = entry.getFileName();
                try {
                    SBMLDocument doc = CombineArchiveUtil.readModel(entry.getPath());
                    models.add(new ManualSecondaryModel(docName, doc));
                } catch (IOException | XMLStreamException e) {
                    LOGGER.warning(docName + " could not be retrieved");
                    e.printStackTrace();
                }
            }

            return models;

        } catch (IOException | JDOMException | ParseException e) {
            throw new CombineArchiveException(file.getName() + " could not be opened");
        }
    }

    /**
     * Reads manual secondary models from a PMF or PMFX file. Faulty models are skipped.
     *
     * @param path
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    public static List<ManualSecondaryModel> read(Path path) throws CombineArchiveException {
        URI modelUri = CombineArchiveUtil.getModelURI(path);

        try (CombineArchive ca = new CombineArchive(path.toFile())) {

            List<ManualSecondaryModel> models = new ArrayList<>();

            for (ArchiveEntry entry : ca.getEntriesWithFormat(modelUri)) {
                String docName = entry.getFileName();
                try {
                    SBMLDocument doc = CombineArchiveUtil.readModel(entry.getPath());
                    models.add(new ManualSecondaryModel(docName, doc));
                } catch (IOException | XMLStreamException e) {
                    LOGGER.warning(docName + " could not be retrieved");
                    e.printStackTrace();
                }
            }

            return models;

        } catch (IOException | JDOMException | ParseException e) {
            throw new CombineArchiveException(path.getFileName() + " could not be opened");
        }
    }

    /**
     * Writes manual secondary models to a PMF or PMFX file. Faulty models are skipped. Existent files
     * with the same filename are overwritten.
     *
     * @param file
     * @param models
     * @throws CombineArchiveException if the CombineArchive cannot be opened or closed properly
     * @deprecated use {@link ManualSecondaryModelFile#write(Path, List)} instead
     */
    @Deprecated
    private static void write(File file, URI modelUri, List<ManualSecondaryModel> models)
        throws CombineArchiveException {

        // Remove if existent file
        if (file.exists()) {
            file.delete();
        }

        // Creates COMBINE archive
        try (CombineArchive ca = new CombineArchive(file)) {

            // Adds models
            for (ManualSecondaryModel model : models) {
                try {
                    CombineArchiveUtil.writeModel(ca, model.getDoc(), model.getDocName(), modelUri);
                } catch (SBMLException | XMLStreamException | IOException e) {
                    LOGGER.warning(model.getDocName() + " could not be saved");
                    e.printStackTrace();
                }
            }

            // Adds description with model type
            Element annot = new PMFMetadataNode(ModelType.MANUAL_SECONDARY_MODEL, Collections.emptySet()).node;
            ca.addDescription(new DefaultMetaDataObject(annot));

            ca.pack();
        } catch (Exception e) {
            file.delete();  // Removes faulty file
            e.printStackTrace();
            throw new CombineArchiveException(file.getName() + " could not be opened");
        }
    }

    /**
     * Writes manual secondary models to a file. Faulty models are skipped. Existent files
     * with the same filename are overwritten.
     *
     * @param path
     * @param models
     * @throws CombineArchiveException if the CombineArchive cannot be opened or closed properly
     */
    public static void write(Path path, List<ManualSecondaryModel> models)
            throws CombineArchiveException, IOException {

        URI modelUri = CombineArchiveUtil.getModelURI(path);

        // Remove if existent file
        Files.deleteIfExists(path);

        // Creates COMBINE archive
        try (CombineArchive ca = new CombineArchive(path.toFile())) {

            // Adds models
            for (ManualSecondaryModel model : models) {
                try {
                    CombineArchiveUtil.writeModel(ca, model.getDoc(), model.getDocName(), modelUri);
                } catch (SBMLException | XMLStreamException | IOException e) {
                    LOGGER.warning(model.getDocName() + " could not be saved");
                    e.printStackTrace();
                }
            }

            // Adds description with model type
            Element annot = new PMFMetadataNode(ModelType.MANUAL_SECONDARY_MODEL, Collections.emptySet()).node;
            ca.addDescription(new DefaultMetaDataObject(annot));

            ca.pack();
        } catch (Exception e) {
            Files.delete(path);  // Removes faulty file
            e.printStackTrace();
            throw new CombineArchiveException(path.getFileName() + " could not be opened");
        }
    }
}
