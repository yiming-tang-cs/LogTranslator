package org.ngmon.logger.logtranslator.generator;

import org.ngmon.logger.logtranslator.common.Utils;
import org.stringtemplate.v4.ST;

import java.util.Arrays;
import java.util.List;

/**
 * This LogGlobalgenerator class creates new LogGlobal file from template,
 * to handle all checking calls about enabled levels in current logging framework.
 * These methods are for example used in following statemnts:
 * if (log.isInfoEnabled()) {...}
 *
 * By default, all these checking methods return true;
 */
public class LogGlobalGenerator {

    private static final List<String> levels = Arrays.asList("trace", "debug", "info", "warn", "error", "fatal", "");
    private static final String path = Utils.getLogTranslatorGeneratedProject() + "src" + Utils.sep + "main" + Utils.sep + "java" + Utils.sep +
            Utils.getNgmonLogGlobal().replace(".", Utils.sep) + ".java";


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

        FileCreator.createDirectory(FileCreator.createPathFromString(path.substring(0, path.lastIndexOf(Utils.sep))));
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

    public static String getPath() {
        return path;
    }
}

