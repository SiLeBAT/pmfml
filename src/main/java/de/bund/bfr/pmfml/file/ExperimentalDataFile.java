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
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Case 0: Experimental data file.
 *
 * @author Miguel Alba
 */
public class ExperimentalDataFile {

    private static final URI numlURI = UriFactory.createNuMLURI();

    private ExperimentalDataFile() {
    }

    public static List<ExperimentalData> readPMF(final File file)
            throws CombineArchiveException {
        return read(file);
    }

    public static List<ExperimentalData> readPMFX(final File file)
            throws CombineArchiveException {
        return read(file);
    }

    public static void writePMF(final String dir, final String filename,
                                List<ExperimentalData> dataRecords) throws CombineArchiveException {
        String caName = dir + "/" + filename + ".pmf";
        write(new File(caName), dataRecords);
    }

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
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    private static List<ExperimentalData> read(final File file) throws CombineArchiveException {

        CombineArchive combineArchive;
        try {
            combineArchive = new CombineArchive(file);
        } catch (IOException | JDOMException | ParseException error) {
            throw new CombineArchiveException(file.getName() + " could not be opened");
        }

        final List<ExperimentalData> dataRecords = new LinkedList<>();

        for (final ArchiveEntry entry : combineArchive.getEntriesWithFormat(numlURI)) {
            final String docName = entry.getFileName();
            try {
                final NuMLDocument doc = CombineArchiveUtil.readData(entry.getPath());
                dataRecords.add(new ExperimentalData(docName, doc));
            } catch (IOException | ParserConfigurationException | SAXException e) {
                System.err.println(docName + " could not be retrieved");
                e.printStackTrace();
            }
        }

        CombineArchiveUtil.close(combineArchive);

        return dataRecords;
    }

    /**
     * Writes experimental data files to a PMF or PMFX file. Faulty data files are skipped Existent
     * files with the same filename are overwritten.
     *
     * @param file
     * @param dataRecords
     * @throws CombineArchiveException if the CombineArchive cannot be opened or closed properly
     */
    private static void write(final File file, final List<ExperimentalData> dataRecords)
            throws CombineArchiveException {

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

        // Add data records
        for (final ExperimentalData ed : dataRecords) {
            try {
                CombineArchiveUtil.writeData(combineArchive, ed.getDoc(), ed.getDocName());
            } catch (IOException | TransformerFactoryConfigurationError | TransformerException
                    | ParserConfigurationException e) {
                System.err.println(ed.getDocName() + " could not be saved");
                e.printStackTrace();
            }
        }

        final ModelType modelType = ModelType.EXPERIMENTAL_DATA;
        final Element metadataAnnotation = new PMFMetadataNode(modelType, new HashSet<>(0)).node;
        combineArchive.addDescription(new DefaultMetaDataObject(metadataAnnotation));

        // Packs and closes the combineArchive
        CombineArchiveUtil.pack(combineArchive);
        CombineArchiveUtil.close(combineArchive);
    }
}
