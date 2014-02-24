package cz.muni.fi.ngmon.logtranslator.translator;

public class FileInfo {

    private String filepath;
    private String namespace;
    private String namespaceClass;
    private String packageName;
//    private String

    public FileInfo(String filepath) {
        this.filepath = filepath;
        // figure out File Information -- using fallback now
        setNamespace("cz.muni.test");
        setPackageName("blabla.hadoop.bla.baf");
    }

    public String getFilepath() {
        return filepath;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getNamespaceEnd() {
        StringBuffer stringBuffer = new StringBuffer(namespace.substring(namespace.lastIndexOf(".")+1));
        stringBuffer.replace(0, 1, stringBuffer.substring(0, 1).toUpperCase());
        return stringBuffer.toString();
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespaceClass() {
        return namespaceClass;
    }

    public void setNamespaceClass(String namespaceClass) {
        this.namespaceClass = namespaceClass;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
