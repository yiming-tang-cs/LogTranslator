package cz.muni.fi.xtoth4.heimdall.logs;

import org.apache.log4j.Level;

import java.io.File;
import java.util.Map;

public interface JsonLogger {


    Log2Json log(String type, Level level, String message);


    void log(String type, Level level, String address, Integer port);

    // ?
    Map<String, AttributeValue<? extends Comparable<?>>> log(String type, Level level, Object ... vars);


    Map<String, AttributeValue<? extends Comparable<?>>> log(String type, Level level, File jsonSchemaFile, Object ... vars);
}
