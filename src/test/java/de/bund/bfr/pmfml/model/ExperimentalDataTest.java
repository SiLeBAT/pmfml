package de.bund.bfr.pmfml.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.bund.bfr.pmfml.numl.NuMLDocument;

public class ExperimentalDataTest {

  private NuMLDocument doc;

  @Test
  public void test() {
    doc = ModelTestUtil.createDummyData();
    final ExperimentalData ed = new ExperimentalData("test.numl", doc);
    
    assertEquals("test.numl", ed.getDocName());
    assertEquals(doc.getConcentrationOntologyTerm(), ed.getDoc().getConcentrationOntologyTerm());
    assertEquals(doc.getTimeOntologyTerm(), ed.getDoc().getTimeOntologyTerm());
    assertEquals(doc.getResultComponent(), ed.getDoc().getResultComponent());
  }
}
