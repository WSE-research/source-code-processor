package org.wseresearch.processor;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class MethodInfo {

    public String fqn, methodName, returnType, sourceCode, javadoc;
    public List<String> parameterTypes;

    // Default constructor for Jackson deserialization
    public MethodInfo() {
        this.parameterTypes = new ArrayList<>();
    }

    public MethodInfo(String fqn, String methodName, String returnType, String sourceCode, String javadoc, List<String> parameterTypes) {
        this.fqn = fqn;
        this.methodName = methodName;
        this.returnType = returnType;
        this.sourceCode = sourceCode;
        this.javadoc = javadoc;
        this.parameterTypes = parameterTypes != null ? parameterTypes : new ArrayList<>();
    }

    public MethodInfo(String fqn, String methodName, String returnType, List<String> parameterTypes) {
        this.fqn = fqn;
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes != null ? parameterTypes : new ArrayList<>();
    }

    public String getJavadoc() {
        return javadoc;
    }

    public void setJavadoc(String javadoc) {
        this.javadoc = javadoc;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "FQN='" + fqn + '\'' +
                ", methodName='" + methodName + '\'' +
                ", returnType='" + returnType + '\'' +
                ", sourceCode='" + sourceCode + '\'' +
                ", javadoc='" + javadoc + '\'' +
                ", parameterTypes=" + parameterTypes +
                '}';
    }

    public String getJsonRepresentation() {
        StringWriter writer = new StringWriter();
        writer.write("{");
        writer.write("\"fqn\": \"" + this.getFqn() + "\",");
        writer.write("\"methodName\": \"" + this.getMethodName() + "\",");
        writer.write("\"returnType\": \"" + this.getReturnType() + "\",");
        writer.write("\"sourceCode\": " + this.getSourceCode() + ",");
        writer.write("\"javadoc\": " + this.getJavadoc() + ",");
        writer.write("\"parameterTypes\": [");
        List<String> params = this.getParameterTypes();
        for (int j = 0; j < params.size(); j++) {
            writer.write("\"" + params.get(j) + "\"");
            if (j < params.size() - 1) {
                writer.write(",");
            }
        }
        writer.write("]");
        writer.write("}");
        return writer.toString();
    }
}
