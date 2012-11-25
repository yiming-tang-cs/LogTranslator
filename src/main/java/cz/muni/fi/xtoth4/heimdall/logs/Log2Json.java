package cz.muni.fi.xtoth4.heimdall.logs;

import org.apache.log4j.Level;

public class Log2Json {
    private String type;
    private int severity;
    private Level level;
    private Payload payload;
    private String application;

    public Log2Json () {
    }

    public Log2Json(String type, Level level, int severity, Payload payload) {
        this.type = type;
        this.level = level;
        this.severity = severity;
        this.payload = payload;
        this.application = "org.apache.tomcat";
    }


    public void setType(String type) {
        this.type = type;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public int getSeverity() {
        return severity;
    }

    public Level getLevel() {
        return level;
    }

    public Payload getPayload() {
        return payload;
    }


    @Override
    public String toString() {
        return "[Event@" + this.hashCode() + "] {" +
                " type='" + type + '\'' +
                ", application='" + application + "'" +
                ", level='" + level + "'" +
                ", severity='" + severity + "'" +
                " " + payload +
                " }";
    }
}
