package org.wseresearch.processor;

import java.util.List;

import org.reflections.Reflections;
import org.slf4j.Logger;

// TODO: Alternative approach: Via JSON and resources
public class MethodRegistry {
    
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MethodRegistry.class);
    private static final List<MethodInfo> methods = loadAll();

    public static List<MethodInfo> getMethods() {
        return methods;
    }

    private static List<MethodInfo> loadAll() {
        Reflections reflections = new Reflections("generated");

        return reflections.getSubTypesOf(MethodRegistryIF.class).stream()
            .map(cls -> {
                try {
                    MethodRegistryIF instance = cls.getDeclaredConstructor().newInstance();
                    return (List<MethodInfo>) instance.getMethods();
                } catch (Exception e) {
                    throw new RuntimeException("Error instantiating " + cls.getName(), e);
                }
            })
            .flatMap(List::stream)
            .toList();
    }


    
}
