Getting the Source
==================

The Snodes source code is available from a Git repository. If you don't have
Git, you can download it from http://git.or.cz/.

The "authoritative" Snodes repo is maintained by Michael Dippery, and is
read-only. To clone a copy of the repo:

    $ git clone git://github.com/mdippery/snodes.git

Building
========

Spaghetti Nodes is a Java application that uses Apache Ant to build its JAR
file. Apache Ant is available from http://ant.apache.org/.

To build Snodes.jar:

1. Download and install Apache Ant from http://ant.apache.org/.
2. Navigate to Spaghetti Nodes' base directory.
3. At a command prompt, type `ant`.
4. Spaghetti Nodes has been built. The output file is `Snodes.jar`.
5. Type `java -jar Snodes.jar` to run Spaghetti Nodes.

More information on Ant is available at http://ant.apache.org/manual/.
