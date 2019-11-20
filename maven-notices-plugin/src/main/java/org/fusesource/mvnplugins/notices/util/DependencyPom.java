package org.fusesource.mvnplugins.notices.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.model.io.ModelWriter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;
import org.codehaus.plexus.util.StringUtils;

public class DependencyPom {
    
    private MavenProject project;
    private Model model;
    private String projectVersion;
    private File file;
    private ArtifactRepository localRepository;
    private List<Dependency> extraDependencies;    
    private MavenSession session;
    private List<Dependency> excludeDependencies;

    public DependencyPom(MavenProject project, ArtifactRepository localRepository, String extraDependencies, String defaultParent, String excludeDependencies) {
        this.project = project;
        this.localRepository = localRepository;
        
        this.extraDependencies = new ArrayList<Dependency>();
        if (extraDependencies != null) {
            for (String depStr : extraDependencies.split(",")) {
                String[] depStrSplit = depStr.split(":");
                Dependency dep = new Dependency();
                dep.setGroupId(depStrSplit[0]);
                dep.setArtifactId(depStrSplit[1]);
                dep.setVersion(depStrSplit[2]);
                this.extraDependencies.add(dep);
            }
        }
        
        this.excludeDependencies = new ArrayList<Dependency>();
        if (excludeDependencies != null) {
            for (String depStr : excludeDependencies.split(",")) {
                String[] depStrSplit = depStr.split(":");
                Dependency dep = new Dependency();
                dep.setGroupId(depStrSplit[0]);
                dep.setArtifactId(depStrSplit[1]);
                this.excludeDependencies.add(dep);
            }
        }
        
        model = project.getOriginalModel();
        model.setPackaging("jar");
        model.setProfiles(null);
        model.setBuild(null);
        model.setArtifactId(model.getArtifactId() + "-dependencies");       
        
        projectVersion = project.getVersion();
        model.setVersion(projectVersion);
        
        setParent(defaultParent);
        
        Build build = new Build();
        model.setBuild(build);
    }
    
    private void setParent(String defaultParent) {
        if (defaultParent != null && !defaultParent.equals("")) {
            // hack to go from maven coordinate string to Parent obj. TODO any Maven util class to do this?
            String[] strings = defaultParent.split(":");
            
            if (strings.length >= 3) {            
                model.getParent().setGroupId(strings[0]);
                model.getParent().setArtifactId(strings[1]);        
                if ("VERSION".equals(strings[2])) {
                    model.getParent().setVersion(projectVersion);            
                } else {
                    model.getParent().setVersion(strings[2]);
                }        
                if (strings.length == 4) {
                    model.getParent().setRelativePath(strings[3]);
                } else {
                    model.getParent().setRelativePath(null);
                }
            }
        }
    }

    public void addPlugin(Plugin plugin) {
        model.getBuild().addPlugin(plugin);        
    }

    public void generatePom(String repositories, String targetDir) throws IOException {
        List<Dependency> loadDependenciesFromRepos = loadDependenciesFromRepos(repositories);
        loadDependenciesFromRepos.addAll(extraDependencies);
        model.setDependencies(loadDependenciesFromRepos);

        project.getProperties().put("skipTests", "true");
        model.setProperties(project.getProperties());

        file = new File(targetDir, "dependency-pom.xml");
        if (file.exists()) {
            file.delete();
        }
        ModelWriter writer = new DefaultModelWriter();
        writer.write(file, null, model);
    }

