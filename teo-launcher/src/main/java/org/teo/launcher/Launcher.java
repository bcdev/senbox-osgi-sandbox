package org.teo.launcher;

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
import org.osgi.framework.startlevel.BundleStartLevel;
import org.teo.launcher.splash.SplashScreen;
import org.teo.launcher.splash.SplashScreenFactory;

import java.io.File;
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

/**
 * Teo's command line interface.
 */
public class Launcher {
    public static final String TEO_CONFIG = "teo.config";
    public static final String TEO_AUTO_START = "teo.auto.start";
    public static final String TEO_HEADLESS = "teo.headless";
    public static final String TEO_MODULES_PATH = "teo.modules.path";
    public static final String TEO_MODULES_WATCHER = "teo.modules.watcher";
    public static final String TEO_SPLASHSCREEN_IMAGE = "teo.splashscreen.image";


    public static final String DEFAULT_CONFIG_FILE = "./teo.config";
    public static final String DEFAULT_BUNDLE_CACHE_DIR = "./bundle-cache";
    public static final String DEFAULT_MODULES_DIR = "./modules";
    public static final long DEFAULT_TIMEOUT = 10 * 1000L;

    private Framework framework;
    private Properties config;
    private Path[] moduleDirs;
    private List<Path> bundlePaths;
    private List<Path> dpPaths;
    private DirectoryWatcher modulesWatcher;
    private boolean headlessMode;
    private long startTime;
    private SplashScreen splashScreen;

    public static Launcher start() throws Exception {
        Launcher launcher = new Launcher();
        launcher.launch();
        return launcher;
    }

    private Launcher() {
    }

    private void launch() throws IOException, BundleException {
        startTime = System.currentTimeMillis();

        loadConfig();

        splashScreen = SplashScreenFactory.createSplashScreen(config);
        splashScreen.open();

        headlessMode = System.getProperty(TEO_HEADLESS, "false").equalsIgnoreCase("true");

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        HashMap<String, String> frameworkConfig = getFrameworkConfig();

        FrameworkFactory factory = getFrameworkFactory();
        framework = factory.newFramework(frameworkConfig);
        framework.init();

        framework.getBundleContext().addFrameworkListener(new MyFrameworkListener());
        framework.getBundleContext().addBundleListener(new BundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                System.out.println("Bundle event: " + event.getOrigin().getSymbolicName() + ": " + event.getType());
            }
        });

        long t1 = System.currentTimeMillis();
        processModules(framework.getBundleContext());
        long t2 = System.currentTimeMillis();
        System.out.println("Processing Teo modules took " + (t2 - t1) + " ms");

        framework.getBundleContext().addFrameworkListener(splashScreen);

        long t3 = System.currentTimeMillis();
        framework.start();
        long t4 = System.currentTimeMillis();
        System.out.println("Starting framework took " + (t4 - t3) + " ms");
    }

    public Properties getConfig() {
        return config;
    }

    public Framework getFramework() {
        return framework;
    }

    public void stopAndWait() throws BundleException, InterruptedException {
        stopAndWait(DEFAULT_TIMEOUT);
    }

    public void stopAndWait(long timeout) throws BundleException, InterruptedException {
        framework.stop();
        waitForStop(timeout);
    }

    public void waitForStop() throws BundleException, InterruptedException {
        waitForStop(DEFAULT_TIMEOUT);
    }

    public void waitForStop(long timeout) throws BundleException, InterruptedException {
        FrameworkEvent frameworkEvent = framework.waitForStop(timeout);
        System.exit(frameworkEvent.getType() == FrameworkEvent.STOPPED ? 0 : frameworkEvent.getType());
    }

    private void loadConfig() throws IOException {
        Properties systemProperties = System.getProperties();

        File configFile = new File(systemProperties.getProperty(TEO_CONFIG, DEFAULT_CONFIG_FILE));
        config = new Properties(systemProperties);
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                config.load(reader);
            }
        }
        for (String key : systemProperties.stringPropertyNames()) {
            boolean unusedKey = key.startsWith("java.")
                                || key.startsWith("awt.")
                                || key.startsWith("os.")
                                || key.startsWith("user.")
                                || key.startsWith("idea.")
                                || key.startsWith("idea.")
                                || key.startsWith("sun.")
                                || key.startsWith("oracle.");
            if (!unusedKey) {
                config.put(key, systemProperties.getProperty(key));
            }
        }

        if (config.get(Constants.FRAMEWORK_STORAGE) == null) {
            config.put(Constants.FRAMEWORK_STORAGE, DEFAULT_BUNDLE_CACHE_DIR);
        }

