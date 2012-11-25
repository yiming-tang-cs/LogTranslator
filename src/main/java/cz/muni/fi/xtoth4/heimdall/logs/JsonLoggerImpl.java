package cz.muni.fi.xtoth4.heimdall.logs;


import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Level;

import java.io.*;
import java.util.*;


/**
 * User: michalxo
 * Date: 11/23/12
 * Time: 6:14 PM
 */
public class JsonLoggerImpl implements JsonLogger {
    private Payload payload = new Payload();
    private Log2Json l2j = new Log2Json();


    public Log2Json log(String type, Level level, String message) {
        payload.add("message", message);
        //l2j.setPayload(payload);
        l2j = new Log2Json(type, level, 0, payload);
        return l2j;
    }

    @Override
    public void log(String type, Level level, String address, Integer port) {
        l2j = new Log2Json();
        payload = new Payload();
        l2j.setLevel(level);
        l2j.setType(type);
        payload.add("address", address);
        payload.add("port", port);
        l2j.setPayload(payload);
    }

    @Override
    public Map<String, AttributeValue<? extends Comparable<?>>> log(String type, Level level, Object... vars) {
        return null;
    }


    @Override
    public Map<String, AttributeValue<? extends Comparable<?>>> log(String type, Level level, File jsonFile, Object... vars) {
        Map<String, AttributeValue<? extends Comparable<?>>> map;
        Map<String, String> mapStr;

        mapStr = parseJson(jsonFile);
        if (vars.length != mapStr.size()) {
            throw new IllegalArgumentException("Size of arguments <" + vars.length + "> and values in json file <" +
                    mapStr.size() + "> are not equal.");
        } else {
            map = typeCastMap(mapStr, vars);
        }
        return map;
    }

    private static Map<String, AttributeValue<? extends Comparable<?>>> typeCastMap(Map stringMap, Object... vars) {
        Map<String, AttributeValue<? extends Comparable<?>>> map = new LinkedHashMap<>(); // preserve insertion-order
        int i = 0;
        for (Object key : stringMap.keySet()) {
            String strClazz = (String) stringMap.get(key);
            String name = (String) key;
            Object value = vars[i];
            Class clazz = value.getClass();

            //Compare value type and put it into map with appropriate typecasting
            if (!clazz.toString().contains(strClazz)) {
                throw new IllegalArgumentException("Expected <" + clazz +"> but found <" + clazz + ">.");
            }
            if (clazz.equals(Integer.class)) {
                map.put(name, new AttributeValue<Integer>((Integer) value, Integer.class));

            } else if (clazz.equals(Double.class)) {
                map.put(name, new AttributeValue<Double>((Double) value, Double.class));

            } else if (clazz.equals(Float.class)) {
                map.put(name, new AttributeValue<>((Float) value, Float.class));

            } else if (clazz.equals(Long.class)) {
                map.put(name, new AttributeValue<>((Long) value, Long.class));

            } else if (clazz.equals(String.class)) {
                map.put(name, new AttributeValue<>((String) value, String.class));
            }
            i++;
        }
        return map;
    }

    /**
     * Simple parsing of json schema for variable number of attributes with different types.
     * Names of payload attributes are provided in this json schema file.
     * @param file valid Json schema
     * @return Map Key\,Value\> where key is name of attribute in json schema and value is appropriate type.
     */
    public static Map<String, String> parseJson(File file) {
        JsonFactory jf = new JsonFactory();
        Map<String, String> map = new LinkedHashMap<>();

        try {
            JsonParser jp = jf.createJsonParser(file);
            JsonToken token;
            String name;
            String value = "";

            // Parse json schema - supported only simple nesting schemas
            while ((token = jp.nextToken()) != JsonToken.END_OBJECT) {
                if ((token == JsonToken.START_OBJECT) || (token == JsonToken.END_OBJECT)) {
                    jp.nextToken();
                }

                name = jp.getText();
                jp.nextToken();
                if ("{".equals(jp.getText())) {
                    while (jp.nextToken() != JsonToken.END_OBJECT) {
                        if ("type".equals(jp.getText())) {
                            jp.nextToken();
                            value = jp.getText();
                        }
                    }
                } else {
                    value = jp.getText();
                }
                //System.out.println(name + " : " + value);
                //First letter to upperCase
                char[] chars = value.toCharArray();
                chars[0] = Character.toUpperCase(chars[0]);
                value = new String(chars);
                if (!name.equals("description")){
                    map.put(name, value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }


}
