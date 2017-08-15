# julielab-java-utilities
A collection of small commonly useful utilities and helpers for Java.

## Classes Overview

### CLIInteractionUtilities.java
* Helper methods to read from the command line
* With a message to print or without
* Restrited to boolean yes/no answers.
### FileUtilities.java
* Helper methods to read or write files
* Automatically handeles regular or gzipped files
* Convention: A File instance with a name that ends with *.gz* or *.gzip* Is handeled like a gzipped file automatically, in reading and writing.
