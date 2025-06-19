package org.wseresearch.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MethodRegistry implements MethodRegistryIF {

    private List<MethodInfo> methods = setAllMethodsFromFiles();
    private ObjectMapper objectMapper = new ObjectMapper();

    private List<MethodInfo> setAllMethodsFromFiles() {
        List<MethodInfo> methods = new ArrayList<>();
        File directory = new File("json");
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    methods.addAll(setMethodsFromFile(file));
                }
            }
        }
        return methods;
    }

    private List<MethodInfo> setMethodsFromFile(File file) {
        // Parse file to JSONObject
        try (FileReader fileReader = new FileReader(file)) {
            JsonNode root = objectMapper.readTree(fileReader);
            JsonNode methods = root.get("methods");
            return objectMapper.readerForListOf(MethodInfo.class).readValue(methods);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<MethodInfo> getMethods() {
        return methods;
    }

    // TODO: Include packageName consideration
    public MethodInfo getMethod(String packageName, String methodName, List<String> params) {
        for (MethodInfo method : this.methods) {
            if (method.getMethodName().equals(methodName)) {
                // Check method.getParams() with params for equality
                if (method.getParameterTypes().equals(params))
                    return method;
            } else continue;
        }
        return null;
    }
/*
    public static MethodInfo getMethod(String packageName, String methodName, List<String> params) throws FileNotFoundException {
        try (InputStream is = MethodRegistry.class.getClassLoader().getResourceAsStream(BASE_PATH + "/" + packageName + ".json")) {
            JsonNode root;
            if (packagenameJsonnodeMap.containsKey(packageName)) {
                root = packagenameJsonnodeMap.get(packageName);
            } else {
                root = mapper.readTree(is);
                packagenameJsonnodeMap.put(packageName, root);
            }
            JsonNode methodsNode = root.get("methods");
            List<MethodInfo> possibleMethods = new ArrayList<>();
            for (JsonNode methodNode : methodsNode) {
                if (methodNode.get("methodName").asText().equals(methodName)) {
                    possibleMethods.add(mapper.convertValue(methodNode, MethodInfo.class));
                }
            }
            if (possibleMethods.size() == 1) {
                return possibleMethods.get(0);
            } else if (possibleMethods.size() > 1) {
                for (MethodInfo methodNode : possibleMethods) {
                    if (methodNode.getParameterTypes().equals(params))
                        return methodNode;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

 */

}
