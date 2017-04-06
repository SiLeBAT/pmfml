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
package de.bund.bfr.pmfml.model;

import de.bund.bfr.pmfml.numl.AtomicDescription;
import de.bund.bfr.pmfml.sbml.ModelVariable;
import de.bund.bfr.pmfml.sbml.PMFUnitDefinition;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Unit;

import de.bund.bfr.pmfml.ModelType;
import de.bund.bfr.pmfml.numl.AtomicValue;
import de.bund.bfr.pmfml.numl.ConcentrationOntology;
import de.bund.bfr.pmfml.numl.NuMLDocument;
import de.bund.bfr.pmfml.numl.ResultComponent;
import de.bund.bfr.pmfml.numl.TimeOntology;
import de.bund.bfr.pmfml.numl.Tuple;
import de.bund.bfr.pmfml.numl.TupleDescription;
import de.bund.bfr.pmfml.sbml.PMFCompartment;
import de.bund.bfr.pmfml.sbml.PMFSpecies;
import de.bund.bfr.pmfml.sbml.PMFUnit;
import de.bund.bfr.pmfml.sbml.Reference;
import de.bund.bfr.pmfml.sbml.ReferenceType;
import de.bund.bfr.pmfml.sbml.SBMLFactory;

/**
 * @author Miguel Alba
 */
public class ModelTestUtil {

    private ModelTestUtil() {
    }

    static SBMLDocument createDummyModel() {
        return new SBMLDocument();
    }

    static NuMLDocument createDummyData() {
        return new NuMLDocument(createConcentrationOntology(), createTimeOntology(),
                createResultComponent());
    }

    private static ConcentrationOntology createConcentrationOntology() {
        PMFUnit[] concUnits = new PMFUnit[]{new PMFUnit(1.0, 0, Unit.Kind.ITEM, 1.0),
                new PMFUnit(1.0, 0, Unit.Kind.GRAM, 1.0)};
        PMFUnitDefinition concUnitDef =
                new PMFUnitDefinition("log10_count_g", "log10(count/g)", "log10", concUnits);

        String compartmentId = "culture_broth_broth_culture_culture_medium";
        String compartmentName = "culture broth, broth culture, culture medium";
        String compartmentDetail = "broth";
        ModelVariable[] modelVariables =
                new ModelVariable[]{new ModelVariable("Temperature", 10.0), new ModelVariable("pH", 5.63)};
        PMFCompartment compartment = SBMLFactory.createPMFCompartment(compartmentId,
                compartmentName, null, compartmentDetail, modelVariables);

        String speciesId = "species4024";
        String speciesName = "salmonella spp";
        String speciesDetail = "Salmonella spec";
        PMFSpecies species = SBMLFactory.createPMFSpecies(compartmentId, speciesId, speciesName,
                concUnitDef.getId(), null, speciesDetail, null);

        return new ConcentrationOntology(concUnitDef, compartment, species);
    }

    private static TimeOntology createTimeOntology() {
        PMFUnit[] hourUnits = new PMFUnit[]{new PMFUnit(3600, 0, Unit.Kind.SECOND, 1)};
        PMFUnitDefinition timeUnitDef = new PMFUnitDefinition("h", "h", null, hourUnits);

        return new TimeOntology(timeUnitDef);
    }

    private static ResultComponent createResultComponent() {
        String id = "exp1";
        int condId = 1;
        String combaseId = "salm25";
        String creatorGivenName = "Jane Doe";
        String creatorFamilyName = "Doe";
        String creatorContact = "jane.doe@people.com";
        ModelType modelType = ModelType.EXPERIMENTAL_DATA;
        String rights = "CC";

        // Create references
        String author = "Baranyi, J.";
        int year = 1994;
        String title = "A dynamic approach to predicting microbial bacterial growth in food";
        String abstractText =
                "A new member of the family of groth models described by Baranyi ...";
        String journal = "International Journal of Food Microbiology";
        String volume = "23";
        String issue = "3";
        int page = 277;
        String website = "http://www.sciencedirect.com/science/article/pii/0168160594901570";
        ReferenceType referenceType = ReferenceType.Paper;

        Reference ref = SBMLFactory.createReference(author, year, title, abstractText, journal,
                volume, issue, page, null, website, referenceType, null);
        Reference[] references = new Reference[]{ref};

        AtomicDescription concDesc = new AtomicDescription("concentration", "concentration");
        AtomicDescription timeDesc = new AtomicDescription("Time", "time");
        TupleDescription tupleDescription = new TupleDescription(concDesc, timeDesc);

        Tuple[] tuples = new Tuple[]{new Tuple(new AtomicValue(2.67), new AtomicValue(0.00)),
                new Tuple(new AtomicValue(2.91), new AtomicValue(50.88)),
                new Tuple(new AtomicValue(2.87), new AtomicValue(73.02)),
                new Tuple(new AtomicValue(3.69), new AtomicValue(103.96)),
                new Tuple(new AtomicValue(4.25), new AtomicValue(145.01))};

        return new ResultComponent(id, condId, combaseId, creatorGivenName, creatorFamilyName,
                creatorContact, null, null, modelType, rights, null, references,
                tupleDescription, tuples);
    }
}
