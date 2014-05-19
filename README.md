CLI/GUI applications
====================

Problem: We must be able to launch multiple applications from the same installation,
at least a single CLI and GUI. A CLI must start up fast, it should not require
all the GUI-related modules which might also require processing lots of
extension point configuration from their module descriptors.

Solution 1: Run CLI with lower start level than GUI.

Solution 1 problem: Plugins must know their start levels. A reader plugins should be
recognised by both CLI and GUI modes, while a tool window extension only applies to
a GUI.

Solution 2: A configuration property tells plugin activators whether we are in
GUI mode or headless CLI mode. Expensive extension point parsing occurs only
if required and applicable. E.g. tool windows & action processing does not take
place in CLI mode.

Solution 3: Plugin activators must actively register their extension services.
The activator will only register its service if a certain service registry is available
(--> OSGi ServiceTracker).

Solution 2/3 problem: Requires activators which is generally a bad idea because class
loaders are always created and Java code is executed even if never needed. Writing of
activators also forces intrusion of OSGi specific API which should be avoided.
Much better to use declarative services (DS).

Problem 1/2/3: Bundle cache is for a single framework instance. Each instance requires
its own bundle cache (is that true?)

CLI arguments passing
=====================

Solution 1: Write own main and embed OSGi framework instance.
Solution 2: Use Equinox IApplication extension point.
Solution 3: Use bnd tool and the 'launcher.arguments' service property.
Solution 4: Use "Framework Launching" as of OSGi spec 4.2
* http://felix.apache.org/site/apache-felix-framework-launching-and-embedding.html
* http://stackoverflow.com/questions/434664/accessing-command-line-arguments-from-osgi-bundle
* http://stackoverflow.com/questions/19515773/command-line-arguments-and-jvm-parameters-for-osgi-bundles


OSGi Development
================

Problem: When using the Felix Maven Bundle Plugin, the MANIFEST.MF is created only after during 
compilation. This prevents an IDE from detected configuration errors at compile time.


