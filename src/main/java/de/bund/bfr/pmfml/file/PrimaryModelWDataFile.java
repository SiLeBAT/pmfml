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
import de.bund.bfr.pmfml.file.uri.UriFactory;
import de.bund.bfr.pmfml.model.PrimaryModelWData;
import de.bund.bfr.pmfml.numl.NuMLDocument;
import de.bund.bfr.pmfml.sbml.DataSourceNode;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Miguel Alba
 */
public class PrimaryModelWDataFile {

    private static final URI SBML_URI = UriFactory.createSBMLURI();
    private static final URI NUML_URI = UriFactory.createNuMLURI();
    private static final URI PMF_URI = UriFactory.createPMFURI();

    public static List<PrimaryModelWData> readPMF(final File file) throws CombineArchiveException {
        return read(file, SBML_URI);
    }

    public static List<PrimaryModelWData> readPMFX(final File file) throws CombineArchiveException {
        return read(file, PMF_URI);
    }

    /**
     * Writes experiments to PrimaryModelWDataFile.
     */
    public static void writePMF(final String dir, final String filename,
                                final List<PrimaryModelWData> models) throws CombineArchiveException {
        String caName = dir + "/" + filename + ".pmf";
        write(new File(caName), SBML_URI, models);
    }

    /**
     * Writes experiments to PrimaryModelWDataFile.
     */
    public static void writePMFX(final String dir, final String filename,
                                 final List<PrimaryModelWData> models) throws CombineArchiveException {
        String caName = dir + "/" + filename + ".pmfx";
        write(new File(caName), PMF_URI, models);
    }

    /**
     * Reads primary models with data from a PMF or PMFX file. FAulty models are skipped.
     *
     * @param file
     * @return List of primary models with data
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    private static List<PrimaryModelWData> read(File file, URI modelURI) throws CombineArchiveException {

        try (CombineArchive ca = new CombineArchive(file)) {

            // Gets data and model entries
            List<ArchiveEntry> dataEntries = ca.getEntriesWithFormat(NUML_URI);
            List<ArchiveEntry> modelEntries = ca.getEntriesWithFormat(modelURI);

            // define models
            List<PrimaryModelWData> models = new ArrayList<>(modelEntries.size());

            // Get data map
            Map<String, ArchiveEntry> dataEntryMap = dataEntries.stream().collect(Collectors.toMap
                    (ArchiveEntry::getFileName, entry -> entry));

            // Parse models in the COMBINE archive
            for (ArchiveEntry modelEntry : modelEntries) {
                String modelDocName = modelEntry.getFileName();

                // Read model
                SBMLDocument modelDoc;
                String dataDocName;
                ArchiveEntry dataEntry;

                try {
                    modelDoc = CombineArchiveUtil.readModel(modelEntry.getPath());
                    Annotation modelAnnot = modelDoc.getModel().getAnnotation();
                    XMLNode metaDataNode = modelAnnot.getNonRDFannotation().getChildElement("metadata", "");
                    XMLNode dataSourceRawNode = metaDataNode = metaDataNode.getChildElement("dataSource", "");
                    DataSourceNode dataSourceNode = new DataSourceNode(dataSourceRawNode);

                    dataDocName = dataSourceNode.getFile();
                    dataEntry = dataEntryMap.get(dataDocName);

                } catch (XMLStreamException | IOException e) {
                    LOGGER.warn(modelDocName + ": Model could not be read. Skipping entry.");
                    continue;
                } catch (NullPointerException e) {
                    // occurs when annotation is not set
                    LOGGER.warn(modelDocName + ": Missing data. Skipping entry");
                    continue;
                }

                // Read data
                NuMLDocument dataDoc;
                try {
                    dataDoc = CombineArchiveUtil.readData(dataEntry.getPath());
                } catch (ParserConfigurationException | SAXException e) {
                    LOGGER.warn(dataDocName + ": could not be retrieved. Skipping entry.");
                    continue;
                }

                models.add(new PrimaryModelWData(modelDocName, modelDoc, dataDocName, dataDoc));
            }

            return models;
        } catch (IOException | ParseException | JDOMException e) {
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }
    }

    /**
     * Writes primary models with data to a PMF or PMFX file. Faulty models are skipped. Existent
     * files are overwritten.
     *
     * @param file
     * @param modelURI
     * @param models
     * @throws CombineArchiveException if the combineArchive could not be opened or closed properly
     */

    private static void write(File file, URI modelURI, List<PrimaryModelWData> models) throws CombineArchiveException {

        // Remove if existent file
        if (file.exists()) {
            file.delete();
        }

        // Creates new COMBINE archive
        try (CombineArchive ca = new CombineArchive(file)) {

            for (PrimaryModelWData model : models) {

                // write data
                ArchiveEntry dataEntry;
                try {
                    dataEntry = CombineArchiveUtil.writeData(ca, model.getDataDoc(), model.getDataDocName());
                } catch (IOException | TransformerFactoryConfigurationError |
                        TransformerException | ParserConfigurationException e) {
                    LOGGER.warn(model.getDataDocName() + ": could not be added. Skipping model.");
                    continue;
                }

                // write model
                try {
                    CombineArchiveUtil.writeModel(ca, model.getModelDoc(), model.getModelDocName(), modelURI);
                } catch (IOException | XMLStreamException | SBMLException e) {
                    LOGGER.warn(model.getModelDocName() + ": could not be added. Skipping model.");
                    // Removes corresponding data. Without the data it does not make sense to keep
                    // its experimental data.
                    ca.removeEntry(dataEntry);
                }
            }

            Element annotation = new PMFMetadataNode(ModelType.PRIMARY_MODEL_WDATA, Collections.emptySet())
                    .node;
            ca.addDescription(new DefaultMetaDataObject(annotation));

            ca.pack();

        } catch (IOException | JDOMException | ParseException | CombineArchiveException | TransformerException e) {
            e.printStackTrace();
            throw new CombineArchiveException(e.getMessage());
        }
    }
}
