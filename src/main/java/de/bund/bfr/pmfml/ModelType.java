package de.bund.bfr.pmfml;
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


/**
 * @author Miguel Alba
 */
public enum ModelType {
  EXPERIMENTAL_DATA, // Experimental data

  PRIMARY_MODEL_WDATA, // Primary models generated from data records
  PRIMARY_MODEL_WODATA, // Primary models generated without data records

  TWO_STEP_SECONDARY_MODEL, // Secondary models generated with the classical
                            // 2-step approach from primary models
  ONE_STEP_SECONDARY_MODEL, // Secondary models generated implicitely during
                            // 1-step fitting of tertiary models
  MANUAL_SECONDARY_MODEL, // Manually generated secondary model

  TWO_STEP_TERTIARY_MODEL, // Tertiary model generated with 2-step fit
                           // approach
  ONE_STEP_TERTIARY_MODEL, // Tertiary model generated with 1-step fit
                           // approach
  MANUAL_TERTIARY_MODEL // Tertiary models generated manually
}
