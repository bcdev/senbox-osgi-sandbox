
Setup
=====

This program, _Teo_ uses the Apache Felix OSGi framework v4.4. In addition to Felix 4.4 you need download the following 
bundles from http://felix.apache.org/ and copy their JARs to `$FELIX_HOME/bundles`.

* org.apache.felix.deploymentadmin-0.9.6
* org.apache.felix.bundlerepository-1.6.6
* org.apache.felix.dependencymanager-3.1.0


Launching
=========

You can use IDEA's OSGi Framework integration to launch Teo or directly using the `org.teo.cli.Main` class. 
In the latter case copy the exiting `teo.config.txt` and rename it to `teo.config`. It is a standard Java properties file. 
If you want to use different configurations set the Java system property `teo.config` to your configuration file.



Existing Problems and Questions
===============================

Multiple instances at the same time
-----------------------------------

Problem: We must be able to launch multiple instances of the CLI application (e.g. ten parallel processes in batch mode)
from the same installation. Afaik OSGi framework instances will then need independent bundle caches which introduces a
considerable file I/O overhead and may also occupy a lot of disk space. This is although the CLI applications has a 
limited lifetime.

Solution: None so far. May hack Felix in order to use Java 7 FileSystem and support a RAM disk. May then avoid 
copying JAR files into the cache by using links.


CLI/GUI at the same time
------------------------

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
---------------------

Solution 1: Write own main and embed OSGi framework instance.
Solution 2: Use Equinox IApplication extension point.
Solution 3: Use bnd tool and the 'launcher.arguments' service property.
Solution 4: Use "Framework Launching" as of OSGi spec 4.2
* http://felix.apache.org/site/apache-felix-framework-launching-and-embedding.html
* http://stackoverflow.com/questions/434664/accessing-command-line-arguments-from-osgi-bundle
* http://stackoverflow.com/questions/19515773/command-line-arguments-and-jvm-parameters-for-osgi-bundles


OSGi Development
----------------

Problem: When using the Felix Maven Bundle Plugin, the MANIFEST.MF is created only after during 
compilation. This prevents an IDE from detected configuration errors at compile time.



Pros and Cons
=============

Pros
----

* Open specifications, documentation, widely used, lots of fora and discussion groups
** Bundle format
** Bundle repository admin
** Deployment package format
** Deployment admin
* Framework implementations, Felix & Equinox


Cons
----

* It is complex and complicated
* Framework runtime errors are hard to trace




