package cz.muni.fi.ngmon.logtranslator.translator;

import org.antlr.v4.misc.OrderedHashMap;

import java.util.Map;

public class Variable {

    // Mapping of variableName : <variableProperties>
    private Map<String, Properties> variableList = new OrderedHashMap<>();
    private String fileName;

    public Map<String, Properties> getVariableList() {
        return variableList;
    }

    public void putVariableList(String variableName, Properties properties) {
        variableList.put(variableName, properties);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "variableList=" + variableList +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    class Properties {
        private String name; // ?
        private String type;
        private int lineNumber;
        private int startPosition;
        private int stopPosition;
        private int fileStartPosition;
        private int fileStopPosition;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public int getStartPosition() {
            return startPosition;
        }

        public void setStartPosition(int startPosition) {
            this.startPosition = startPosition;
        }

        public int getStopPosition() {
            return stopPosition;
        }

        public void setStopPosition(int stopPosition) {
            this.stopPosition = stopPosition;
        }

        public int getFileStartPosition() {
            return fileStartPosition;
        }

        public void setFileStartPosition(int fileStartPosition) {
            this.fileStartPosition = fileStartPosition;
        }

        public int getFileStopPosition() {
            return fileStopPosition;
        }

        public void setFileStopPosition(int fileStopPosition) {
            this.fileStopPosition = fileStopPosition;
        }

        @Override
        public String toString() {
            return "Properties{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", lineNumber=" + lineNumber +
                    ", startPosition=" + startPosition +
                    ", stopPosition=" + stopPosition +
                    ", fileStartPosition=" + fileStartPosition +
                    ", fileStopPosition=" + fileStopPosition +
                    '}';
        }
    }
}
