/***************************************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors: Department Biological Safety - BfR
 **************************************************************************************************/
package de.bund.bfr.pmfml.file;

import de.bund.bfr.pmfml.numl.NuMLDocument;
import de.bund.bfr.pmfml.numl.NuMLReader;
import de.bund.bfr.pmfml.numl.NuMLWriter;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CombineArchiveUtil {

    private static final SBMLReader READER = new SBMLReader();
    private static final SBMLWriter WRITER = new SBMLWriter();

    private CombineArchiveUtil() {
    }

    /**
     * Packs safely a CombineArchive
     *
     * @param combineArchive
     * @throws CombineArchiveException if the CombineArchive could not be packed properly
     * @deprecated Use try-with instead
     */
    public static void pack(final CombineArchive combineArchive) throws CombineArchiveException {
        try {
            combineArchive.pack();
        } catch (IOException | TransformerException e) {
            throw new CombineArchiveException(combineArchive.getEntityPath() + " could not be packed");
        }
    }

    /**
     * Closes safely a CombineArchive
     *
     * @param combineArchive
     * @throws CombineArchiveException if the CombineArchive could not be closed properly
     * @deprecated Use try-with instead when opening {@link CombineArchive}
     */
    public static void close(final CombineArchive combineArchive) throws CombineArchiveException {
        try {
            combineArchive.close();
        } catch (IOException e) {
            throw new CombineArchiveException(combineArchive.getEntityPath() + " could not be closed");
        }
    }

    /**
     * Removes previous CombineArchive if it exists
     */
    public static void removeExistentFile(final String filename) {
        final File tmpFile = new File(filename);
        if (tmpFile.exists()) {
            tmpFile.delete();
        }
    }

    // New functions --- old ones should be deleted
    static NuMLDocument readData(Path path) throws IOException, ParserConfigurationException, SAXException {
        try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
            return NuMLReader.read(is);
        }
    }

    static ArchiveEntry writeData(CombineArchive archive, NuMLDocument doc, String docName)
            throws IOException, TransformerFactoryConfigurationError, TransformerException, ParserConfigurationException {

        File tmpFile = File.createTempFile("tmp", ".numl");
        tmpFile.deleteOnExit();
        NuMLWriter.write(doc, tmpFile);
        return archive.addEntry(tmpFile, docName, URIS.numl);
    }

    static SBMLDocument readModel(Path path) throws IOException, XMLStreamException {
        try (InputStream stream = Files.newInputStream(path, StandardOpenOption.READ)) {
            return READER.readSBMLFromStream(stream);
        }
    }

    static ArchiveEntry writeModel(CombineArchive archive, SBMLDocument doc, String docName, URI modelUri)
            throws IOException, SBMLException, XMLStreamException {
        // Creates temporary file for the model
        final File tmpFile = File.createTempFile("tmp", ".sbml");
        tmpFile.deleteOnExit();
        // Writes model to tmpFile and adds it to the file
        WRITER.write(doc, tmpFile);
        return archive.addEntry(tmpFile, docName, modelUri);
    }

    static URI getModelURI(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        if (filename.endsWith(".pmf"))
            return URIS.sbml;
        if (filename.endsWith(".pmfx") || filename.endsWith(".fskx"))
            return URIS.pmf;
        throw new IllegalArgumentException("Not supported file: " + path);
    }
}
