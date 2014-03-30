package org.ngmon.logger.logtranslator.generator;

import org.ngmon.logger.logtranslator.common.Utils;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class LogGlobalGenerator {

    private static final String sep = File.separator;

    private static final List<String> levels = Arrays.asList("trace", "debug", "info", "warn", "error", "fatal", "");
    public static final String path = Utils.getApplicationHome() + "src" + sep + "main" + sep + "java" + sep +
            Utils.getNgmonLogGlobal().replace(".", sep) + ".java";


    public static void create() {
        String LOG_GLOBAL_TEMPLATE = "package <packageNamespace>;\n\n" +
                "public class LogGlobal {\n\n" +
                "<methods>" +
                "    public static boolean log() {\n" +
                "        return true;\n" +
                "    }\n" +
                "}\n";
        ST logGlobalFile = new ST(LOG_GLOBAL_TEMPLATE);
        String namespace = Utils.getNgmonLogGlobal();
        namespace = namespace.substring(0, namespace.lastIndexOf("."));
        logGlobalFile.add("packageNamespace", namespace);

        StringBuilder methods = new StringBuilder();
        for (String level : levels) {
            methods.append(generateMethod(level));
        }
        logGlobalFile.add("methods", methods);

        FileCreator.createDirectory(FileCreator.createPathFromString(path.substring(0, path.lastIndexOf(sep))));
        FileCreator.createFile(FileCreator.createPathFromString(path), logGlobalFile.render());
    }

    private static String generateMethod(String level) {
        if (level.length() > 0) {
            level = Character.toUpperCase(level.charAt(0)) + level.substring(1);
        }
        String METHOD = "    public static boolean is<level>Enabled() {\n" +
                "        return true;\n" +
                "    }\n\n";
        ST method = new ST(METHOD);
        method.add("level", level);
        return method.render();
    }

}

