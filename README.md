# julielab-java-utilities
A collection of small commonly useful utilities and helpers for Java.
This project should be extended by other commonly useful classes and methods.

BEFORE EXTENDING this project, please be (reasonably) sure that the desired functionality is not already contained in another libarary. Candidates to check are
* [Apache Commons](https://commons.apache.org), check the components on the page
* [Google Guava](https://commons.apache.org/proper/commons-lang/), [Java API Docs](http://google.github.io/guava/releases/23.0/api/docs/)

Feel free to add more generally useful libraries here!

## Classes Overview

### CLIInteractionUtilities.java
* Helper methods to read from the command line
* With a message to print or without
* Restricted to boolean yes/no answers.
### CompressionUtilities.java
* **NOTE**: requires the dependency `org.rauschig`:`jarchivelib`:`0.7.1` to exist on the classpath. This dependency is not resolved transitively from this project.
* has a method to extract archive files with automatic archive format determination
### ConfigurationUtilities.java
* **NOTE**: requires the dependency `org.apache.commons`:`commons-configuration2`:`2.2` to exist on the classpath. This dependency is not resolved transitively from this project.
* Helper methods for the apache commons configuration 2 project
* configuration parameter checking
* configuration parameter path creation
* configuration file reading
### IOStreamUtilities
* Method to get a UTF-8 encoded reader from an InputStream
* Method to get a list of UTF-8 encoded lines from an InputStream
### FileUtilities.java
* Helper methods to read or write files
* Automatically handles regular or gzipped files
* Convention: A File instance with a name that ends with *.gz* or *.gzip* Is handled like a gzipped file automatically, in reading and writing.
* Method to create a JAR file from an array of given files
* Method to add a file into an existing JAR
* Method to find a (file) resource via file path, URI or on the classpath.
### JarLoader.java
* **NOTE** requires the dependency `net.bytebuddy`:`byte-buddy-agent`:`1.7.9` to exist on the classpath. This dependency is not resolved transitively from this project.
* Allows to load JAR files during runtime
* Exploits the fact that `the system class loader is an `URIClassLoader` until Java 8
* Automatically detects Java version to pick the correct JAR loading strategy
* Beginning with Java 9, uses the `java.lang.instrument` package and employs the Agent class included in this project
  * Needs to determine the file path of the JAR containing the Agent class
  * This JAR needs to have a `META-INF/MANIFEST.MF` file with the entry `Agent-Class: de.julielab.java.utilities.classpath.Agent`
  * The `julielab-java-utilities` JAR is searched on the classpath for this purpose.
  * In case of an uber JAR or fat JAR (e.g. through the Maven assembly or shadow plugins), the uber JAR itself is pointed to. The uber JAR must then have the manifest entry as explained above.
### prerequisites.PrerequisiteChecker
* Simple to use API to check parameter values for being not null or not empty. Automatically generates an error report if checks fail via an `IllegalArgumentException.
* Example: `PrerequisiteChecker.checkThat().notNull(ob1, ob2).notEmpty(coll).withNames("ob1", "ob2", "coll").execute()`
* Is **deactivated** by default: No checks will be performed unless the Java system property `de.julielab.prerequisitechecksenabled` is set to `true`.
* Can be used with the `Supplier` interface. This allows to quickly check a path within a given object, e.g. `PrerequisiteChecker.checkThat().notNull(ob).notNull(() -> ob.prop1).notNull(() -> ob.prop1.prop2).withNames("Base object), "Property 1", "Property 2).execute()`
### UriUtilities.java
* Get InputStreams and Readers from `java.net.URI`
* Automatically handles regular or gzipped files, analogous to FileUtilities.java
