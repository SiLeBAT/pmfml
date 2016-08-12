package de.bund.bfr.pmfml.sbml;

import junit.framework.TestCase;

/**
 * Created by de on 12.08.2016.
 */
public class LimitsConstraintTest extends TestCase {

    // test constraint with 2 conditions: min & max
    public void testFullConstraint() {
        LimitsConstraint lc = new LimitsConstraint("x", 0.0, 10.0);
        Limits limits = lc.getLimits();
        assertEquals("x", limits.getVar());
        assertEquals(0.0, limits.getMin());
        assertEquals(10.0, limits.getMax());

        // test with constraint
        LimitsConstraint lcCopy = new LimitsConstraint(lc.getConstraint());
        Limits limitsCopy = lcCopy.getLimits();
        assertEquals("x", limitsCopy.getVar());
        assertEquals(0.0, limitsCopy.getMin());
        assertEquals(10.0, limitsCopy.getMax());
    }

    // test constraint with 1 condition: min
    public void testConstraintWithMin() {
        LimitsConstraint lc = new LimitsConstraint("x", 0.0, null);
        Limits limits = lc.getLimits();
        assertEquals("x", limits.getVar());
        assertEquals(0.0, limits.getMin());
        assertNull(limits.getMax());

        // test with constraint
        LimitsConstraint lcCopy = new LimitsConstraint(lc.getConstraint());
        Limits limitsCopy = lcCopy.getLimits();
        assertEquals("x", limitsCopy.getVar());
        assertEquals(0.0, limitsCopy.getMin());
        assertNull(limitsCopy.getMax());
    }

    // test constraint with 1 condition: max
    public void testConstraintWithMax() {
        LimitsConstraint lc = new LimitsConstraint("x", null, 10.0);
        Limits limits = lc.getLimits();
        assertEquals("x", limits.getVar());
        assertNull(limits.getMin());
        assertEquals(10.0, limits.getMax());

        // test with constraint
        LimitsConstraint lcCopy = new LimitsConstraint(lc.getConstraint());
        Limits limitsCopy = lcCopy.getLimits();
        assertEquals("x", limitsCopy.getVar());
        assertNull(limitsCopy.getMin());
        assertEquals(10.0, limitsCopy.getMax());
    }
}
