package org.teo.cli;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Teo's command line interface.
 */
public class Main {

    public static final String DEFAULT_CONFIG_FILE = "./teo.config";
    public static final String DEFAULT_BUNDLE_CACHE_DIR = "./bundle-cache";
    public static final String DEFAULT_MODULES_DIR = "./modules";

    static Framework framework;
    private static String contextName;
    private static Properties config;
    private static Path[] moduleDirs;
    private static List<Path> bundlePaths;
    private static List<Path> dpPaths;
    private static DirectoryWatcher directoryWatcher;


    public static void main(String[] args) throws Exception {

        JarInputStream jarFile = new JarInputStream(new FileInputStream("com.acme.toolbox/target/com.acme.toolbox.dp.jar"));
        Manifest manifest = jarFile.getManifest();
        System.out.println("manifest = [" + manifest + "]");
        while (true) {
            JarEntry entry = jarFile.getNextJarEntry();
            if (entry == null) break;
            System.out.println("entry = [" + entry + "]");
        }

        //System.exit(0);

        final long mainEnterTime = System.currentTimeMillis();

        Properties systemProperties = System.getProperties();
        contextName = systemProperties.getProperty("teo.context", "teo");
        File configFile = new File(systemProperties.getProperty(contextName + ".config", DEFAULT_CONFIG_FILE));
        config = new Properties(systemProperties);
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                config.load(reader);
            }
        }
        for (String key : systemProperties.stringPropertyNames()) {
            if (key.startsWith(contextName + ".")
                    || key.startsWith("felix.")
                    || key.contains(".felix.")
                    || key.startsWith("org.osgi.")) {
                config.put(key, systemProperties.getProperty(key));
            }
        }

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        HashMap<String, String> frameworkConfig = getFrameworkConfig();

        try {
            FrameworkFactory factory = getFrameworkFactory();
            framework = factory.newFramework(frameworkConfig);
            framework.init();

            long t1 = System.currentTimeMillis();
            processModules(framework.getBundleContext());
            long t2 = System.currentTimeMillis();
            System.out.println("Processing modules took " + (t2 - t1) + " ms");

            framework.getBundleContext().addFrameworkListener(new MyFrameworkListener(mainEnterTime));
            framework.getBundleContext().addBundleListener(new BundleListener() {
                @Override
                public void bundleChanged(BundleEvent event) {
                    System.out.println("Bundle event: " + event.getOrigin().getSymbolicName() + ": " + event.getType());
                }
            });
            long t3 = System.currentTimeMillis();
            framework.start();
            long t4 = System.currentTimeMillis();
            System.out.println("Starting framework took " + (t4 - t3) + " ms");
            framework.waitForStop(0);
            System.exit(0);
        } catch (Exception ex) {
            System.err.println("Could not create OSGi framework: " + ex);
            ex.printStackTrace();
            System.exit(2);
        }
    }

    private static HashMap<String, String> getFrameworkConfig() {
        HashMap<String, String> frameworkConfig = new HashMap<>();
        for (String key : config.stringPropertyNames()) {
            frameworkConfig.put(key, config.getProperty(key));
        }
        if (frameworkConfig.get(Constants.FRAMEWORK_STORAGE) == null) {
            frameworkConfig.put(Constants.FRAMEWORK_STORAGE, DEFAULT_BUNDLE_CACHE_DIR);
        }
        if (frameworkConfig.get(Constants.FRAMEWORK_STORAGE_CLEAN) == null) {
            frameworkConfig.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        }
        return frameworkConfig;
    }

    private static FrameworkFactory getFrameworkFactory() {
        ServiceLoader<FrameworkFactory> serviceLoader = ServiceLoader.load(FrameworkFactory.class);
        Iterator<FrameworkFactory> frameworkFactoryIterator = serviceLoader.iterator();
        return frameworkFactoryIterator.next();
    }

    private static void processModules(BundleContext bundleContext) throws BundleException, IOException {

        String autoStart = config.getProperty(contextName + ".auto.start");
        ArrayList<Bundle> autoStartBundles = new ArrayList<>();
        if (autoStart != null) {
            String[] bundlePaths = autoStart.split("\\s+");
            for (String bundlePath : bundlePaths) {
                String location = bundlePath.trim();
                System.out.println("Auto start bundle: " + location);
                Bundle bundle = bundleContext.installBundle(location);
                autoStartBundles.add(bundle);
            }
            for (Bundle bundle : autoStartBundles) {
                bundle.start();
            }
        }

        Object modulesPathVal = config.getProperty(contextName + ".modules.path", DEFAULT_MODULES_DIR);
        String modulesPathStr = modulesPathVal.toString();
        String[] pathNames = modulesPathStr.split(File.pathSeparator);
        moduleDirs = new Path[pathNames.length];
        for (int i = 0; i < pathNames.length; i++) {
            moduleDirs[i] = FileSystems.getDefault().getPath(pathNames[i]).toAbsolutePath().normalize();
        }

        bundlePaths = new ArrayList<>();
        dpPaths = new ArrayList<>();

        for (Path modulesPath : moduleDirs) {
            Files.walkFileTree(modulesPath, EnumSet.noneOf(FileVisitOption.class), 1, new ModuleFileVisitor());
        }

        ArrayList<Bundle> bundles = new ArrayList<>();
        for (Path bundlePath : bundlePaths) {
            String location = bundlePath.toFile().toURI().toString();
            System.out.println("Module bundle: " + location);
            Bundle bundle = bundleContext.installBundle(location);
            bundles.add(bundle);
        }

        for (Bundle bundle : bundles) {
            bundle.start();
        }


        // todo
        // After framework started
        // DeploymentAdmin pda = getDeploymentAdmin();
        for (Path dpPath : dpPaths) {
            String location = dpPath.toFile().toURI().toString();
            // pda.installPackage(location);
            System.out.println("Deployment package found: " + location);
        }
    }

    private static class MyFrameworkListener implements FrameworkListener {
        private final long mainEnterTime;

        public MyFrameworkListener(long mainEnterTime) {
            this.mainEnterTime = mainEnterTime;
        }

        @Override
        public void frameworkEvent(FrameworkEvent event) {
            if (event.getType() == FrameworkEvent.STARTED) {
                final long frameworkStartTime = System.currentTimeMillis();
                long delta = frameworkStartTime - mainEnterTime;
                System.out.println("framework started after " + delta + " ms");

                try {
                    initModuleDirectoryWatcher();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void initModuleDirectoryWatcher() throws IOException {
            directoryWatcher = new DirectoryWatcher();
            for (Path moduleDir : moduleDirs) {
                directoryWatcher.addDirectory(moduleDir);
            }
            directoryWatcher.addListener(new DirectoryListener());
            directoryWatcher.start();
        }

    }

    private static class DirectoryListener implements DirectoryWatcher.Listener {
        @Override
        public void entryCreated(Path dir, Path child) {
            System.out.println("entryCreated: dir = " + dir + ", child = " + child);
        }

        @Override
        public void entryDeleted(Path dir, Path child) {
            System.out.println("entryDeleted: dir = " + dir + ", child = " + child);
        }

        @Override
        public void entryModified(Path dir, Path child) {
            System.out.println("entryModified: dir = " + dir + ", child = " + child);
        }
    }

    private static class ModuleFileVisitor implements FileVisitor<Path> {
        final boolean dirsOnly = config.getProperty(contextName + ".modules.dirsOnly", "false").equals("true");
        final boolean jarsOnly = config.getProperty(contextName + ".modules.jarsOnly", "false").equals("true");

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (!jarsOnly) {
                Path manifestPath = FileSystems.getDefault().getPath(dir.toString(), "META-INF", "MANIFEST.MF");
                if (Files.isRegularFile(manifestPath, LinkOption.NOFOLLOW_LINKS)) {
                    bundlePaths.add(dir);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!dirsOnly) {
                String s = file.getFileName().toString().toLowerCase();
                if (s.endsWith(".jar") || s.endsWith(".zip")) {
                    bundlePaths.add(file);
                }
                if (s.endsWith(".dp")) {
                    dpPaths.add(file);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            System.out.println("Error: " + file + ": " + exc.getMessage());
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }

    private static class ShutdownHook extends Thread {
        public ShutdownHook() {
            super(Main.contextName + " shutdown hook");
        }

        public void run() {
            try {
                if (directoryWatcher != null) {
                    directoryWatcher.stop();
                }
                if (framework != null) {
                    framework.stop();
                    framework.waitForStop(0);
                }
            } catch (Exception ex) {
                System.err.println("Error stopping framework: " + ex);
            }
        }
    }
}
