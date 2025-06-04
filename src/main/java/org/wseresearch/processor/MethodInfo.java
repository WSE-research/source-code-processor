package org.wseresearch.processor;

public class MethodInfo {
    public final String packageName, className, methodName, sourceCode;

    public MethodInfo(String packageName, String className, String methodName, String sourceCode) {
        this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;
        this.sourceCode = sourceCode;
    }

    public String getPackageName() { return packageName; }
    public String getClassName()   { return className; }
    public String getMethodName()  { return methodName; }
    public String getSourceCode()  { return sourceCode; }
}
