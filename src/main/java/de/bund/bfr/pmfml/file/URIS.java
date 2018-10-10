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
    public static final URI numl;

    /** Generic SBML URI */
    public static final URI sbml;

    public static final URI pmf;

    public static final URI txt;

    static {
        try {
            numl = new URI("https://raw.githubusercontent.com/NuML/NuML/master/NUMLSchema.xsd");
            pmf = new URI("http://sourceforge.net/projects/microbialmodelingexchange/files/");
            sbml = new URI("http://identifiers.org/combine/specifications/sbml");
            txt = new URI("http://purl.org/NET/mediatypes/text-xplain");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }
}
