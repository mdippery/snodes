Getting the Source
==================

The Snodes source code is available from a Git repository. If you don't have
Git, you can download it from [git-scm.com][git].

The "authoritative" Snodes repo is maintained by Michael Dippery, and is
read-only. To clone a copy of the repo:

    $ git clone git://github.com/mdippery/snodes.git

Building
========

Spaghetti Nodes is a Java application that uses **sbt** to build its JAR
file. sbt is available from [scala-sbt.org][sbt].

To build Snodes.jar:

1. Download and install sbt from [scala-sbt.org][sbt].
2. Navigate to Spaghetti Nodes' base directory.
3. Type `sbt run` to build and run Spaghetti Nodes.

[git]:        https://git-scm.com/
[sbt]:        https://www.scala-sbt.org/
