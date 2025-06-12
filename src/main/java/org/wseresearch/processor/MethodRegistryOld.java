package org.wseresearch.processor;

import java.util.List;

import org.reflections.Reflections;

public class MethodRegistryOld {
    
    private static final List<MethodInfo> methods = loadAll();

    public static List<MethodInfo> getMethods() {
        return methods;
    }

    public static List<MethodInfo> loadAll() {
        Reflections reflections = new Reflections("generated");

        List<MethodInfo> methods = reflections.getSubTypesOf(MethodRegistryIF.class).stream()
            .map(cls -> {
                try {
                    MethodRegistryIF instance = cls.getDeclaredConstructor().newInstance();
                    instance.getMethods().forEach(methodInfo -> 
                        System.out.println(methodInfo.getSourceCode())
                    );
                    return (List<MethodInfo>) instance.getMethods();
                } catch (Exception e) {
                    throw new RuntimeException("Error instantiating " + cls.getName(), e);
                }
            })
            .flatMap(List::stream)
            .toList();

        return methods;
    }


}
