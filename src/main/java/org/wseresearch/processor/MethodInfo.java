package org.wseresearch.processor;

import java.util.List;

public class MethodInfo {

    public String fqn, methodName, returnType, sourceCode;
    public List<String> parameterTypes;

    public MethodInfo(String fqn, String methodName, String returnType, String sourceCode, List<String> parameterTypes) {
        this.fqn = fqn;
        this.methodName = methodName;
        this.returnType = returnType;
        this.sourceCode = sourceCode;
        this.parameterTypes = parameterTypes;
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
                ", parameterTypes=" + parameterTypes +
                '}';
    }
}
