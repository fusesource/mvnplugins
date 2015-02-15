package de.smartics.maven.plugin.jboss.modules.domain;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link de.smartics.maven.plugin.jboss.modules.domain.SlotStrategy}.
 */
@RunWith(Parameterized.class)
public class SlotStrategyTest {

    private Artifact artifact = new DefaultArtifact("com.yourcompany", "awesome-artifact", "jar", "1.2.3");

    private SlotStrategy slotStrategy;

    private String expectedVersion;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"1.2.3", SlotStrategy.MAIN, "main"},
                {"1.2.3", SlotStrategy.VERSION_MAJOR, "1"},
                {"4.5", SlotStrategy.VERSION_MAJOR, "4"},
                {"4.5", SlotStrategy.VERSION_MAJOR, "4"},
                {"4.5", SlotStrategy.VERSION_FULL, "4.5.0"},
                {"4.5", SlotStrategy.MAIN, "main"}
        });
    }

    public SlotStrategyTest(String artifactVersion, SlotStrategy slotStrategy, String expectedVersion) {
        this.slotStrategy = slotStrategy;
        this.expectedVersion = expectedVersion;

        this.artifact = new DefaultArtifact("com.yourcompany", "awesome-artifact", "jar", artifactVersion);
    }

    @Test
    public void testVersionForSlotStrategy() {
        assertEquals("strategy version incorrect", expectedVersion,
                slotStrategy.calcSlot(artifact, SlotStrategy.MAIN_SLOT));
    }

}
