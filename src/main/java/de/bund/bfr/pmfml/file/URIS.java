package de.bund.bfr.pmfml.file;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Identifiers of a COMBINE specification.
 *
 * @author Miguel de Alba
 * @see <a href="http://co.mbine.org/standards/specifications/">http://co.mbine.
 *      org/standards/specifications/</a>
 */
public class URIS {

    /**
     * COMBINE has no official support for NuML, so it has no identifier for NuML.
     * NuML's schema is used instead.
     */
    public static final URI numl = URI.create("https://raw.githubusercontent.com/NuML/NuML/master/NUMLSchema.xsd");

    /** Generic SBML URI */
    public static final URI sbml = URI.create("http://identifiers.org/combine/specifications/sbml");

    public static final URI pmf = URI.create("http://sourceforge.net/projects/microbialmodelingexchange/files/");

    public static final URI txt = URI.create("http://purl.org/NET/mediatypes/text-xplain");
}
