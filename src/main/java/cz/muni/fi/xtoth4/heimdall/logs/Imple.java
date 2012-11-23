package cz.muni.fi.xtoth4.heimdall.logs;

import org.apache.log4j.Level;

import java.util.HashMap;
import java.util.Map;

public class Imple implements JsonLogger {


	@Override
	public Map<String, AttributeValue<? extends Comparable<?>>> log(String type, Level level, Object... vars) {

		Map<String, AttributeValue<? extends Comparable<?>>> map = new HashMap<>();

		for (int i = 0; i < vars.length; i++) {
			Object var = vars[i];

			map.put("ss", new AttributeValue<>("value", String.class));
		}

		return null;
	}
}
