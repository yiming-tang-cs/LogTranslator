package org.ngmon.logger.logtranslator.generator;

import org.ngmon.logger.logtranslator.common.Log;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class represents a new NGMON method which will be added
 * into appropriate NGMON's namespace.
 */
public class NGMONMethod implements Comparable {

    private LinkedHashMap<String, String> formalParameters;
    private String methodName;


    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    private Log log;

    public NGMONMethod(String methodName, LinkedHashMap<String, String> formalParameters) {
        this.methodName = methodName;
        this.formalParameters = formalParameters;
    }


    public Map<String, String> getFormalParameters() {
        return formalParameters;
    }

    public String getMethodName() {
        return methodName;
    }

    /**
     * If two methods have the same formal argument list of types AND
     * same method name, method is considered to be the same.
     * <p/>
     * We assume that 2 various NGMON methods from 2 namespaces would never be called.
     *
     * @param obj other object to compare with
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        NGMONMethod other = (NGMONMethod) obj;

        return (this.getMethodName().equals(other.getMethodName()) &&
            this.formalParameters.size() == other.formalParameters.size() &&
            this.formalParameters.values().equals(other.formalParameters.values()));
    }

    @Override
    public int hashCode() {
        int result = formalParameters.hashCode();
        result = 31 * result + methodName.hashCode();
        return result;
    }

    /**
     * -1 = smaller object closer to a (a <- z)
     * 0 = equal object
     * 1 = bigger object, closer to z (a -> z)
     *
     * @param obj object to compare
     * @return appropriate int value
     */
    @Override
    public int compareTo(Object obj) {
        NGMONMethod other = (NGMONMethod) obj;
        if (this.getMethodName().equals(other.getMethodName())) {
            if (this.getFormalParameters().keySet().size() == other.getFormalParameters().size()) {
                // equal name & size
                if (this.getFormalParameters().values().equals(other.getFormalParameters().values())) {
                    return 0;
                }
            } else if (this.getFormalParameters().keySet().size() < other.getFormalParameters().size()) {
                return -1;
            } else {
                return 1;
            }
        }
        return this.getMethodName().compareTo(other.getMethodName());
    }

    @Override
    public String toString() {
        return "LOG." + methodName + "(" + formalParameters +")";

    }
}
