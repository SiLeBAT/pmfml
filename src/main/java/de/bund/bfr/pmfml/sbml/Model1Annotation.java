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

import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.xml.XMLNode;
import org.sbml.jsbml.xml.XMLTriple;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Primary model annotation. Holds model id, model title, uncertainties, references, and condId.
 *
 * @author Miguel de Alba
 */
public class Model1Annotation {

    private static final String METADATA_NS = "pmf";
    private static final String METADATA_TAG = "metadata";

    Uncertainties uncertainties;
    List<Reference> refs;
    String condID;
    Annotation annotation;

    /**
     * Gets fields from existing primary model annotation.
     */
    public Model1Annotation(final Annotation annotation) {

        this.annotation = annotation;

        final XMLNode metadataNode = annotation.getNonRDFannotation().getChildElement(METADATA_TAG, "");

        // Gets condID
        condID = new CondIdNode(metadataNode.getChildElement(CondIdNode.TAG, "")).getCondId();

        // Gets model quality annotation
        final XMLNode modelQualityNode = metadataNode.getChildElement(UncertaintyNode.TAG, "");
        if (modelQualityNode != null) {
            uncertainties = new UncertaintyNode(modelQualityNode).getMeasures();
        }

        // Gets references
        refs = metadataNode.getChildElements(ReferenceSBMLNode.TAG, "").stream().
                map(refNode -> new ReferenceSBMLNode(refNode).toReference()).collect(Collectors.toList());
    }

    public Model1Annotation(final Uncertainties uncertainties, final List<Reference> references,
                            final String condID) {

        // Builds metadata node
        final XMLNode metadataNode = new XMLNode(new XMLTriple(METADATA_TAG, "", METADATA_NS));

        // Builds uncertainties node
        metadataNode.addChild(new UncertaintyNode(uncertainties).getNode());

        // Builds reference nodes
        for (final Reference reference : references) {
            metadataNode.addChild(new ReferenceSBMLNode(reference).node);
        }

        // Builds condID node
        metadataNode.addChild(new CondIdNode(condID).node);

        // Saves fields
        this.uncertainties = uncertainties;
        this.refs = references;
        this.condID = condID;

        this.annotation = new Annotation();
        this.annotation.setNonRDFAnnotation(metadataNode);
    }

    public Uncertainties getUncertainties() {
        return uncertainties;
    }

    public List<Reference> getReferences() {
        return refs;
    }

    public String getCondID() {
        return condID;
    }

    public Annotation getAnnotation() {
        return annotation;
    }
}
