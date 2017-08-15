# julielab-java-utilities
A collection of small commonly useful utilities and helpers for Java.
This project should be extended by other commonly useful classes and methods.

BEFORE EXTENDING this project, please be (reasonibly) sure that the desired functionality is not already contained in another libarary. Candidates to check are
* [Apache Commons](https://commons.apache.org), check the components on the page
* [Google Guava](https://commons.apache.org/proper/commons-lang/), [Java API Docs](http://google.github.io/guava/releases/23.0/api/docs/)
Feel free to add more generally useful libraries here!

## Classes Overview

### CLIInteractionUtilities.java
* Helper methods to read from the command line
* With a message to print or without
* Restrited to boolean yes/no answers.
### FileUtilities.java
* Helper methods to read or write files
* Automatically handeles regular or gzipped files
* Convention: A File instance with a name that ends with *.gz* or *.gzip* Is handeled like a gzipped file automatically, in reading and writing.
