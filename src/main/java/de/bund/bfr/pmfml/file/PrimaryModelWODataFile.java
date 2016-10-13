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

import de.binfalse.bflog.LOGGER;
import de.bund.bfr.pmfml.ModelType;
import de.bund.bfr.pmfml.file.uri.SbmlUri;
import de.bund.bfr.pmfml.file.uri.UriFactory;
import de.bund.bfr.pmfml.model.PrimaryModelWOData;
import de.bund.bfr.pmfml.file.uri.PmfUri;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.sbml.jsbml.SBMLDocument;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Case 1b: Primary models without data file.
 *
 * @author Miguel Alba
 */
public class PrimaryModelWODataFile {

    private static final URI SBML_URI = UriFactory.createNuMLURI();
    private static final URI PMF_URI = UriFactory.createPMFURI();

    public static List<PrimaryModelWOData> readPMF(final File file) throws CombineArchiveException {
        return read(file, SBML_URI);
    }

    public static List<PrimaryModelWOData> readPMFX(final File file) throws CombineArchiveException {
        return read(file, PMF_URI);
    }

    public static void writePMF(final String dir, final String filename,
                                final List<PrimaryModelWOData> models) throws CombineArchiveException {
        String caName = dir + "/" + filename + ".pmf";
        write(new File(caName), SBML_URI, models);
    }

    public static void writePMFX(final String dir, final String filename,
                                 final List<PrimaryModelWOData> models) throws CombineArchiveException {
        String caName = dir + "/" + filename + ".pmfx";
        write(new File(caName), PMF_URI, models);
    }


    /**
     * Reads primary models without data from a PMF or PMFX file. Faulty files are skipped.
     *
     * @param file
     * @param modelURI URI used for the models: {@link PmfUri} or
     *                 {@link SbmlUri}
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */

    private static List<PrimaryModelWOData> read(File file, URI modelURI)
            throws CombineArchiveException {

        try (CombineArchive ca = new CombineArchive(file)) {

            List<PrimaryModelWOData> models = new ArrayList<>();

            for (ArchiveEntry entry : ca.getEntriesWithFormat(modelURI)) {
                String docName = entry.getFileName();

                // Read model
                try {
                    SBMLDocument doc = CombineArchiveUtil.readModel(entry.getPath());
                    models.add(new PrimaryModelWOData(docName, doc));
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                    LOGGER.warn(docName + ": could not be read. Skipping entry");
                }
            }

            return models;

        } catch (IOException | ParseException | JDOMException e) {
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }
    }

    /**
     * Writes primary models without data to a PMF or PMFX file. Faulty data files are skipped.
     * Existent files with the same filename are overwritten.
     *
     * @param file
     * @param models
     * @throws CombineArchiveException if the CombineArchive cannot be opened or closed properly
     */
    private static void write(File file, URI modelUri, List<PrimaryModelWOData> models) throws
            CombineArchiveException {

        // Remove if existent file
        if (file.exists()) {
            file.delete();
        }

        // Creates new COMBINE archive
        try (CombineArchive ca = new CombineArchive(file)) {
            for (PrimaryModelWOData model : models) {
                // Write model
                try {
                    CombineArchiveUtil.writeModel(ca, model.getDoc(), model.getDocName(), modelUri);
                } catch (IOException | TransformerFactoryConfigurationError |
                        XMLStreamException e) {
                    LOGGER.warn(model.getDocName() + ": could not be saved. Skipping model.");
                }
            }

            // Adds description with model type
            Element annot = new PMFMetadataNode(ModelType.PRIMARY_MODEL_WODATA, Collections.emptySet()).node;
            ca.addDescription(new DefaultMetaDataObject(annot));

            ca.pack();

        } catch (Exception e) {
            file.delete();  // Removes faulty file
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }
    }
}
