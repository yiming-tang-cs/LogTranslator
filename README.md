### === NGMON LogTranslator ===

Project for changing classic log statements from Log4j, Slf4j, Commons Logging frameworks into NGMON's syntax.

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


#### Maven Dependencies

 <dependency>
      <groupId>org.ngmon</groupId>
      <artifactId>ngmon-logger-java</artifactId>
      <version>1.0-SNAPSHOT</version>
  </dependency>
  <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-api</artifactId>
      <version>2.0-rc1</version>
  </dependency>
  <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-core</artifactId>
      <version>2.0-rc1</version>
  </dependency>