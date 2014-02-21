package cz.muni.fi.ngmon.logtranslator.translator;

public class Changer {

    private String content = null;
    private boolean changed = false;
    private String filePath;
    private Changer parent;

    public Changer() {
        this.changed = false;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Changer getParent() {
        return parent;
    }

    public void setParent(Changer parent) {
        this.parent = parent;
    }
}
