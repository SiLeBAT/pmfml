package de.bund.bfr.pmfml.sbml;

import junit.framework.TestCase;

/**
 * Created by de on 12.08.2016.
 */
public class Model2AnnotationTest extends TestCase {

    public void test() {
        Uncertainties uncert1 = new UncertaintiesImpl(101, "BacillusCereus_CultureMedium", "uncertainties", 0.996,
                0.345, 1.909, -32.997, -34.994, 16);
        Reference ref = new ReferenceImpl("Baranyi, J.", 1994,
                "A dynamic approach to predicting bacterial growth in food", "A new member ...",
                "International Journal of Food Microbiology", "23", "3", 277, 1,
                "http://www.sciencedirect.com/science/article/pii/0168160594901570",
                ReferenceType.Paper, "comment");

        Model2Annotation annot1 = new Model2Annotation(2, uncert1, new Reference[]{ref});
        Model2Annotation annot2 = new Model2Annotation(annot1.getAnnotation());

        // check condId
        assertTrue(2 == annot2.getGlobalModelID());

        // check uncertainties
        Uncertainties uncert2 = annot2.getUncertainties();
        assertTrue(101 == uncert2.getID());
        assertEquals("BacillusCereus_CultureMedium", uncert2.getModelName());
        assertEquals("uncertainties", uncert2.getComment());
        assertEquals(0.996, uncert2.getR2(), 0.0);
        assertEquals(0.345, uncert2.getRMS(), 0.0);
        assertEquals(1.909, uncert2.getSSE(), 0.0);
        assertEquals(-32.997, uncert2.getAIC(), 0.0);
        assertEquals(-34.994, uncert2.getBIC(), 0.0);
        assertTrue(16 == uncert2.getDOF());

        // check references
        Reference ref2 = annot2.getReferences()[0];
        assertEquals(ref2.getAuthor(), ref.getAuthor());
        assertEquals(ref2.getYear(), ref.getYear());
        assertEquals(ref2.getTitle(), ref.getTitle());
        assertEquals(ref2.getAbstractText(), ref.getAbstractText());
        assertEquals(ref2.getJournal(), ref.getJournal());
        assertEquals(ref2.getVolume(), ref.getVolume());
        assertEquals(ref2.getIssue(), ref.getIssue());
        assertEquals(ref2.getPage(), ref.getPage());
        assertEquals(ref2.getApprovalMode(), ref.getApprovalMode());
        assertEquals(ref2.getWebsite(), ref.getWebsite());
        assertEquals(ref2.getType(), ref.getType());
        assertEquals(ref2.getComment(), ref.getComment());
    }
}
