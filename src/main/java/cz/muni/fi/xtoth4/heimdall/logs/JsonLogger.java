package cz.muni.fi.xtoth4.heimdall.logs;

import org.apache.log4j.Level;

import java.util.Map;

public interface JsonLogger {

	Map<String, AttributeValue<? extends Comparable<?>>> log(String type, Level level, Object ... vars);

}
