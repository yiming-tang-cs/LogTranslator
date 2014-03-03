package cz.muni.fi.ngmon.logtranslator.generator;

import java.util.List;

// https://theantlrguy.atlassian.net/wiki/display/ST4/StringTemplate+4+Documentation
public class FileGenerator {

    private String packageDeclaration;
    private String namespaceName;
    private String methodName;
    private List<String> variableNameTypeList;
    private List<String> variableNameList;


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




    public void setPackageDeclaration(String packageDeclaration) {
        this.packageDeclaration = packageDeclaration;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setVariableNameTypeList(List<String> variableNameTypeList) {
        this.variableNameTypeList = variableNameTypeList;
    }

    public void setVariableNameList(List<String> variableNameList) {
        this.variableNameList = variableNameList;
    }

    public void setFileTemplate(String fileTemplate) {
        this.fileTemplate = fileTemplate;
    }

    public void setMethodTemplate(String methodTemplate) {
        this.methodTemplate = methodTemplate;
    }
}
