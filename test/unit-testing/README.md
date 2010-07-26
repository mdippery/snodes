Integrating Junit
=================

Apache Ant can easily integrate JUnit into its build system, but it requires some configuration. If you are using a version of Ant older than 1.7, you must copy `junit.jar` to a location on Ant's classpath. `junit.jar` should be copied to `$HOME/.ant/lib`.

Adding Test Cases
=================

1. Name test case classes with the word "Test", followed by the class you are
   testing. For example, if you are testing PathUtils, name your test case
   class "TestPathUtils".
2. All test case classes should be installed in `snodes.<your package>`.
   For example, to test PathUtils, create a test case class with the
   fully-qualified name `snodes.fs.TestPathUtils`.
