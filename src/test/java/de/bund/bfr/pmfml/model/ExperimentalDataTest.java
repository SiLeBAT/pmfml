package de.bund.bfr.pmfml.model;

import de.bund.bfr.pmfml.numl.NuMLDocument;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExperimentalDataTest {

    @Test
    public void test() {
        NuMLDocument doc = ModelTestUtil.createDummyData();
        ExperimentalData ed = new ExperimentalData("test.numl", doc);

        assertEquals("test.numl", ed.getDocName());
        assertEquals(doc.getConcentrationOntologyTerm(), ed.getDoc().getConcentrationOntologyTerm());
        assertEquals(doc.getTimeOntologyTerm(), ed.getDoc().getTimeOntologyTerm());
        assertEquals(doc.getResultComponent(), ed.getDoc().getResultComponent());
    }
}
