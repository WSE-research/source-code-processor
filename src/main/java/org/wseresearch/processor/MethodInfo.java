package org.wseresearch.processor;

import java.util.List;

public class MethodInfo {

    public String packageName, methodName, returnType, className, sourceCode;
    public List<String> parameterTypes;

    public MethodInfo(String packageName, String className, String methodName, String returnType, String sourceCode, List<String> parameterTypes) {
        this.packageName = packageName;
        this.className = className;
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "packageName='" + packageName + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", returnType='" + returnType + '\'' +
                ", sourceCode='" + sourceCode + '\'' +
                ", parameterTypes=" + parameterTypes +
                '}';
    }
}
