import com.google.auto.service.AutoService;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.*;
import java.lang.reflect.Field;
import org.wseresearch.processor.MethodInfo;
import java.util.UUID;

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
                        methods.add(new MethodInfo(
                            processingEnv.getElementUtils().getPackageOf(e).toString(),
                            e.getSimpleName().toString(),
                            method.getSimpleName().toString(),
                            method.toString() // dummy source
                        ));
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
            JavaFileObject file = processingEnv.getFiler().createSourceFile("generated.MethodRegistry" + randomSuffix);
            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                out.println("package generated;");
                out.println("import java.util.*;");
                out.println("import org.wseresearch.processor.MethodInfo;");
                out.println("import org.wseresearch.processor.MethodRegistryIF;");
                out.printf("public class MethodRegistry%s implements MethodRegistryIF {%n", randomSuffix);
                out.println("  @Override");
                out.println("  public List<MethodInfo> getMethods() {");
                out.println("    return List.of(");
                for (int i = 0; i < methods.size(); i++) {
                    MethodInfo m = methods.get(i);
                    out.printf("      new MethodInfo(\"%s\", \"%s\", \"%s\", \"%s\")%s%n",
                        m.packageName, m.className, m.methodName, escape(m.sourceCode),
                        i == methods.size() - 1 ? "" : ",");
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
        return code.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
