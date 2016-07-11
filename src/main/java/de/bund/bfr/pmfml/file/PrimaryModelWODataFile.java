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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;

import de.bund.bfr.pmfml.ModelType;
import de.bund.bfr.pmfml.file.uri.UriFactory;
import de.bund.bfr.pmfml.model.PrimaryModelWOData;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;

/**
 * Case 1b: Primary models without data file.
 *
 * @author Miguel Alba
 */
public class PrimaryModelWODataFile {

    private static final URI SBML_URI = UriFactory.createNuMLURI();
    private static final URI PMF_URI = UriFactory.createPMFURI();

    public static List<PrimaryModelWOData> readPMF(final File file)
            throws CombineArchiveException {
        return read(file, SBML_URI);
    }

    public static List<PrimaryModelWOData> readPMFX(final File file)
            throws CombineArchiveException {
        return read(file, PMF_URI);
    }

    public static void writePMF(final String dir, final String filename,
                                final List<PrimaryModelWOData> models) throws Exception {
        final String caName = String.format("%s/%s.pmf", dir, filename);
        write(new File(caName), SBML_URI, models);
    }

    public static void writePMFX(final String dir, final String filename,
                                 final List<PrimaryModelWOData> models) throws Exception {
        final String caName = String.format("%s/%s.pmfx", dir, filename);
        write(new File(caName), PMF_URI, models);
    }


    /**
     * Reads primary models without data from a PMF or PMFX file. Faulty files are skipped.
     *
     * @param file
     * @param modelURI URI used for the models: {@link de.bund.bfr.pmfml.file.uri.PmfUri} or
     *                 {@link de.bund.bfr.pmfml.file.uri.SbmlUri}
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    private static List<PrimaryModelWOData> read(final File file, final URI modelURI)
            throws CombineArchiveException {

        CombineArchive combineArchive;
        try {
            combineArchive = new CombineArchive(file);
        } catch (IOException | JDOMException | ParseException error) {
            throw new CombineArchiveException(file.getName() + " could not be opened");
        }

        final List<PrimaryModelWOData> models = new LinkedList<>();

        // Parse models in the combineArchive
        final List<ArchiveEntry> modelEntries = combineArchive.getEntriesWithFormat(modelURI);
        for (final ArchiveEntry entry : modelEntries) {
            final String docName = entry.getFileName();

            try {
                final SBMLDocument doc = CombineArchiveUtil.readModel(entry.getPath());
                models.add(new PrimaryModelWOData(docName, doc));
            } catch (IOException | XMLStreamException e) {
                System.err.println(docName + " could not be retrieved");
                e.printStackTrace();
            }
        }

        CombineArchiveUtil.close(combineArchive);

        return models;
    }

    /**
     * Writes primary models without data to a PMF or PMFX file. Faulty data files are skipped.
     * Existent files with the same filename are overwritten.
     *
     * @param file
     * @param models
     * @throws CombineArchiveException if the CombineArchive cannot be opened or closed properly
     */
    private static void write(final File file, final URI modelURI,
                              final List<PrimaryModelWOData> models) throws CombineArchiveException {

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

        // Add models
        for (final PrimaryModelWOData model : models) {
            try {
                CombineArchiveUtil.writeModel(combineArchive, model.getDoc(), model.getDocName(), modelURI);
            } catch (SBMLException | XMLStreamException | IOException e) {
                System.err.println(model.getDocName() + " could not be saved");
                e.printStackTrace();
            }
        }

        // Adds description with model type
        final ModelType modelType = ModelType.PRIMARY_MODEL_WODATA;
        final Element metadataAnnotation = new PMFMetadataNode(modelType, new HashSet<String>(0)).node;
        combineArchive.addDescription(new DefaultMetaDataObject(metadataAnnotation));

        // Packs and closes the combineArchive
        CombineArchiveUtil.pack(combineArchive);
        CombineArchiveUtil.close(combineArchive);
    }
}
