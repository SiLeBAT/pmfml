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
import de.bund.bfr.pmfml.model.ExperimentalData;
import de.bund.bfr.pmfml.numl.NuMLDocument;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Case 0: Experimental data file.
 *
 * @author Miguel Alba
 */
public class ExperimentalDataFile {

    private static Logger LOGGER = Logger.getLogger("ExperimentalDataFile");

    private ExperimentalDataFile() {
    }

    /**
     * @deprecated use {@link ExperimentalDataFile#read(File)} instead
     */
    public static List<ExperimentalData> readPMF(final File file) throws CombineArchiveException {
        return read(file);
    }

    /**
     * @deprecated use {@link ExperimentalDataFile#read(File)} instead
     */
    public static List<ExperimentalData> readPMFX(final File file) throws CombineArchiveException {
        return read(file);
    }

    /**
     * @deprecated use {@link ExperimentalDataFile#write(File, List)} instead
     */
    public static void writePMF(final String dir, final String filename,
                                List<ExperimentalData> dataRecords) throws CombineArchiveException {
        String caName = dir + "/" + filename + ".pmf";
        write(new File(caName), dataRecords);
    }

    /**
     * @deprecated use {@link ExperimentalDataFile#write(File, List)} instead
     */
    public static void writePMFX(final String dir, final String filename,
                                 List<ExperimentalData> dataRecords) throws CombineArchiveException {
        String caName = dir + "/" + filename + ".pmfx";
        write(new File(caName), dataRecords);
    }

    /**
     * Reads experimental data files from a PMF or PMFX file. Faulty data files are skipped.
     *
     * @param file
     * @return List of experimental data files
     * @throws CombineArchiveException error with the COMBINE archive
     * @deprecated use {@link ExperimentalDataFile#read(Path)} instead
     */
    @Deprecated
    private static List<ExperimentalData> read(final File file) throws CombineArchiveException {

        try (CombineArchive ca = new CombineArchive(file)) {
            List<ExperimentalData> dataRecords = new ArrayList<>();

            for (ArchiveEntry entry : ca.getEntriesWithFormat(URIS.numl)) {
                String docName = entry.getFileName();

                try {
                    NuMLDocument doc = CombineArchiveUtil.readData(entry.getPath());
                    dataRecords.add(new ExperimentalData(docName, doc));
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    LOGGER.warning(docName + " could not be retrieved");
                    e.printStackTrace();
                }
            }

            return dataRecords;
        } catch (IOException | ParseException | JDOMException | CombineArchiveException e) {
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }
    }

    /**
     * Writes experimental data files to a PMF or PMFX file. Faulty data files are skipped Existent
     * files with the same filename are overwritten.
     *
     * @param file
     * @param dataRecords
     * @throws CombineArchiveException errors with COMBINE archive
     * @deprecated use {@link ExperimentalDataFile#write(Path, List)} instead
     */
    @Deprecated
    private static void write(File file, List<ExperimentalData> dataRecords) throws CombineArchiveException {

        // Remove if existent file
        if (file.exists()) {
            file.delete();
        }

        try (CombineArchive ca = new CombineArchive(file)) {

            // Add data records
            for (ExperimentalData ed : dataRecords) {
                try {
                    CombineArchiveUtil.writeData(ca, ed.getDoc(), ed.getDocName());
                } catch (TransformerException | ParserConfigurationException e) {
                    LOGGER.warning(ed.getDocName() + " could not be saved");
                    e.printStackTrace();
                }
            }

            Element metadataAnnotation = new PMFMetadataNode(ModelType.EXPERIMENTAL_DATA, Collections.emptySet()).node;
            ca.addDescription(new DefaultMetaDataObject(metadataAnnotation));

            CombineArchiveUtil.addReadme(ca);

            ca.pack();
        } catch (Exception e) {
            file.delete();  // Removes faulty file
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }
    }

    /**
     * Reads experimental data files from a file. Faulty data files are skipped.
     *
     * @param path
     * @return List of experimental data files
     * @throws CombineArchiveException error with the COMBINE archive
     */
    public static List<ExperimentalData> read(final Path path) throws CombineArchiveException {

        try (CombineArchive ca = new CombineArchive(path.toFile())) {
            List<ExperimentalData> dataRecords = new ArrayList<>();

            for (ArchiveEntry entry : ca.getEntriesWithFormat(URIS.numl)) {
                String docName = entry.getFileName();

                try {
                    NuMLDocument doc = CombineArchiveUtil.readData(entry.getPath());
                    dataRecords.add(new ExperimentalData(docName, doc));
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    LOGGER.warning(docName + " could not be retrieved");
                    e.printStackTrace();
                }
            }

            return dataRecords;
        } catch (IOException | ParseException | JDOMException | CombineArchiveException e) {
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }
    }

    /**
     * Writes experimental data files to a PMF or PMFX file. Faulty data files are skipped Existent
     * files with the same filename are overwritten.
     *
     * @param path
     * @param dataRecords
     * @throws CombineArchiveException errors with COMBINE archive
     */
    public static void write(Path path, List<ExperimentalData> dataRecords) throws CombineArchiveException,
            IOException {

        // Remove if existent file
        Files.deleteIfExists(path);

        try (CombineArchive ca = new CombineArchive(path.toFile())) {

            // Add data records
            for (ExperimentalData ed : dataRecords) {
                try {
                    CombineArchiveUtil.writeData(ca, ed.getDoc(), ed.getDocName());
                } catch (TransformerException | ParserConfigurationException e) {
                    LOGGER.warning(ed.getDocName() + " could not be saved");
                    e.printStackTrace();
                }
            }

            Element metadataAnnotation = new PMFMetadataNode(ModelType.EXPERIMENTAL_DATA, Collections.emptySet()).node;
            ca.addDescription(new DefaultMetaDataObject(metadataAnnotation));

            CombineArchiveUtil.addReadme(ca);

            ca.pack();
        } catch (Exception e) {
            Files.delete(path);  // Removes faulty file
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }
    }
}
