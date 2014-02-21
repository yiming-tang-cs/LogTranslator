package cz.muni.fi.ngmon.logtranslator.translator;

// https://theantlrguy.atlassian.net/wiki/display/ST4/StringTemplate+4+Documentation
public class FileGenerator {

    private String fileTemplate = "package %s;\n" +
            "\n" +
            "import cz.muni.fi.annotation.Namespace;\n" +
            "import cz.muni.fi.logger.AbstractNamespace;\n" +
            "\n" +
            "@Namespace\n" +
            "public class NAMESPACE_CLASSNAME extends AbstractNamespace {\n" +
            "\n" +
            "%s\n" +
            "}";
}
