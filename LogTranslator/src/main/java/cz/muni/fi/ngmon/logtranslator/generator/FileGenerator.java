package cz.muni.fi.ngmon.logtranslator.generator;

import cz.muni.fi.ngmon.logtranslator.common.LogFile;

import java.util.List;
import java.util.Map;

// https://theantlrguy.atlassian.net/wiki/display/ST4/StringTemplate+4+Documentation
public class FileGenerator {

    private String packageDeclaration;
    private String namespaceName;
//    private List<LogFile.Variable> variables;
    private Map<String, List<LogFile.Variable>> methodData;



    private String fileTemplate =
            "package %packageDeclaration;\n" +
            "\n" +
            "import cz.muni.fi.annotation.Namespace;\n" +
            "import cz.muni.fi.logger.AbstractNamespace;\n" +
            "\n" +
            "@Namespace\n" +
            "public class %namespace extends AbstractNamespace {\n" +
            "\n" +
            "%methodTemplate\n" +
            "}";


    /*  public AbstractNamespace MOUNT_MNT_PATH_CLIENT(String path, String client) {
		    return log(path, client);
	    } */
    private String methodTemplate =
            "\tpublic AbstractNamespace %methodName(%variableNameTypeList) {\n" +
            "\t\treturn log(%variableNameList);\n" +
            "\t}";


    public FileGenerator() {

    }


    public void setPackageDeclaration(String packageDeclaration) {
        this.packageDeclaration = packageDeclaration;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }


}
