package org.teo;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A plugin that generates a Deployment Package from a Manifest file and attached resources.
 *
 * @author Norman Fomferra
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.PACKAGE)
public class OsgiDeploymentPackageMojo extends AbstractMojo {

    private static final String NAME = "Name:";

    @Parameter(defaultValue = "${project.build.finalName}")
    private String finalName;

    @Parameter(defaultValue = "${project.build.directory}/classes")
    private File baseDirectory;

    @Parameter(defaultValue = "${project.build.directory}")
    private File outputDirectory;

    @Parameter(defaultValue = "dp")
    private String filenameExtension;

    @Parameter(defaultValue = "UTF-8")
    private String manifestEncoding;

    @Parameter(required = false)
    private List<String> resources;

    public void execute() throws MojoExecutionException {

        File dpFile = new File(outputDirectory, finalName + "." + filenameExtension);
        getLog().info("Creating OSGi Deployment Package " + dpFile);

        Path manifestPath = FileSystems.getDefault().getPath(baseDirectory.getPath(), "META-INF", "MANIFEST.MF");
        List<String> resourceNames;
        try {
            getLog().info("Reading " + manifestPath);
            resourceNames = getResourcesFromManifest(manifestPath);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        if (!outputDirectory.exists()) {
            getLog().info("Creating output directory " + outputDirectory);
            outputDirectory.mkdirs();
        }
        try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(dpFile))) {
            zipStream.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            Files.copy(manifestPath, zipStream);
            addResources(dpFile, zipStream, resourceNames);
            if (resources != null) {
                addResources(dpFile, zipStream, resources);
            }
        } catch (IOException e) {
            dpFile.delete();
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private void addResources(File dpFile, ZipOutputStream zipStream, List<String> resourceNames) throws IOException {
        for (String resourceName : resourceNames) {
            Path resourcePath = FileSystems.getDefault().getPath(baseDirectory.getPath(), resourceName);
            getLog().info("Adding " + resourcePath + " to " + dpFile.getName());
            zipStream.putNextEntry(new ZipEntry(resourceName));
            Files.copy(resourcePath, zipStream);
        }
    }

    private List<String> getResourcesFromManifest(Path manifestPath) throws IOException {
        List<String> manifestLines = Files.readAllLines(manifestPath, Charset.forName(manifestEncoding));
        List<String> resourceNames = new ArrayList<>();
        for (String line : manifestLines) {
            if (line.startsWith(NAME)) {
                resourceNames.add(line.substring(NAME.length()).trim());
            }
        }
        return resourceNames;
    }
}
