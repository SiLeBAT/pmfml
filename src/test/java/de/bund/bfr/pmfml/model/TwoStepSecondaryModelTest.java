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

import de.bund.bfr.pmfml.numl.NuMLDocument;
import org.junit.Test;
import org.sbml.jsbml.SBMLDocument;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Miguel Alba
 */
public class TwoStepSecondaryModelTest {

    @Test
    public void test() {
        SBMLDocument secModel = ModelTestUtil.createDummyModel();
        SBMLDocument primModelDoc = ModelTestUtil.createDummyModel();
        NuMLDocument primModelData = ModelTestUtil.createDummyData();
        PrimaryModelWData primModel =
                new PrimaryModelWData("primModel.sbml", primModelDoc, "primModelData.numl", primModelData);
        TwoStepSecondaryModel model = new TwoStepSecondaryModel("secModel.sbml", secModel,
                Collections.singletonList(primModel));

        assertEquals("secModel.sbml", model.getSecDocName());
        assertEquals(secModel, model.getSecDoc());
        assertEquals(Collections.singletonList(primModel), model.getPrimModels());
    }
}
