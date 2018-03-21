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
* **NOTE**: requires the dependency org.rauschig:jarchivelib:0.7.1 to exist on the classpath. This dependency is not resolved transitively from this project.
* has a method to extract archive files with automatic archive format determination
### ConfigurationUtilities.java
* **NOTE**: requires the dependency org.apache.commons:commons-configuration2:2.2 to exist on the classpath. This dependency is not resolved transitively from this project.
* Helper methods for the apache commons configuration 2 project
* configuration parameter checking
* configuration parameter path creation
* configuration file reading
### FileUtilities.java
* Helper methods to read or write files
* Automatically handles regular or gzipped files
* Convention: A File instance with a name that ends with *.gz* or *.gzip* Is handled like a gzipped file automatically, in reading and writing.