//        if (config.get(Constants.FRAMEWORK_STORAGE_CLEAN) == null) {
//            config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
//        }
    }

    private HashMap<String, String> getFrameworkConfig() {
        HashMap<String, String> frameworkConfig = new HashMap<>();
        for (String key : config.stringPropertyNames()) {
            frameworkConfig.put(key, config.getProperty(key));
        }
        return frameworkConfig;
    }

    private static FrameworkFactory getFrameworkFactory() {
        ServiceLoader<FrameworkFactory> serviceLoader = ServiceLoader.load(FrameworkFactory.class);
        Iterator<FrameworkFactory> frameworkFactoryIterator = serviceLoader.iterator();
        return frameworkFactoryIterator.next();
    }

    private void processModules(BundleContext bundleContext) throws BundleException, IOException {

        Bundle[] installedBundles = framework.getBundleContext().getBundles();
        for (Bundle installedBundle : installedBundles) {
            BundleStartLevel startLevel = installedBundle.adapt(BundleStartLevel.class);
            Boolean persistentlyStarted = null;
            if (startLevel != null) {
                persistentlyStarted = startLevel.isPersistentlyStarted();
            }
            System.out.printf("Installed bundle found: %s %s; auto-start=%s%n",
                              installedBundle.getSymbolicName(), installedBundle.getVersion(),
                              persistentlyStarted != null ? persistentlyStarted : "?");
        }

        int startOptions = headlessMode ? Bundle.START_TRANSIENT : 0;
        startOptions = 0;

        String autoStart = config.getProperty(TEO_AUTO_START);
        ArrayList<Bundle> autoStartBundles = new ArrayList<>();
        if (autoStart != null) {
            String[] bundlePaths = autoStart.split("\\s+");
            for (String bundlePath : bundlePaths) {
                String location = bundlePath.trim();
                System.out.println("Installing auto start bundle: " + location);
                Bundle bundle = bundleContext.installBundle(location);
                autoStartBundles.add(bundle);
            }
            for (Bundle bundle : autoStartBundles) {
                System.out.printf("Starting auto start bundle: %s %s (from %s)%n",
                                  bundle.getSymbolicName(),
                                  bundle.getVersion(),
                                  bundle.getLocation());
                bundle.start(startOptions);
            }
        }

        Object modulesPathVal = config.getProperty(TEO_MODULES_PATH, DEFAULT_MODULES_DIR);
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
            System.out.println("Installing module bundle: " + location);
            Bundle bundle = bundleContext.installBundle(location);
            bundles.add(bundle);
        }

        for (Bundle bundle : bundles) {
            System.out.printf("Starting module bundle: %s %s (from %s)%n",
                              bundle.getSymbolicName(),
                              bundle.getVersion(),
                              bundle.getLocation());
            bundle.start(startOptions);
        }

        // todo
        // After framework started
        // DeploymentAdmin pda = getDeploymentAdmin();
        for (Path dpPath : dpPaths) {
            String location = dpPath.toFile().toURI().toString();
            // pda.installPackage(location);
            System.out.println("OSGi deployment package found: " + location);
        }
    }

    private class MyFrameworkListener implements FrameworkListener {

        public MyFrameworkListener() {
        }

        @Override
        public void frameworkEvent(FrameworkEvent event) {
            if (event.getType() == FrameworkEvent.STARTED) {
                final long frameworkStartTime = System.currentTimeMillis();
                long delta = frameworkStartTime - startTime;
                System.out.println("Framework started after " + delta + " ms");

                boolean installWatcher = config.getProperty(TEO_MODULES_WATCHER, "false").equalsIgnoreCase("true");
                if (!installWatcher) {
                    try {
                        initModuleDirectoryWatcher();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (event.getType() == FrameworkEvent.STOPPED) {
                if (modulesWatcher != null) {
                    modulesWatcher.stop();
                }
            }
        }

        private void initModuleDirectoryWatcher() throws IOException {
            modulesWatcher = new DirectoryWatcher();
            for (Path moduleDir : moduleDirs) {
                modulesWatcher.addDirectory(moduleDir);
            }
            modulesWatcher.addListener(new DirectoryListener());
            modulesWatcher.start();
        }

    }

    private static class DirectoryListener implements DirectoryWatcher.Listener {
        @Override
        public void entryCreated(Path dir, Path child) {
            System.out.println("Teo modules watcher: entryCreated: dir = " + dir + ", child = " + child);
        }

        @Override
        public void entryDeleted(Path dir, Path child) {
            System.out.println("Teo modules watcher: entryDeleted: dir = " + dir + ", child = " + child);
        }

        @Override
        public void entryModified(Path dir, Path child) {
            System.out.println("Teo modules watcher: entryModified: dir = " + dir + ", child = " + child);
        }
    }

    private class ModuleFileVisitor implements FileVisitor<Path> {

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path manifestPath = FileSystems.getDefault().getPath(dir.toString(), "META-INF", "MANIFEST.MF");
            if (Files.isRegularFile(manifestPath, LinkOption.NOFOLLOW_LINKS)) {
                bundlePaths.add(dir);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String s = file.getFileName().toString().toLowerCase();
            if (s.endsWith(".jar") || s.endsWith(".zip")) {
                bundlePaths.add(file);
            }
            if (s.endsWith(".dp")) {
                dpPaths.add(file);
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

    private class ShutdownHook extends Thread {
        public ShutdownHook() {
            super("Teo shutdown hook");
        }

        public void run() {
            try {
                if (modulesWatcher != null) {
                    modulesWatcher.stop();
                }
                if (framework != null) {
                    framework.stop();
                    framework.waitForStop(0);
                }
            } catch (Exception ex) {
                System.err.println("Error stopping OSGi framework: " + ex);
            }
        }
    }
}
