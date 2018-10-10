package de.bund.bfr.pmfml.file;

import java.net.URI;

/**
 * Identifiers of a COMBINE specification.
 *
 * @author Miguel de Alba
 * @see <a href="http://co.mbine.org/standards/specifications/">http://co.mbine.
 *      org/standards/specifications/</a>
 */
class URIS {

    /**
     * COMBINE has no official support for NuML, so it has no identifier for NuML.
     * NuML's schema is used instead.
     */
    static final URI numl = URI.create("https://raw.githubusercontent.com/NuML/NuML/master/NUMLSchema.xsd");

    /** Generic SBML URI */
    static final URI sbml = URI.create("http://identifiers.org/combine/specifications/sbml");

    static final URI pmf = URI.create("http://sourceforge.net/projects/microbialmodelingexchange/files/");

    static final URI txt = URI.create("http://purl.org/NET/mediatypes/text-xplain");
}
