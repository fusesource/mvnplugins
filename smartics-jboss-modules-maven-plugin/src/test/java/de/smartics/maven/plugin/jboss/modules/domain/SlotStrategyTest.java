package de.smartics.maven.plugin.jboss.modules.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link de.smartics.maven.plugin.jboss.modules.domain.SlotStrategy} with the default MAIN slot strategy.
 */
@RunWith(Parameterized.class)
public class SlotStrategyTest extends AbstractSlotStrategyTest {
  
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"1.2.3", SlotStrategy.MAIN, "main"},
                {"1.2.3", SlotStrategy.VERSION_MAJOR, "1"},
                {"4.5", SlotStrategy.VERSION_MAJOR, "4"},
                {"4.5", SlotStrategy.VERSION_MAJOR_MINOR, "4.5"},
                {"4.5.3", SlotStrategy.VERSION_MAJOR_MINOR, "4.5"},
                {"4", SlotStrategy.VERSION_MAJOR_MINOR, "4.0"},
                {"4.5", SlotStrategy.VERSION_FULL, "4.5.0"},
                {"4.5", SlotStrategy.MAIN, "main"}
        });
    }

    /**
     * For each test create an artifact and use {@link de.smartics.maven.plugin.jboss.modules.domain.SlotStrategy}
     * to calculate the version and test it against the expected version.
     *
     * @param artifactVersion the version of the {@link org.eclipse.aether.artifact.Artifact} to create.
     * @param slotStrategy the {@link de.smartics.maven.plugin.jboss.modules.domain.SlotStrategy} to test.
     * @param expectedVersion the expected version.
     */
    public SlotStrategyTest(final String artifactVersion, final SlotStrategy slotStrategy,
                            final String expectedVersion) {
      super(artifactVersion, slotStrategy, expectedVersion);
    }

    @Test
    public void testVersionForMainDefaultSlotStrategy() {
        assertEquals("strategy version incorrect", expectedVersion,
                slotStrategy.calculateSlot(version, SlotStrategy.MAIN_SLOT));
    }

    @Test
    public void testVersionForMainDefaultSlotStrategyFromString() {
        assertEquals("strategy version incorrect", expectedVersion,
                SlotStrategy.fromString(slotStrategy.toString()).calculateSlot(version, SlotStrategy.MAIN_SLOT));
    }

}
