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

import org.junit.Test;
import org.sbml.jsbml.SBMLDocument;

import static org.junit.Assert.assertEquals;

public class PrimaryModelWODataTest {

    @Test
    public void test() {
        SBMLDocument doc = ModelTestUtil.createDummyModel();
        PrimaryModelWOData model = new PrimaryModelWOData("model.sbml", doc);

        assertEquals("model.sbml", model.getDocName());
        assertEquals(doc, model.getDoc());
    }
}
