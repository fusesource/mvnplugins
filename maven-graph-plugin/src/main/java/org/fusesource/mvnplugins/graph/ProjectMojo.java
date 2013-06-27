/**
 * Copyright (C) 2009 Progress Software, Inc.
 * http://fusesource.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.mvnplugins.graph;

import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.apache.maven.shared.dependency.tree.DependencyNode;

import java.util.ArrayList;
import java.io.File;

/**
 * Generates a graph image of the dependencies of the project using the graphviz
 * tool 'dot'.  You must have the 'dot' executable installed and in your path
 * before using this goal.
 * <p/>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 * @goal project
 * @requiresDependencyResolution compile|test|runtime
 */
public class ProjectMojo extends AbstractMojo {

    /**
     * @required
     * @readonly
     * @parameter expression="${project}"
     * @since 1.0
     */
    protected MavenProject project;

    /**
     * @required
     * @readonly
     * @parameter expression="${localRepository}"
     * @since 1.0
     */
    protected ArtifactRepository localRepository;

    /**
     * @required
     * @component
     * @since 1.0
     */
    protected ArtifactResolver artifactResolver;

    /**
     * @required
     * @readonly
     * @component
     * @since 1.0
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @required
     * @readonly
     * @component
     * @since 1.0
     */
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * @required
     * @readonly
     * @component
     */
    protected ArtifactCollector artifactCollector;

    /**
     * @required
     * @readonly
     * @component
     * @since 1.0
     */
    protected DependencyTreeBuilder treeBuilder;

    /**
     * The file the diagram will be written to.  Must use a file extension that the dot command supports or just the
     * '.dot' extension.
     * <br/>
     * @parameter default-value="${project.build.directory}/project-graph.png" expression="${graph.target}"
     */
    protected File target;

    /**
     * If set to true, omitted dependencies will not be drawn.  Dependencies are marked
     * as omitted if it would result in a resolution conflict.
     * <br/>
     * @parameter default-value="true" expression="${hide-omitted}"
     */
    protected boolean hideOmitted;

    /**
     * If set to true optional dependencies are not drawn.
     * <br/>
     * @parameter default-value="false" expression="${hide-optional}"
     */
    protected boolean hideOptional;

    /**
     * If set to true if dependencies external to the reactor project should be hidden.
     * <br/>
     * @parameter default-value="false" expression="${hide-external}"
     */
    protected boolean hideExternal;

    /**
     * If set to true pom dependencies are not drawn.
     * <br/>
     * @parameter default-value="true" expression="${hide-poms}"
     */
    protected boolean hidePoms;

    /**
     * A comma separated list of scopes.  Dependencies which
     * match the specified scopes will not be drawn.
     * <br/>
     * For example: <code>runtime,test</code>
     * <br/>
     * @parameter expression="${hide-scope}"
     */
    protected String hideScopes;

    /**
     * If set to true then dependencies not explicitly defined in the projects
     * pom will not be drawn.
     * <br/>
     * @parameter default-value="false" expression="${hide-transitive}"
     */
    protected boolean hideTransitive;

    /**
     * If set to true then the version label will not be drawn.
     * <br/>
     * @parameter default-value="false" expression="${hide-version}"
     */
    protected boolean hideVersion;

    /**
     * If set to true then the group id label will not be drawn.
     * <br/>
     * @parameter default-value="false" expression="${hide-group-id}"
     */
    protected boolean hideGroupId;
    
    /**
     * A comma separated list of group Ids to include. If this parameter
     * is specified, any group Id not matching will  be excluded.
     * A '*' can be appended <b>only</b> at the end to match subgroups<br/>
     * For example: <code>com.apache,java.net.*</code>
     * @parameter expression="${include-group-ids}"
     */
    protected String includeGroupIds;
    
    /**
     * A comma separated list of group Ids to exclude. A '*' can be appended
     * <b>only</b> at the end to match subgroups<br/>
     * For example: <code>com.apache,java.net.*</code>
     * @parameter expression="${exclude-group-ids}"
     */
    protected String excludeGroupIds;

    /**
     * If set to true then the module type label will not be drawn.
     * <br/>
     * @parameter default-value="false" expression="${hide-type}"
     */
    protected boolean hideType;

    /**
     * If set to true then the intermediate dot file will not be deleted.
     * <br/>
     * @parameter default-value="false" expression="${keep-dot}"
     */
    protected boolean keepDot;

    /**
     * The label for the graph.
     * <br/>
     * @parameter default-value="Dependency Graph for ${project.name}" expression="${graph.label}"
     */
    protected String label;

    /**
     * If true then the 'test scope' and 'optional' attributes are cascaded
     * down to the dependencies of the original node. 
     *
     * <br/>
     * @parameter default-value="true" expression="${graph.cascade}"
     */
    protected boolean cascade;

    /**
     * The direction that the graph will be laid out in.
     * it can be one of the following values:
     * <br/>
     * <code>TB LR BT RL <code>
     * <br/>
     * top to bottom, from left to right, from bottom to top, and from right to left, respectively
     *
     * <br/>
     * @parameter default-value="TB" expression="${graph.direction}"
     */
    protected String direction;

    public void execute() throws MojoExecutionException {
        try {
            DependencyVisualizer visualizer = new DependencyVisualizer();
            visualizer.cascade = cascade;
            visualizer.direction = direction;
            visualizer.hideOptional = hideOptional;
            visualizer.hidePoms = hidePoms;
            visualizer.hideOmitted = hideOmitted;
            visualizer.hideExternal = hideExternal;
            visualizer.hideVersion = hideVersion;
            visualizer.hideGroupId = hideGroupId;
            visualizer.hideType = hideType;
            visualizer.keepDot = keepDot;
            visualizer.label = label;
            visualizer.hideTransitive = hideTransitive;
            visualizer.log = getLog();

            if (hideScopes != null) {
                for (String scope : hideScopes.split(",")) {
                    visualizer.hideScopes.add(scope.trim());
                }
            }
            
            if (excludeGroupIds != null) {
               for (String groupId : excludeGroupIds.split(",")) {
                  visualizer.excludeGroupIds.add(groupId.trim());
               }
            }
            
            if (includeGroupIds != null) {
               for (String groupId : includeGroupIds.split(",")) {
                  visualizer.includeGroupIds.add(groupId.trim());
               }
            }

            ArrayList<MavenProject> projects = new ArrayList<MavenProject>();
            collectProjects(projects);

            for (MavenProject p : projects) {
                ArtifactFilter filter = null;
                DependencyNode node = treeBuilder.buildDependencyTree(p, localRepository, artifactFactory, artifactMetadataSource, filter, artifactCollector);
                visualizer.add(node);
            }

            getTarget().getParentFile().mkdirs();
            visualizer.export(getTarget());
            getLog().info("Dependency graph exported to: " + getTarget());
        } catch (DependencyTreeBuilderException e) {
            throw new MojoExecutionException("Could not build the depedency tree.", e);
        }
    }

    protected void collectProjects(ArrayList<MavenProject> projects) {
        projects.add(project);
    }

    public File getTarget() {
        return target;
    }

}