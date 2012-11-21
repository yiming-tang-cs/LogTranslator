package cz.muni.fi.xtoth4.heimdall.logs;


public class Log2Json {
    private String type;
    private String severity;
    private String level;
    private String payload;
    private String application;


    public Log2Json(String type, String level, String severity, String payload) {
        this.type = type;
        this.level = level;
        this.severity = severity;
        this.payload = payload;
        this.application = "org.apache.tomcat";
    }


    public void setType(String type) {
        this.type = type;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getType() {

        return type;
    }

    public String getSeverity() {
        return severity;
    }

    public String getLevel() {
        return level;
    }

    public String getPayload() {
        return payload;
    }


    @Override
    public String toString() {
        return "[Event@" + this.hashCode() + "] {" +
                " type='" + type + '\'' +
                ", application='" + application + "'" +
                ", level='" + level + "'" +
                ", severity='" + severity + "'" +
                ", payload='" + payload + "'" +
                '}';
    }
}
