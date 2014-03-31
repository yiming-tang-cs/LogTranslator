### === NGMON LogTranslator ===

Project for changing classic log statements from Log4j, Slf4j, Commons Logging frameworks into NGMON's syntax.
For now, it works only with maven projects.

1) Set appropriately all the necessary settings in logtranslator.properties files and execute

2) mvn install

3) mvn exec:exec

4) Add maven dependencies on NGMON. You can find them in Readme.txt file.

#### Possible problems
 * Syntax errors complaining mainly about "diamonds" and their type mismatch.
    * **Fix:** Make sure you compile and run with JDK 1.7 and language level is set to "Diamonds, ARM, Multicatach". (Project settings in your IDE)

 * [WARNING] bad version number found in .m2/repository/org/aspectj/aspectjrt/1.7.2/aspectjrt-1.7.2.jar expected 1.6.11 found 1.7.2
    * **Fix:** Change in "aspectj-maven-plugin" artifact .m2/repository/org/codehaus/mojo/aspectj-maven-plugin/1.4/aspectj-maven-plugin-1.4.pom.xml file version
      <aspectjVersion>1.7.2</aspectjVersion> from 1.6.11. This might help to solve a problem.

 * [WARNING] antlr & ngmon logger are having some problems during compilation. We pass '-XX:-UseSplitVerifier' argument to JVM.
    This behaviour and workaround is tested with OpenJDK 1.7.51 and OracleJDK 1.7.17.

#### Maven Dependencies

 You also need to build ngmon-logger-java by yourself.

 Sources are available to download from https://github.com/ngmon/ngmon-logger-java/

<pre>
<code>
 &lt;dependency&gt;
      &lt;groupId&gt;org.ngmon&lt;/groupId&gt;
      &lt;artifactId&gt;ngmon-logger-java&lt;/artifactId&gt;
      &lt;version&gt;1.0-SNAPSHOT&lt;/version&gt;
  &lt;/dependency&gt;
  &lt;dependency&gt;
      &lt;groupId&gt;org.apache.logging.log4j&lt;/groupId&gt;
      &lt;artifactId&gt;log4j-api&lt;/artifactId&gt;
      &lt;version&gt;2.0-rc1&lt;/version&gt;
  &lt;/dependency&gt;
  &lt;dependency&gt;
      &lt;groupId&gt;org.apache.logging.log4j&lt;/groupId&gt;
      &lt;artifactId&gt;log4j-core&lt;/artifactId&gt;
      &lt;version&gt;2.0-rc1&lt;/version&gt;
  &lt;/dependency&gt;
</code>
</pre>
