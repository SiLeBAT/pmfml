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
package de.bund.bfr.pmfml.sbml;

import org.sbml.jsbml.Species;

/**
 * Represents the species XML element of a PMF-SBML file. It has the properties:
 * <ul>
 * <li>Compartment (mandatory)</li>
 * <li>Id (mandatory)</li>
 * <li>Name (mandatory)</li>
 * <li>Units (mandatory)</li>
 * <li>Combase code (optional)</li>
 * <li>Detail (optional)</li>
 * <li>Description</li>
 * </ul>
 * 
 * @author Miguel Alba
 */
public interface PMFSpecies {

  boolean BOUNDARY_CONDITION = true;
  boolean CONSTANT = false;
  boolean ONLY_SUBSTANCE_UNITS = false;

  /** Returns the {@link Species} of this {@link PMFSpecies}. */
  Species getSpecies();

  /** Returns the compartment of this {@link PMFSpecies}. */
  String getCompartment();

  /** Returns the id of this {@link PMFSpecies}. */
  String getId();

  /** Returns the name of this {@link PMFSpecies}. */
  String getName();

  /** Returns the units of this {@link PMFSpecies}. */
  String getUnits();

  /**
   * Returns the combase code of this {@link PMFSpecies}. If not set, returns null.
   */
  String getCombaseCode();

  /**
   * Returns the detail of this {@link PMFSpecies}. If not set, returns null.
   */
  String getDetail();

  /**
   * Returns the description of this {@link PMFSpecies}. If not set, returns null.
   */
  String getDescription();

  /**
   * Sets the compartment value of this {@link PMFSpecies} with 'compartment'. If 'compartment' is
   * null or empty it will do nothing.
   */
  void setCompartment(final String compartment);

  /**
   * Sets the id value of this {@link PMFSpecies} with 'id'. If 'id' is null or empty it will do
   * nothing.
   */
  void setId(final String id);

  /**
   * Sets the name value of this {@link PMFSpecies} with 'name'. If 'name' is null or empty it will
   * do nothing.
   */
  void setName(final String name);

  /**
   * Sets the units value of this {@link PMFSpecies} with 'units'. It 'units' is null or empty it
   * will do nothing.
   */
  void setUnits(final String units);

  /**
   * Sets the combase code value of this {@link PMFSpecies} with 'combaseCode'. It 'combaseCode' is
   * null or empty it will do nothing.
   */
  void setCombaseCode(final String combaseCode);

  /**
   * Sets the detail of this {@link PMFSpecies} with 'detail'. If 'detail' is null or empty it will
   * do nothing.
   */
  void setDetail(final String detail);

  /**
   * Sets the description of this {@link PMFSpecies} with 'description'. If 'description' is null or
   * empty it will do nothing.
   */
  void setDescription(final String description);

  /** Returns true if the combase code of this {@link PMFSpecies} is set. */
  boolean isSetCombaseCode();

  /** Returns true if the detail of this {@link PMFSpecies} is set. */
  boolean isSetDetail();

  /** Returns true if the description of this {@link PMFSpecies} is set. */
  boolean isSetDescription();
}
