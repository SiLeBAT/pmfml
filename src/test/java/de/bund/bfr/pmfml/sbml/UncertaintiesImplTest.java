/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.pmfml.sbml;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Miguel Alba
 */
public class UncertaintiesImplTest {

	@Test
	public void testIdAccesors() {
		// Constructor should ignore null values for id
		UncertaintiesImpl uncert = new UncertaintiesImpl(null, null, null, null, null, null, null, null, null);
		assertFalse(uncert.isSetID());
		assertNull(uncert.getID());

		uncert.setID(101);
		assertTrue(uncert.isSetID());
		assertTrue(101 == uncert.getID());
	}

	@Test
	public void testModelNameAccesors() {
		// Constructor should ignore null values for model name
		UncertaintiesImpl uncert = new UncertaintiesImpl(null, null, null, null, null, null, null, null, null);
		assertFalse(uncert.isSetModelName());
		assertNull(uncert.getModelName());
		
		// Constructor should ignore empty strings for model name
		uncert = new UncertaintiesImpl(null, "", null, null, null, null, null, null, null);
		assertFalse(uncert.isSetModelName());
		assertNull(uncert.getModelName());

		// setModelName should ignore null strings
		uncert.setModelName(null);
		assertFalse(uncert.isSetModelName());
		assertNull(uncert.getModelName());

		// setModelName should ignore empty strings
		uncert.setModelName("");
		assertFalse(uncert.isSetModelName());
		assertNull(uncert.getModelName());

		// setModelName should accept non-empty strings
		uncert.setModelName("BacillusCereus_CultureMedium");
		assertTrue(uncert.isSetModelName());
		assertEquals("BacillusCereus_CultureMedium", uncert.getModelName());
	}

	@Test
	public void testCommentAccesors() {
		// Constructor should ignore null values for comment
		UncertaintiesImpl uncert = new UncertaintiesImpl(null, null, null, null, null, null, null, null, null);
		assertFalse(uncert.isSetComment());
		assertNull(uncert.getComment());

		// Constructor should ignore empty strings for comment
		uncert = new UncertaintiesImpl(null, null, "", null, null, null, null, null, null);
		assertFalse(uncert.isSetComment());
		assertNull(uncert.getComment());

		// setComment should ignore null strings
		uncert.setComment(null);
		assertFalse(uncert.isSetComment());
		assertNull(uncert.getComment());

		// setComment should ignore empty strings
		uncert.setComment("");
		assertFalse(uncert.isSetComment());
		assertNull(uncert.getComment());

		// setComment should accept non-empty strings
		uncert.setComment("uncertainties");
		assertTrue(uncert.isSetComment());
		assertEquals("uncertainties", uncert.getComment());
	}

	@Test
	public void testR2Accesors() {
		// Constructor should ignore null values for r2
		UncertaintiesImpl uncert = new UncertaintiesImpl(null, null, null, null, null, null, null, null, null);
		assertFalse(uncert.isSetR2());
		assertNull(uncert.getR2());

		uncert.setR2(0.996);
		assertTrue(uncert.isSetR2());
		assertTrue(Double.compare(0.996, uncert.getR2()) == 0);
	}

	@Test
	public void testRMSAccesors() {
		// Constructor should ignore null values for rms
		UncertaintiesImpl uncert = new UncertaintiesImpl(null, null, null, null, null, null, null, null, null);
		assertFalse(uncert.isSetRMS());
		assertNull(uncert.getRMS());

		uncert.setRMS(0.345);
		assertTrue(uncert.isSetRMS());
		assertTrue(Double.compare(0.345, uncert.getRMS()) == 0);
	}

	@Test
	public void testSSEaccesors() {
		UncertaintiesImpl uncert = new UncertaintiesImpl(null, null, null, null, null, null, null, null, null);
		assertFalse(uncert.isSetSSE());
		assertNull(uncert.getSSE());

		uncert.setSSE(1.909);
		assertTrue(uncert.isSetSSE());
		assertTrue(Double.compare(1.909, uncert.getSSE()) == 0);
	}

	@Test
	public void testAICaccesors() {
		UncertaintiesImpl uncert = new UncertaintiesImpl(null, null, null, null, null, null, null, null, null);
		assertFalse(uncert.isSetAIC());
		assertNull(uncert.getAIC());

		uncert.setAIC(-32.997);
		assertTrue(uncert.isSetAIC());
		assertTrue(Double.compare(uncert.getAIC(), -32.997) == 0);
	}

	@Test
	public void testBICaccesors() {
		UncertaintiesImpl uncert = new UncertaintiesImpl(null, null, null, null, null, null, null, null, null);
		assertFalse(uncert.isSetBIC());
		assertNull(uncert.getBIC());

		uncert.setBIC(34.994);
		assertTrue(uncert.isSetBIC());
		assertTrue(Double.compare(uncert.getBIC(), 34.994) == 0);
	}

	@Test
	public void testDOFaccesors() {
		UncertaintiesImpl uncert = new UncertaintiesImpl(null, null, null, null, null, null, null, null, null);
		assertFalse(uncert.isSetDOF());
		assertNull(uncert.getDOF());

		uncert.setDOF(16);
		assertTrue(uncert.isSetDOF());
		assertTrue(uncert.getDOF() == 16);
	}
}
