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
import de.bund.bfr.pmfml.file.uri.UriFactory;
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
import java.text.ParseException;
import java.util.*;

/**
 * Case 2b: One step secondary model file. Secondary models generated "implicitly" during 1-step
 * fitting of tertiary models.
 *
 * @author Miguel Alba
 */
public class OneStepSecondaryModelFile {

    private static final URI SBML_URI = UriFactory.createSBMLURI();
    private static final URI PMF_URI = UriFactory.createPMFURI();
    private static final URI NuML_URI = UriFactory.createNuMLURI();

    public static List<OneStepSecondaryModel> readPMF(final File file) throws Exception {
        return read(file, SBML_URI);
    }

    public static List<OneStepSecondaryModel> readPMFX(final File file) throws Exception {
        return read(file, PMF_URI);
    }

    /**
     */
    public static void writePMF(final String dir, final String filename,
                                final List<OneStepSecondaryModel> models) throws Exception {

        // Creates CombineArchive name
        String caName = dir + "/" + filename + ".pmf";
        write(new File(caName), SBML_URI, models);
    }

    public static void writePMFX(final String dir, final String filename,
                                 final List<OneStepSecondaryModel> models) throws Exception {

        // Creates CombineArchive name
        String caName = dir + "/" + filename + ".pmfx";
        write(new File(caName), PMF_URI, models);
    }

    /**
     * Reads {@link OneStepSecondaryModel}(s) from a PMF or PMFX file. Faulty models are skipped.
     *
     * @param file
     * @param modelURI
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    private static List<OneStepSecondaryModel> read(final File file, final URI modelURI)
            throws CombineArchiveException {

        // Creates CombineArchive
        CombineArchive combineArchive;
        try {
            combineArchive = new CombineArchive(file);
        } catch (IOException | JDOMException | ParseException error) {
            throw new CombineArchiveException(file.getName() + " could not be opened");
        }

        final List<OneStepSecondaryModel> models = new LinkedList<>();

        // Gets data entries
        final List<ArchiveEntry> dataEntriesList = combineArchive.getEntriesWithFormat(NuML_URI);
        final Map<String, NuMLDocument> dataEntriesMap = new HashMap<>(dataEntriesList.size());
        for (final ArchiveEntry entry : dataEntriesList) {
            try {
                final NuMLDocument doc = CombineArchiveUtil.readData(entry.getPath());
                dataEntriesMap.put(entry.getFileName(), doc);
            } catch (IOException | ParserConfigurationException | SAXException e) {
                System.err.println(entry.getFileName() + " could not be read");
                e.printStackTrace();
            }
        }

        for (final ArchiveEntry entry : combineArchive.getEntriesWithFormat(modelURI)) {
            final String modelName = entry.getFileName();
            final SBMLDocument modelDoc;
            try {
                modelDoc = CombineArchiveUtil.readModel(entry.getPath());
            } catch (IOException | SBMLException | XMLStreamException e) {
                System.err.println(entry.getFileName() + " could not be read");
                e.printStackTrace();
                continue;
            }

            // Looks for DataSourceNode
            final CompSBMLDocumentPlugin secCompPlugin =
                    (CompSBMLDocumentPlugin) modelDoc.getPlugin(CompConstants.shortLabel);
            final ModelDefinition md = secCompPlugin.getModelDefinition(0);
            final XMLNode m2Annot = md.getAnnotation().getNonRDFannotation();
            final XMLNode metadata = m2Annot.getChildElement("metadata", "");

            final List<String> numlDocNames = new LinkedList<>();
            final List<NuMLDocument> numlDocs = new LinkedList<>();

            for (final XMLNode node : metadata.getChildElements("dataSource", "")) {
                final DataSourceNode dsn = new DataSourceNode(node);
                final String dataFileName = dsn.getFile();

                numlDocNames.add(dataFileName);
                numlDocs.add(dataEntriesMap.get(dataFileName));
            }
            models.add(new OneStepSecondaryModel(modelName, modelDoc, numlDocNames, numlDocs));
        }

        CombineArchiveUtil.close(combineArchive);

        return models;
    }

    /**
     * Writes one step secondary models to a PMF or PMFX file. Faulty models are skipped. Existent
     * files are overwritten.
     *
     * @param file
     * @param modelURI
     * @param models
     * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
     */
    private static void write(final File file, final URI modelURI,
                              final List<OneStepSecondaryModel> models) throws CombineArchiveException {

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

        // Adds models and data
        for (final OneStepSecondaryModel model : models) {
            try {
                for (int i = 0; i < model.getDataDocs().size(); i++) {
                    final String dataDocName = model.getDataDocNames().get(i);
                    final NuMLDocument dataDoc = model.getDataDocs().get(i);
                    CombineArchiveUtil.writeData(combineArchive, dataDoc, dataDocName);
                }

                CombineArchiveUtil.writeModel(combineArchive, model.getModelDoc(), model.getModelDocName(),
                        modelURI);
            } catch (IOException | TransformerFactoryConfigurationError | TransformerException
                    | ParserConfigurationException | SBMLException | XMLStreamException e) {
                System.err.println(model.getModelDocName() + " could not be read");
                e.printStackTrace();
            }
        }

        // Adds description with model type
        final ModelType modelType = ModelType.ONE_STEP_SECONDARY_MODEL;
        final Element metadataAnnotation = new PMFMetadataNode(modelType, new HashSet<>(0)).node;
        combineArchive.addDescription(new DefaultMetaDataObject(metadataAnnotation));

        CombineArchiveUtil.pack(combineArchive);
        CombineArchiveUtil.close(combineArchive);
    }
}
