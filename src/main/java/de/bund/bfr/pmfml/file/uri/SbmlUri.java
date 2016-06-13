/*******************************************************************************
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
 *******************************************************************************/
package de.bund.bfr.pmfml.file.uri;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Generic SBML URI
 * 
 * @author Miguel de Alba
 * @see <a href="http://co.mbine.org/specifications/sbml">http://co.mbine.org/
 *      specifications/sbml</a>
 */
public class SbmlUri implements UriBase {

  public URI createURI() {
	  
	  try {
		  return new URI("http://identifiers.org/combine/specifications/sbml");
	  } catch (URISyntaxException error) {
		  // should not occur - passed URL above is valid
		  throw new RuntimeException(error);
	  }
  }
}
