package org.ethereum.net.client;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CapabilityTest {

    @Test
    public void testEquals_OK() {
        // An object must be equal to itself
        Capability capability = new Capability("task.1", (byte) 0);
        assertEquals(capability, capability);

        // Same name and version must make Capabilities to be equal (Including name being null)
        assertEquals(new Capability("task.1", (byte) 0), new Capability("task.1", (byte) 0));
        assertEquals(new Capability(null, (byte) 0), new Capability(null, (byte) 0));

        // Capability must be different from null
        assertNotEquals(new Capability("task.1", (byte) 0), null);

        // Different combinations of name and version that must make object to be different
        assertNotEquals(new Capability("task.1", (byte) 0), new Capability("task.2", (byte) 0));
        assertNotEquals(new Capability("task.1", (byte) 0), new Capability("task.1", (byte) 1));
        assertNotEquals(new Capability("task.1", (byte) 0), new Capability("task.2", (byte) 1));
    }

    @Test
    public void testHashcode_OK() {
        // Same name and version must make hashcode to be equal
        assertEquals(new Capability("task.1", (byte) 0).hashCode(), new Capability("task.1", (byte) 0).hashCode());
        assertEquals(new Capability(null, (byte) 0).hashCode(), new Capability(null, (byte) 0).hashCode());

        // Different combinations of name and version that must make hashcode to be different
        assertNotEquals(new Capability("task.1", (byte) 0).hashCode(), new Capability("task.2", (byte) 0).hashCode());
        assertNotEquals(new Capability("task.1", (byte) 0).hashCode(), new Capability("task.1", (byte) 1).hashCode());
        assertNotEquals(new Capability("task.1", (byte) 0).hashCode(), new Capability("task.2", (byte) 1).hashCode());
    }

}