    public File buildPom() throws MavenInvocationException, MojoExecutionException, FileNotFoundException {
        if (file != null) {
            InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile(file);
            request.setBaseDirectory(file.getParentFile());
            request.setLocalRepositoryDirectory(new File(localRepository.getBasedir()));
            request.setGoals(Collections.singletonList("package"));
            List<String> profiles = new LinkedList<String>();
            for (Profile p : project.getActiveProfiles()) {
                profiles.add(p.getId());
            }
            request.setProfiles(profiles);
            request.setShellEnvironmentInherited(true);
            if (session.getRequest().getUserSettingsFile().exists()) {
                request.setUserSettingsFile(session.getRequest().getUserSettingsFile());
            }
            if (session.getRequest().getLoggingLevel() == MavenExecutionRequest.LOGGING_LEVEL_DEBUG) {
                request.setDebug(true);
            }
            request.setOffline(session.getRequest().isOffline());
            request.setProperties(session.getUserProperties());

            PrintStream invokerLog = null;
            InvocationResult invocationResult = null; 
            try {
                invokerLog = new PrintStream(new FileOutputStream(file.getAbsoluteFile().toString() + ".log"));                            
                request.setOutputHandler(new PrintStreamHandler(invokerLog, false));            
            
                Invoker invoker = new DefaultInvoker();
                invocationResult = invoker.execute(request);    
                
                if (invocationResult.getExitCode() != 0) {
                    throw new MojoExecutionException("Error running forked build. Please check target/dependency-pom.xml.log for details. Invokation request: " + request);
                }
            } finally {
                if (invokerLog != null) {
                    invokerLog.close();    
                }
            }             
            
            return new File(project.getBasedir() + File.separator + "target" 
                    + File.separator + "target" + File.separator + model.getArtifactId() + "-" + projectVersion + ".jar");           
        }
        return null;
    }
    
    private List<Dependency> loadDependenciesFromRepos(String repositories) {
        if (repositories.contains(",")) {            
            List<Dependency> deps = new ArrayList<Dependency>();
            for (String repo : repositories.split(",")) {
                List<Dependency> loadDependenciesFromRepo = loadDependenciesFromRepo(new File(repo));
                for (Dependency dep : loadDependenciesFromRepo) {
                    if (dep != null && !dependencyExists(dep, deps)) {
                        deps.add(dep);
                    }
                }
            }
            return deps;
        } else {
            return loadDependenciesFromRepo(new File(repositories));
        }
    }

    @SuppressWarnings("unchecked")
    private List<Dependency> loadDependenciesFromRepo(File repo) {
        Iterator<File> jars = FileUtils.listFiles(repo, new String[] { "jar" }, true).iterator();
        List<Dependency> deps = new ArrayList<Dependency>();
        while (jars.hasNext()) {
            File jar = jars.next();
            if (!jar.getName().contains("tests.jar") && !jar.getName().contains("sources.jar") && !jar.getName().contains("javadoc.jar") && !jar.getName().contains("classes.jar")) {
                Dependency dependencyFromJar = dependencyFromJar(jar, repo);
                if (dependencyFromJar != null && !dependencyExists(dependencyFromJar, deps) && !dependencyExists(dependencyFromJar, excludeDependencies)) {
                    deps.add(dependencyFromJar);
                }
            }
        }
        return deps;
    }    
    
    private boolean dependencyExists(Dependency dependencyFromJar, List<Dependency> deps) {
        for (Dependency dep : deps) {
            if (dep.getGroupId().equals(dependencyFromJar.getGroupId()) && 
                    dep.getArtifactId().equals(dependencyFromJar.getArtifactId())) {
                return true;
            }
        }
        return false;
    }

    private Dependency dependencyFromJar(File jar, File repo) {
        Dependency dep = new Dependency();
        String version = jar.getParentFile().getName();
        String artifactId = jar.getParentFile().getParentFile().getName();
        String groupIdSep = StringUtils.replace(jar.getParentFile().getParentFile().getParentFile().getPath(), repo.getPath(), "");
        String groupId = groupIdSep.replace(File.separatorChar, '.');
        groupId = groupId.startsWith(".") ? groupId.substring(1) : groupId;
        groupId = groupId.endsWith(".") ? groupId.substring(0, groupId.length() - 1) : groupId;
        
        String fileName = jar.getName();
        int idxOfVersion = fileName.lastIndexOf(version);
        int idxOfLastDot = fileName.lastIndexOf(".");
        if (idxOfVersion > 0 && idxOfVersion + version.length() < idxOfLastDot && !fileName.contains("SNAPSHOT")) {
            String classifier = fileName.substring(idxOfVersion + version.length() + 1, idxOfLastDot);
            dep.setClassifier(classifier);
        }        

        dep.setArtifactId(artifactId);
        dep.setGroupId(groupId);
        dep.setVersion(version);        
        return dep;
    }

    public void setSession(MavenSession session) {
        this.session = session;        
    }
}
