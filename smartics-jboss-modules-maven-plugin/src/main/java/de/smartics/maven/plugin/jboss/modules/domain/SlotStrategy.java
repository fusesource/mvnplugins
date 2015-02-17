/*
 * Copyright 2013-2014 smartics, Kronseder & Reiner GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.smartics.maven.plugin.jboss.modules.domain;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.eclipse.aether.artifact.Artifact;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * The naming strategy for module slots.
 */
public enum SlotStrategy
{
  // ***************************** Enumeration ******************************

  // ******************************** Fields ********************************

  // --- constants ----------------------------------------------------------

  /**
   * The module is set to the main slot (default).
   */
  MAIN("main") {
    @Override
    public String calculateSlot(Artifact artifact, String defaultSlot) {
      return StringUtils.isBlank(defaultSlot) ? MAIN_SLOT : defaultSlot;
    }
  },

  /**
   * The module is set to the major minor artifact version slot
   */
  VERSION_MAJOR_MINOR("version-major-minor") {
    @Override
    public String calculateSlot(Artifact artifact, String defaultSlot) {
      final ArtifactVersion version = calcVersion(artifact);
      final String slot = String.format("%s.%s", String.valueOf(version.getMajorVersion()),
                                                 String.valueOf(version.getMinorVersion()));
      return applySlotStrategy(defaultSlot, slot);
    }
  },

  /**
   * The module is set to the major artifact version slot.
   */
  VERSION_MAJOR("version-major") {
    @Override
    public String calculateSlot(Artifact artifact, String defaultSlot) {
      final ArtifactVersion version = calcVersion(artifact);
      return applySlotStrategy(defaultSlot, String.valueOf(version.getMajorVersion()));
    }
  },

  /**
   * The module is set to the full artifact version slot
   */
  VERSION_FULL("version-full") {
    @Override
    public String calculateSlot(Artifact artifact, String defaultSlot) {
      final ArtifactVersion version = calcVersion(artifact);
      String slot = String.format("%s.%s.%s", String.valueOf(version.getMajorVersion()),
                                              String.valueOf(version.getMinorVersion()),
                                              String.valueOf(version.getIncrementalVersion()));
      return applySlotStrategy(defaultSlot, slot);
    }
  };

  /**
   * The main slot.
   */
  public static final String MAIN_SLOT = "main";

  // --- members ------------------------------------------------------------

  /**
   * The identifier of the strategy.
   */
  private String id;

  // ***************************** Constructors *****************************

  private SlotStrategy(final String id)
  {
    this.id = id;
  }

  // ******************************** Methods *******************************

  // --- init ---------------------------------------------------------------

  // --- get&set ------------------------------------------------------------

  // --- business -----------------------------------------------------------

  /**
   * Returns the slot strategy identified by the given {@code id}.
   *
   * @param id the identifier of the requested slot strategy.
   * @return the requested slot strategy.
   * @throws IllegalArgumentException if {@code id} is not a valid slot
   *           strategy.
   */
  public static SlotStrategy fromString(final String id)
    throws IllegalArgumentException
  {
    for (final SlotStrategy strategy : values())
    {
      if (id.equals(strategy.id))
      {
        return strategy;
      }
    }

    throw new IllegalArgumentException(String.format(
        "Invalid slot strategy '%s'. Allowed values are: %s", id,
        Arrays.toString(values())));
  }

  // --- object basics ------------------------------------------------------

  @Override
  public String toString()
  {
    return id;
  }

  /**
   * Calculates the name for the slot, each type of SlotStrategy has a different implementation.
   *
   * @param artifact the artifact with additional information. If
   *          <code>null</code>: a static prefix will be assumed.
   * @param defaultSlot the name of the default slot to use.
   * @return the name of the slot.
   */
  public abstract String calculateSlot(final Artifact artifact, final String defaultSlot);

  /**
   *
   * @param defaultSlot
   * @param slot
   * @return
   */
  private static String applySlotStrategy(final String defaultSlot, String slot)
  {
    if (!(StringUtils.isBlank(defaultSlot) || MAIN_SLOT.equals(defaultSlot)))
    {
      slot = defaultSlot.concat(slot);
    }
    return slot;
  }

  /**
   * Calculates the name for the slot.
   *
   * @param defaultSlot the name of the default slot. May be blank.
   * @param moduleSlot the name of the module slot. May be blank.
   * @param artifact the artifact with additional information. If
   *          <code>null</code>: a static prefix will be assumed.
   * @return the name of the slot.
   */
  public String calcSlot(final String defaultSlot, final String moduleSlot,
      final Artifact artifact)
  {
    final String fallBackSlot = StringUtils.isBlank(moduleSlot) ? defaultSlot : moduleSlot;
    return calculateSlot(artifact, fallBackSlot);
  }

  /**
   * Calculate the version for the given {@link org.eclipse.aether.artifact.Artifact}.
   *  
   * @param artifact the {@link org.eclipse.aether.artifact.Artifact} to find 
   *                 the {@link org.apache.maven.artifact.versioning.ArtifactVersion} of.
   * @return the {@link org.apache.maven.artifact.versioning.ArtifactVersion} created from
   *         the version or if the version is null it is set to VersionX.
   */
  ArtifactVersion calcVersion(final Artifact artifact)
  {
      return new DefaultArtifactVersion(artifact == null ? "VersionX" : artifact.getVersion());
  }
}
