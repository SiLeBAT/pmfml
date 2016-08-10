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

import org.sbml.jsbml.Parameter;

/**
 * Coefficient that extends the SBML {@link Parameter} with more data: P, error, correlations and a
 * description tag.
 * 
 * @author Miguel Alba
 */
public interface PMFCoefficient {

  /** Returns the {@link Parameter} of this {@link PMFCoefficient}. */
  Parameter getParameter();

  /** Returns the id of this {@link PMFCoefficient}. */
  String getId();

  /** Returns the value of this {@link PMFCoefficient}. */
  double getValue();

  /** Returns the unit of this {@link PMFCoefficient}. */
  String getUnit();

  /** Returns the P of this {@link PMFCoefficient}. If not set set returns null. */
  Double getP();

  /** Returns the error of this {@link PMFCoefficient}. If not set returns null. */
  Double getError();

  /** Returns the T of this {@link PMFCoefficient}. If not set returns null. */
  Double getT();

  /** Returns the {@link Correlation}s of this {@link PMFCoefficient}. If not set returns null. */
  Correlation[] getCorrelations();

  /** Returns the description of this {@link PMFCoefficient}. If not set return null. */
  String getDescription();

  Boolean getIsStart();
  
  /**
   * Sets the id value of this {@link PMFCoefficient} with 'id'. If 'id' is null or empty it will do
   * nothing.
   */
  void setId(final String id);

  /** Sets the value of this {@link PMFCoefficient} with 'value'. */
  void setValue(final double value);

  /**
   * Sets the unit of this {@link PMFCoefficient} with 'unit'. If 'unit' is null or empty it will do
   * nothing.
   */
  void setUnit(final String unit);

  /** Sets the P value of this {@link PMFCoefficient} with 'p'. */
  void setP(final double p);

  /** Sets the T value of this {@link PMFCoefficient} with 'T'. */
  void setT(final double t);

  /** Sets the error value of this {@link PMFCoefficient} with 'error'. */
  void setError(final double error);

  /**
   * Sets the {@link Correlation}s of this {@link PMFCoefficient} with 'correlations'. If
   * 'correlations is null it will do nothing.
   */
  void setCorrelations(Correlation[] correlations);

  /**
   * Sets the description of this {@link PMFCoefficient} with 'description'. If 'description' is
   * null or empty it will do nothing.
   */
  void setDescription(final String description);

  void setIsStart(final boolean isStart);

  /** Returns true if the P of this {@link PMFCoefficient} is set. */
  boolean isSetP();

  /** Returns true if the error of this {@link PMFCoefficient} is set. */
  boolean isSetError();

  /** Returns true if the T of this {@link PMFCoefficient} is set. */
  boolean isSetT();

  /** Returns true if the {@link Correlation}s of this {@link PMFCoefficient} are set. */
  boolean isSetCorrelations();

  /** Returns true if the description of this {@link PMFCoefficient} is set. */
  boolean isSetDescription();

  boolean isSetIsStart();
}
