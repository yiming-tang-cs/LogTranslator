package cz.muni.fi.xtoth4.heimdall.logs;


public class Log2Json {
    private Log2JsonType type;
    private int severity;
    private Log2JsonLevel level;
    private String payload;
    private String application;


    public Log2Json(Log2JsonType type, Log2JsonLevel level, int severity, String payload) {
        this.type = type;
        this.level = level;
        this.severity = severity;
        this.payload = payload;
        this.application = "org.apache.tomcat";
    }


    public void setType(Log2JsonType type) {
        this.type = type;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public void setLevel(Log2JsonLevel level) {
        this.level = level;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Log2JsonType getType() {
        return type;
    }

    public int getSeverity() {
        return severity;
    }

    public Log2JsonLevel getLevel() {
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
