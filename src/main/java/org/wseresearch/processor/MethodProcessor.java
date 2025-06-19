package org.wseresearch.processor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class MethodProcessor extends AbstractProcessor {

    private Map<String, List<MethodInfo>> packageMethodMap = new HashMap<>();
    private List<MethodInfo> methods = new ArrayList<>();
    private final String SOURCE_CODE_NOT_FOUND_FOR_CLASS = "No source code available";
    private final String SOURCE_CODE_NOT_FOUNT_FOR_METHOD = "Source code not found";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getRootElements()) {
            if (e.getKind() == ElementKind.CLASS) {
                for (Element enclosed : e.getEnclosedElements()) {
                    if (enclosed.getKind() == ElementKind.METHOD) {
                        ExecutableElement method = (ExecutableElement) enclosed;
                        String fqn = ((TypeElement) e).getQualifiedName().toString();
                        String methodName = method.getSimpleName().toString();
                        String returnType = method.getReturnType().toString();
                        List<String> parameterTypes = method.getParameters()
                                .stream()
                                .map(p -> p.asType().toString())
                                .collect(Collectors.toList());
                        String sourceCode = escapeSourceCode(getSourceCodeForMethod(fqn, methodName, parameterTypes));
                        MethodInfo methodInfo = new MethodInfo(fqn, methodName, returnType, "\"" + sourceCode + "\"", parameterTypes);
                        addMethodToMap(methodInfo);
                        this.methods.add(methodInfo);
                    }
                }
            }
        }

        if (roundEnv.processingOver() && !methods.isEmpty()) {
            generateFileRegistry();
        }
        return false;
    }

    private void addMethodToMap(MethodInfo methodInfo) {
        String fqn = methodInfo.getFqn();
        if (!packageMethodMap.containsKey(fqn)) {
            ArrayList<MethodInfo> methods = new ArrayList<>();
            methods.add(methodInfo);
            packageMethodMap.put(fqn, methods);
        } else {
            packageMethodMap.get(fqn).add(methodInfo);
        }
    }

    private void generateFileRegistry() {
        packageMethodMap.forEach((k, v) -> {
            try {
                FileObject file = processingEnv.getFiler().createResource(
                        StandardLocation.CLASS_OUTPUT,
                        "",
                        "jsons/" + k + ".json"
                );

                try (Writer writer = file.openWriter()) {
                    writer.write("{");
                    writer.write("\"methods\": [");
                    for (int i = 0; i < v.size(); i++) {
                        MethodInfo method = v.get(i);
                        writer.write(method.getJsonRepresentation());
                        if (i < v.size() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write("]");
                    writer.write("}");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Escapes source code for inclusion in a Java string literal, handling multi-line content
     * and special characters.
     */
    private String escapeSourceCode(String sourceCode) {
        // Escape backslashes and quotes, then replace newlines with \n
        return sourceCode
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "  ");
    }

    /**
     * Finds the source file for a given method with the use of JavaParser.
     *
     * @param methodName Name of the method without '()'
     * @return Source code as string for requested method
     */
    public String getSourceCodeForMethod(String fqn, String methodName, List<String> parameterTypes) {
        Path sourceFilePath = getSourceFilePath(fqn);
        CompilationUnit cu;
        try {
            // Transform source file to compilationUnit
            cu = com.github.javaparser.StaticJavaParser.parse(sourceFilePath.toFile());
        } catch (IOException e) {
            return SOURCE_CODE_NOT_FOUND_FOR_CLASS;
        }
        Optional<MethodDeclaration> methodDeclaration = findMethodInCompilationUnit(cu, methodName, parameterTypes);
        if (methodDeclaration.isPresent()) {
            MethodDeclaration md = methodDeclaration.get();
            return (md.getDeclarationAsString(true, true, true) + " " + md.getBody().orElse(null));
        }
        return SOURCE_CODE_NOT_FOUNT_FOR_METHOD;
    }

    /**
     * @param cu             CompilationUnit of the class file where the requested method should be accessible
     * @param methodName     Name of the method (i.e. getA - not getA())
     * @param parameterTypes Parameter signature to check for overloaded methods
     * @return MethodDeclaration
     */
    private Optional<MethodDeclaration> findMethodInCompilationUnit(CompilationUnit cu, String methodName, List<String> parameterTypes) {
        // Filter all methods with the passed methodName
        List<MethodDeclaration> possibleMethods = cu.findAll(com.github.javaparser.ast.body.MethodDeclaration.class).stream()
                .filter(md -> md.getNameAsString().equals(methodName))
                .collect(Collectors.toList());

        if (possibleMethods.size() == 1) {
            return Optional.of(possibleMethods.get(0)); // No overloaded methods, return the only occurrence

        } else if (possibleMethods.size() > 1) {
            // If there are multiple methods with the same name, we match by parameter types
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
     * Given the fully qualified name of a class (i.e. packages and class) get the path of it
     *
     * @param fqn Fully Qualified Name of the class file (e.g., "com.example.MyClass")
     * @return Path of source file
     */
    public Path getSourceFilePath(String fqn) {
        String baseDir = "src/main/java";
        String relativePath = fqn.replace('.', '/') + ".java";
        return Path.of(baseDir, relativePath);
    }

}
