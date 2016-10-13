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
package de.bund.bfr.pmfml.numl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;

/**
 * @author Miguel Alba
 */
public class AtomicDescription {

  static final String ELEMENT_NAME = "atomicDescription";

  private static final String NAME = "name";
  private static final String ONTOLOGY_TERM = "ontologyTerm";
  private static final String VALUE_TYPE = "valueType";

  private String name;
  private String ontologyTermId;
  public static final DataType valueType = DataType.DOUBLE;

  public AtomicDescription(final String name, final String ontologyTermId) {
    this.name = name;
    this.ontologyTermId = ontologyTermId;

  }

  public AtomicDescription(final Element node) {
    name = node.getAttribute(NAME);
    ontologyTermId = node.getAttribute(ONTOLOGY_TERM);
  }

  public String getName() {
    return name;
  }

  public String getOntologyTermId() {
    return ontologyTermId;
  }

  @Override
  public String toString() {
      return "AtomicDescription [name=" + name + ", ontologyTerm=" + ontologyTermId + ", valueType=" +
              valueType + "]";
  }

  @Override
  public boolean equals(final Object obj) {

    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;

    AtomicDescription other = (AtomicDescription) obj;
    return Objects.equals(name, other.name) && Objects.equals(ontologyTermId, other.ontologyTermId);
  }

  public Element toNode(final Document doc) {
    final Element node = doc.createElement(ELEMENT_NAME);
    node.setAttribute(NAME, name);
    node.setAttribute(ONTOLOGY_TERM, ontologyTermId);
    node.setAttribute(VALUE_TYPE, valueType.name());
    return node;
  }
}
