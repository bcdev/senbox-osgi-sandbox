Project Description
===================

This is _Teo_, an application is used to get in contact with OSGi programming and most importantly, as a mean to
make a decision if OSGi should be used as a fundamental architectural layer of the Sentinel Toolbox. In particular,
Teo covers the following use cases.

*UC-1*: Launch a GUI app and also a CLI app from a set of shared bundles
The current Ceres solution is to have a launcher that operates in two ways. For GUI apps, it works similar to OSGi,
that is, all JARs in the `modules` folder of the app's installation directory are started as dynamic modules.
For CLI apps, the launcher that constructs a classpath from all JARs in the `modules` folder.

    Result: This is no problem. See code org.teo.TeoCli and org.teo.TeoGui code. For the CLI,
    less bundles are needed. Therefore a special launch configuration could be used or we could
    use special Bundle requirements, see _Bundle Requirements_ in chapter 3.3.6 of the OSGi core spec.


*UC-2*: Run multiple app instances at the same time. This is mandatory for the CLI, desired for the GUI.
There is no problem with Ceres in this case, because Ceres does not persistently maintain module state.

    Result: with Felix there seems to be no problem as long as the framework property `org.osgi.framework.storage.clean`
    is not set. It's only value can be `onFirstInit` which will clear the cache before the framework is initialised and therefore
    causing multiple framework instances to fail. The current startup time is around 600ms.

*UC-3*: Use the Toolbox API in non-OSGi based environments such as as Calvalus and MERCI. Use the API from other languages, such as the Python API wrapper.
There is no problem with Ceres in this case, because Ceres does not persistently maintain module state. However,
we currently provide app services via two mechanisms: Java SPI and our Ceres extension registry. The latter is not
accessible in CLI mode. In CLI mode, we are restricted to Java SPI which requires a non-configurable
SPI implementation in Java.

    Result: Embed the OSGi framework. Bundles expose client API, other bundles implement them as services. The client API bundles
    are put on the classpath of the host app. Then a configured 
    framework instance is started. It allows the framework to delegate class loading to the app class loader
    for exposed client APIs. Then we access services via the framework's bundle context. 
    TODO: can we run OSGi embedded in a Tomcat servlet?

*UC-4*: Find out how OSGi can help implementing the _Toolbox Concept_, which is set of modules within a host application.

    Result: Excellent support using the _Package Deployment Admin_ of the compendium specs. See code in `teo-gui-obr` module.
    See also module `obr-maven-plugin` which can create spec-conform deployment packages. 
   
*UC-5*: Find out how OSGi can help implementing the _Stand-Alone Tools Adapter_, which is an an environment for the
integration external executables in the a host application.

    Result: Probably good support using the _Application Admin_ of the compendium specs. See code in `teo-gui-apps`
    module.
    
*UC-6*: Find out how to setup an OSGi framework, so that users can both install plugins by copying them into a 
`modules` folder and using a module manager. 

    Result: See `teo-launcher` which installs a directory watcher on `modules` folder. Module changes can easily be 
    translated into bundle install, update, and uninstall actions. 
    
*UC-7*: Find out how OSGi integrates with Maven and IDEs, namely IntelliJ IDEA. We must be able to easily analyse dependencies,
run and debug in different configurations directly from generated artifacts.

    Result: `maven-bundle-plugin` allows to create bundles JARs or create bundle manifest files using POM information.
    IDEA has OSGi framework support through specific facets.


Project Structure
=================

_Teo_ is composed of multiple sub projects which mainly generate OSGi bundles.

* `teo-launcher` - Teo's launcher. It is a bundle so that we can update it.
* `teo-appadmin` - A bundle that registers an OSGi Application Admin service implementation.
* `teo-core` - Represents Teo's core library and core API
* `teo-gui` - Represents Teo's (empty) desktop GUI application and GUI API. Registers the 'gui' service.
* `teo-gui-obr` - Uses the OSGi OSGi Repository Admin and and Deployment Admin services (tools will appear in "Help" menu)
* `teo-gui-apps` - Uses the OSGi OSGi Application Admin service (tool will appear in "Tools" menu)
* `com.acme.toolbox` - Generates a deployment package comprising the `teo-gui-acme1` and `teo-gui-acme2` bundles.
** `teo-gui-acme1` - GUI extension #1 from ACME (will appear in "View" menu)
** `teo-gui-acme2` - GUI extension #2 from ACME (will appear in "View" menu)
* `odp-maven-plugin` - OSGi Deployment Package Maven Plugin (goal `odp` is required because none of the Maven plugins can create ZIPs with entries in a defined order)


Setup
=====

_Teo_ uses the Apache Felix OSGi framework v4.4. In addition to Felix 4.4 you need download the following
bundles from http://felix.apache.org/ and copy their JARs to `$FELIX_HOME/bundles`.

* org.apache.felix.bundlerepository-1.6.6  (if not contained in framework by default)
* org.apache.felix.deploymentadmin-0.9.6
* org.apache.felix.dependencymanager-3.1.0


Building
========

`mvn install` does the job. All OSGi bundles (*.jar) are copied into the `modules` folder.

For deployment admin testing, a separate deployment package `com.acme.toolbox.dp` can be generated in
`com.acme.toolbox/target` by using the Maven goal `odp:odp`.


Launching
=========

You can use IDEA's OSGi Framework integration to launch Teo or directly using the `org.teo.cli.Main` class. 
In the latter case copy the exiting `teo.config.txt` and rename it to `teo.config`. It is a standard Java properties file. 
If you want to use different configurations set the Java system property `teo.config` to your configuration file.

To test package deployment, delete the ACME directories and JARs in the `modules` folder. As deployment package select
`com.acme.toolbox/target/com.acme.toolbox.dp.jar`.


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



Pros and Cons for Sentinel Toolbox
==================================

Pros
----

* Open specifications, documentation, widely used, lots of fora and discussion groups
** Bundle repository admin
** Bundle format
** Deployment admin for Toolbox installation (cool features such as fixes, rollback, icon)
** Deployment package format
* Efficiently solves plugins dependency conflicts
* Felix or Equinox OS framework implementations, both can be used
* OSGi framework APIs are not very invasive (can be limited to org.osgi.framework.BundleActivator sub classes)
* Replace all libraries in the `libs` folder of the app's installation directory by dynamical
  bundles.
* Modules can deployed to the user's home directory, so individual app configurations can exist in parallel
  for a single host application installed for all users


Cons
----

* Core specs are already complex, the compendium specs are overwhelming
* Using maven bundle plugin prevents IDEA from telling (import/export dependency) problems at compile time
* Framework runtime (import/export dependency, class loader) errors are very hard to trace




