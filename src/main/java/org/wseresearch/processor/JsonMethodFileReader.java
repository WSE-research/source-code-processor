package org.wseresearch.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMethodFileReader {

    private static final String BASE_PATH = "jsons";
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JsonMethodFileReader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Reads content from a JSON file for a specific method.
     * 
     * @param fqn Fully qualified name of the class (i.e., package name + class name)
     * @param methodName The method name
     * @param paramTypes The parameter types
     * @return The JSON content as a string, or null if file not found
     * @throws IOException If an I/O error occurs
     */
    public static String getFileInfo(String fqn) throws IOException {
        // First, try to load the class file JSON
        String resourcePath = "/" + BASE_PATH + "/" + fqn + ".json";
        InputStream is = JsonMethodFileReader.class.getClassLoader().getResourceAsStream(resourcePath);
          
        try (InputStream stream = is;
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public static MethodInfo getMethod(String fqn, String methodName, List<String> params) {
        try {
            String jsonString = getFileInfo(fqn);
            JsonNode rootNode = objectMapper.readTree(jsonString);
            List<MethodInfo> methods = objectMapper.readerForListOf(MethodInfo.class).readValue(rootNode.get("methods"));
            
            // Handle null params case
            List<String> requestedParams = params != null ? params : new ArrayList<>();
            
            return methods.stream()
                    .filter(method -> method.getMethodName().equals(methodName) && 
                            (requestedParams.isEmpty() || method.getParameterTypes().equals(requestedParams)))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            logger.error("Error reading method info for fqn: {}, method: {}, params: {}", fqn, methodName, params, e);
            return null;
        }
    }
}
