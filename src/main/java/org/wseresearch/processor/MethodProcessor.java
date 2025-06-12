package org.wseresearch.processor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.auto.service.AutoService;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.*;
import java.lang.reflect.Field;
import java.nio.file.Path;

import org.checkerframework.checker.units.qual.s;
import org.wseresearch.processor.MethodInfo;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class MethodProcessor extends AbstractProcessor {

    private List<MethodInfo> methods = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getRootElements()) {
            if (e.getKind() == ElementKind.CLASS) {
                for (Element enclosed : e.getEnclosedElements()) {
                    if (enclosed.getKind() == ElementKind.METHOD) {
                        ExecutableElement method = (ExecutableElement) enclosed;
                        String packageName = processingEnv.getElementUtils().getPackageOf(method).toString();
                        String className = ((TypeElement) e).getQualifiedName().toString();
                        String methodName = method.getSimpleName().toString();
                        String returnType = method.getReturnType().toString();
                        List<String> parameterTypes = method.getParameters()
                            .stream()
                            .map(p -> p.asType().toString())
                            .collect(Collectors.toList());
                        String sourceCode = escapeSourceCode(getSourceCodeForMethod(className, methodName, parameterTypes));
                        MethodInfo methodInfo = new MethodInfo(packageName, className, methodName, returnType, "\"" + sourceCode + "\"", parameterTypes);
                        this.methods.add(methodInfo);
                    }
                }
            }
        }

        if (roundEnv.processingOver() && !methods.isEmpty()) {
            generateRegistry();
        }

        return false;
    }

    private void generateRegistry() {
        try {
            String randomSuffix = UUID.randomUUID().toString().replace("-", "");
//            JavaFileObject file = processingEnv.getFiler().createSourceFile("generated.MethodRegistry" + randomSuffix);
            JavaFileObject file = processingEnv.getFiler().createSourceFile("org.wseresearch.processor.MethodRegistry", (Element[]) null);
            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                out.println("package org.wseresearch.processor;");
                out.println("import java.util.*;");
                out.println("import org.wseresearch.processor.MethodInfo;");
//                out.println("import org.wseresearch.processor.MethodRegistryIF;");
                //out.println("public class MethodRegistry implements MethodRegistryIF {");
                out.printf("public class MethodRegistry {");
                out.println("  public static List<MethodInfo> getMethods() {");
                out.println("    return List.of(");
                for (int i = 0; i < methods.size(); i++) {
                    MethodInfo m = methods.get(i);
                    String paramTypes = m.parameterTypes.stream()
                        .map(pt -> "\"" + escape(pt) + "\"")
                        .collect(Collectors.joining(", ", "List.of(", ")"));
                    out.printf("      new MethodInfo(\"%s\", \"%s\",\"%s\", \"%s\", %s, %s)%s%n",
                            escape(m.packageName), escape(m.className), escape(m.methodName), 
                            escape(m.returnType), m.getSourceCode(), paramTypes,
                            (i < methods.size() - 1 ? "," : ""));
                }
                out.println("    );");
                out.println("  }");
                out.println("}");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String escape(String code) {
        if (code == null) return "null";
        return code.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    
    /**
     * Escapes source code for inclusion in a Java string literal, handling multi-line content
     * and special characters.
     */
    private String escapeSourceCode(String sourceCode) {
        if (sourceCode == null) {
            return "No source code available";
        }
        // Escape backslashes and quotes, then replace newlines with \n
        return sourceCode
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "")
            .replace("\n", "  ");
    }
    
    /**
     * Finds the source file for a given method with the use of JavaParser.
     * @param method
     * @return
     */
    public String getSourceCodeForMethod(String fqn, String methodName, List<String> parameterTypes) {
        Path sourceFilePath = getSourceFilePath(fqn);
        CompilationUnit cu;
        try {
            cu = com.github.javaparser.StaticJavaParser.parse(sourceFilePath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
            return "Error";
        }
        Optional<MethodDeclaration> methodDeclaration = findMethodInCompilationUnit(cu, methodName, parameterTypes);
        if (methodDeclaration.isPresent()) {
            MethodDeclaration md = methodDeclaration.get();
            return (md.getDeclarationAsString(true, true, true) + " " + md.getBody().orElse(null));
        }
        return "Error2";
    }

    private Optional<MethodDeclaration> findMethodInCompilationUnit(CompilationUnit cu, String methodName, List<String> parameterTypes) {
        List<MethodDeclaration> possibleMethods = cu.findAll(com.github.javaparser.ast.body.MethodDeclaration.class).stream()
            .filter(md -> md.getNameAsString().equals(methodName))
            .collect(Collectors.toList());

        if (possibleMethods.size() == 1) {
            return Optional.of(possibleMethods.get(0));
        } else if (possibleMethods.size() > 1) {
            // If there are multiple methods with the same name, we can try to match by parameter types
            for (MethodDeclaration md : possibleMethods) {
                List<String> paramTypes = md.getParameters().stream()
                    .map(p -> p.getType().asString())
                    .collect(Collectors.toList());
                if (paramTypes.equals(parameterTypes)) {
                    return Optional.of(md);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 
     * @param fqn Fully Qualified Name of the class file (e.g., "com.example.MyClass")
     * @return
     */
    public Path getSourceFilePath(String fqn) {
        // Assumes source files are in "src/main/java" relative to project root
        String baseDir = "src/main/java";
        String relativePath = fqn.replace('.', '/') + ".java";
        return Path.of(baseDir, relativePath);
    }

}
