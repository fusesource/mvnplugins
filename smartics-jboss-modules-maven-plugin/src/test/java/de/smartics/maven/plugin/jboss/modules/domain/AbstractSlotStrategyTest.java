package de.smartics.maven.plugin.jboss.modules.domain;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * A common base class from the {@link de.smartics.maven.plugin.jboss.modules.domain.SlotStrategy} tests.
 */
abstract class AbstractSlotStrategyTest {
  // A simple artifact to use in the tests
  ArtifactVersion version;

  String expectedVersion;

  // The class under test
  SlotStrategy slotStrategy;

  /**
   * Create an {@link org.apache.maven.artifact.versioning.ArtifactVersion}, 
   * a {@link de.smartics.maven.plugin.jboss.modules.domain.SlotStrategy} and an expected version.
   *
   * @param artifactVersion {@link org.apache.maven.artifact.versioning.ArtifactVersion} to use in the tests.
   * @param slotStrategy {@link de.smartics.maven.plugin.jboss.modules.domain.SlotStrategy} to test.
   * @param expectedVersion the expected version when testing calculateSlot of SlotStrategy.
   */
  AbstractSlotStrategyTest(final String artifactVersion, final SlotStrategy slotStrategy, final String expectedVersion)
  {
    this.slotStrategy = slotStrategy;
    this.expectedVersion = expectedVersion;
    this.version = new DefaultArtifactVersion(artifactVersion);
  }

  /**
   * Test SlotStrategy.calculateSlot(..)
   */
  abstract void testVersionForMainDefaultSlotStrategy();

  /**
   * Test SlotStrategy.fromString()
   */
  abstract void testVersionForMainDefaultSlotStrategyFromString();

}