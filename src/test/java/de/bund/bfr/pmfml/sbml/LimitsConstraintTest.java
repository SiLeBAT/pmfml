package de.bund.bfr.pmfml.sbml;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class LimitsConstraintTest {

    // test constraint with 2 conditions: min & max
    @Test
    public void testFullConstraint() {
        LimitsConstraint lc = new LimitsConstraint("x", 0.0, 10.0);
        Limits limits = lc.getLimits();
        assertEquals("x", limits.getVar());
        assertEquals(0.0, limits.getMin(), 0.0);
        assertEquals(10.0, limits.getMax(), 0.0);

        // test with constraint
        LimitsConstraint lcCopy = new LimitsConstraint(lc.getConstraint());
        Limits limitsCopy = lcCopy.getLimits();
        assertEquals("x", limitsCopy.getVar());
        assertEquals(0.0, limitsCopy.getMin(), 0.0);
        assertEquals(10.0, limitsCopy.getMax(), 0.0);
    }

    // test constraint with 1 condition: min
    @Test
    public void testConstraintWithMin() {
        LimitsConstraint lc = new LimitsConstraint("x", 0.0, null);
        Limits limits = lc.getLimits();
        assertEquals("x", limits.getVar());
        assertEquals(0.0, limits.getMin(), 0.0);
        assertNull(limits.getMax());

        // test with constraint
        LimitsConstraint lcCopy = new LimitsConstraint(lc.getConstraint());
        Limits limitsCopy = lcCopy.getLimits();
        assertEquals("x", limitsCopy.getVar());
        assertEquals(0.0, limitsCopy.getMin(), 0.0);
        assertNull(limitsCopy.getMax());
    }

    // test constraint with 1 condition: max
    @Test
    public void testConstraintWithMax() {
        LimitsConstraint lc = new LimitsConstraint("x", null, 10.0);
        Limits limits = lc.getLimits();
        assertEquals("x", limits.getVar());
        assertNull(limits.getMin());
        assertEquals(10.0, limits.getMax(), 0.0);

        // test with constraint
        LimitsConstraint lcCopy = new LimitsConstraint(lc.getConstraint());
        Limits limitsCopy = lcCopy.getLimits();
        assertEquals("x", limitsCopy.getVar());
        assertNull(limitsCopy.getMin());
        assertEquals(10.0, limitsCopy.getMax(), 0.0);
    }
}
