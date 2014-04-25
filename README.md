### === NGMON LogTranslator ===

Project for changing classic log statements from Log4j, Slf4j, Commons Logging frameworks into NGMON's syntax.
For now, it works only with maven projects.

1) Set appropriately all the necessary settings in logtranslator.properties files and execute

2) mvn install

3) mvn exec:exec

4) Install by maven generated source project

5) Add maven project settings for NGMON LogTranslator in parent's pom.xml file. You can find them [here][mvn-settings].

6) Recompile your application of choice (Apache Hadoop in our case)

7) Execute hadoop job with added classpath to ngmon-logger jar.

[How to add custom jar to MapReduce] (http://blog.cloudera.com/blog/2011/01/how-to-include-third-party-libraries-in-your-map-reduce-job/)

#### Possible problems
 * Syntax errors complaining mainly about "diamonds" and their type mismatch.
    * **Fix:** Make sure you compile and run with JDK 1.7 and language level is set to "Diamonds, ARM, Multicatch". (Project settings in your IDE)

 * [WARNING] bad version number found in .m2/repository/org/aspectj/aspectjrt/1.7.2/aspectjrt-1.7.2.jar expected 1.6.11 found 1.7.2
    * **Fix:** Change in "aspectj-maven-plugin" artifact .m2/repository/org/codehaus/mojo/aspectj-maven-plugin/1.4/aspectj-maven-plugin-1.4.pom.xml file version
      <aspectjVersion>1.7.2</aspectjVersion> from 1.6.11. This might help to solve a problem.

 * [WARNING] antlr & ngmon logger are having some problems during compilation. We pass '-XX:-UseSplitVerifier' argument to JVM.
    This behaviour and workaround is tested with OpenJDK 1.7.51 and OracleJDK 1.7.17.

 * All variables should be declared before usage. LogTranslator is not as powerful as compiler and makes only 1 pass over each java file.
   (fix hadoop-common/hadoop-tools/hadoop-streaming/src/main/java/org/apache/hadoop/streaming/StreamJob.java )
#### Maven Dependencies
[mvn-deps]:
 You also need to build ngmon-logger-java by yourself.

 Sources are available to download from https://github.com/ngmon/ngmon-logger-java/

 You need to add/change packaging to "pom" in logtranslator's project parent's pom.xml file.
  Also, you have to add logtranslator as module for this parent to understand logtranslator's directory.

    <dependency>
       <groupId>org.ngmon.logger</groupId>
       <artifactId>logtranslator</artifactId>
       <version>1.0-SNAPSHOT</version>
       <scope>compile</scope>
    </dependency>

Or add as module?!



#==== Apache Hadoop ====
## == Installation procedure ==
1) Install these tools on your machine:
protobuf-c
hawtbuf-protoc  (maven's invocator of proto-buf-c?)

2) Download findbugs  (http://findbugs.sourceforge.net/downloads.html)
environment properties settings:
set FINDBUGS_HOME
set proper JAVA_HOME - must point to javah executable

3)
git clone https://github.com/apache/hadoop-common.git

We have used 'commit 33a47d90022f8c6611e89f2da0b6f72e008ed529' for testing purposes.

mvn package -Pdist,native,docs -Dtar -DskipTests

4)
./hadoop-dist/target/hadoop-3.0.0-SNAPSHOT/bin/hadoop jar ./hadoop-mapreduce-project/hadoop-mapreduce-examples/target/hadoop-mapreduce-examples-3.0.0-SNAPSHOT.jar teragen 3 /tmp/teragen2

Note: When translating hadoop-common, make sure you do following:

Move fields definitions from back of the file, to the beginning before first constructor/ method.
hadoop-common/hadoop-tools/hadoop-streaming/src/main/java/org/apache/hadoop/streaming/StreamJob.java