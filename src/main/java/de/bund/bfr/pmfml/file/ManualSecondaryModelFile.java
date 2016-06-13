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

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.jdom2.Element;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;

import de.bund.bfr.pmfml.ModelType;
import de.bund.bfr.pmfml.file.uri.UriFactory;
import de.bund.bfr.pmfml.model.ManualSecondaryModel;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;

/**
 * Case 2c: Manual secondary models. Secondary models generated manually.
 * 
 * @author Miguel Alba
 */
public class ManualSecondaryModelFile {

  private static final URI SBML_URI = UriFactory.createSBMLURI();
  private static final URI PMF_URI = UriFactory.createPMFURI();

  public static List<ManualSecondaryModel> readPMF(final String filename)
      throws CombineArchiveException {
    return read(filename, SBML_URI);
  }

  public static List<ManualSecondaryModel> readPMFX(final String filename)
      throws CombineArchiveException {
    return read(filename, PMF_URI);
  }

  public static void writePMF(final String dir, final String filename,
      final List<ManualSecondaryModel> models) throws CombineArchiveException {

    // Creates CombineArchive name
    final String caName = String.format("%s/%s.pmf", dir, filename);
    write(caName, SBML_URI, models);
  }

  public static void writePMFX(final String dir, final String filename,
      final List<ManualSecondaryModel> models) throws CombineArchiveException {

    // Creates CombineArchive name
    final String caName = String.format("%s/%s.pmfx", dir, filename);
    write(caName, PMF_URI, models);
  }

  /**
   * Reads manual secondary models from a PMF or PMFX file. Faulty models are skipped.
   *
   * @param filename
   * @param modelURI
   * @throws CombineArchiveException if the CombineArchive could not be opened or closed properly
   */
  private static List<ManualSecondaryModel> read(final String filename, final URI modelURI)
      throws CombineArchiveException {
    final CombineArchive combineArchive = CombineArchiveUtil.open(filename);

    final List<ManualSecondaryModel> models = new LinkedList<>();

    for (final ArchiveEntry entry : combineArchive.getEntriesWithFormat(modelURI)) {
      final String docName = entry.getFileName();
      try {
        final SBMLDocument doc = CombineArchiveUtil.readModel(entry.getPath());

        models.add(new ManualSecondaryModel(docName, doc));
      } catch (IOException | XMLStreamException e) {
        System.err.println(docName + " could not be retrieved");
        e.printStackTrace();
      }
    }

    CombineArchiveUtil.close(combineArchive);

    return models;
  }

  /**
   * Writes manual secondary models to a PMF or PMFX file. Faulty models are skipped. Existent files
   * with the same filename are overwritten.
   *
   * @param filename
   * @param models
   * @throws CombineArchiveException if the CombineArchive cannot be opened or closed properly
   */
  private static void write(final String filename, final URI modelURI,
      final List<ManualSecondaryModel> models) throws CombineArchiveException {

    CombineArchiveUtil.removeExistentFile(filename);

    // Creates new CombineArchive
    final CombineArchive combineArchive = CombineArchiveUtil.open(filename);

    // Adds models
    for (final ManualSecondaryModel model : models) {
      try {
        CombineArchiveUtil.writeModel(combineArchive, model.getDoc(), model.getDocName(), modelURI);
      } catch (SBMLException | XMLStreamException | IOException e) {
        System.err.println(model.getDocName() + " could not be saved");
        e.printStackTrace();
      }
    }

    // Adds description with model type
    final ModelType modelType = ModelType.MANUAL_SECONDARY_MODEL;
    final Element metadataAnnotation = new PMFMetadataNode(modelType, new HashSet<String>(0)).node;
    combineArchive.addDescription(new DefaultMetaDataObject(metadataAnnotation));

    CombineArchiveUtil.pack(combineArchive);
    CombineArchiveUtil.close(combineArchive);
  }
}
