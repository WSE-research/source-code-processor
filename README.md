# Source code retrieval Annotation processor

## Goal

This annotation processor aims to parse source code at compile-time to provide it during runtime.

## Features
- Capture at compile‐time for each method:
  - fully qualified class name (`fqn`)
  - method name and return type
  - normalized parameter types
  - escaped source code as a single string
  - extracted Javadoc text
- Emit one JSON file per class under `jsons/` on the classpath
- Runtime reader (`JsonMethodFileReader`) to load and filter `MethodInfo`

## TypeNameUtils

Provides compile-time and runtime helpers to derive and normalize Java type names.

### Compile-time

- `getTypeName(TypeMirror type)`:  
  Returns the canonical name for a compile-time `TypeMirror`  
  • primitives & void (e.g. `int`)  
  • arrays (`Foo[]`)  
  • declared types with generics (`List<String>`)
- `normalize(String typeString)`:  
  Removes all whitespace from a type string for consistent matching.
- `normalize(List<String> types)`:  
  Applies `normalize` to each element in the list.

### Runtime (e.g. via Reflection)

- `getTypeName(Type type)`:  
  Resolves a runtime `java.lang.reflect.Type`, handling  
  • `ParameterizedType` (`List<String>`)  
  • `GenericArrayType` (`T[]`)  
  • `WildcardType` (`? extends Number`)  
  • type variables and raw `Class`
- `normalize(String)` & `normalize(List<String>)`:  
  Same normalization behavior as compile-time.

## Usage

### Implementation

To use this annotation processor for your application, add the dependency as well as the maven-compiler plugin with the annotationProcessor (source-code-processor) as annotationProcessorPath.

**Dependency:**

```xml
<dependency>
  <groupId>org.wseresearch</groupId>
  <artifactId>source-code-processor</artifactId>
  <version>[0.0.1,1.0)</version>
</dependency>
```

**Maven-compiler Plugin configuration:**

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.11.0</version>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>org.wseresearch</groupId>
        <artifactId>source-code-processor</artifactId>
        <version>0.0.1</version>
      </path>
    </annotationProcessorPaths>
    <generatedSourcesDirectory>${project.build.directory}/generated-sources/annotations</generatedSourcesDirectory>
  </configuration>
</plugin>
```

### In-Application Usage
- After compilation, each class’s methods are written as JSON in  
  `CLASS_OUTPUT/jsons/<fully.qualified.Name>.json`
- The JSON structure is:
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Methods",
  "type": "object",
  "properties": {
    "methods": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "fqn":           { "type": "string" },
          "methodName":    { "type": "string" },
          "returnType":    { "type": "string" },
          "sourceCode":    { "type": "string" },
          "javadoc":       { "type": "string" },
          "parameterTypes": {
            "type": "array",
            "items": { "type": "string" }
          }
        },
        "required": ["fqn","methodName","returnType","sourceCode","javadoc","parameterTypes"]
      }
    }
  },
  "required": ["methods"]
}
```

- At runtime, load and query methods via `JsonMethodFileReader`:
```java
MethodInfo info = JsonMethodFileReader.getMethod(
  "com.example.MyClass",
  "myMethod",
  List.of("java.lang.String")
);
String code = info.getSourceCode();
String doc  = info.getJavadoc();
```

## Schema Example

Given the following JSON (`eu.wdaqua.qanary.web.QanaryWebConfiguration.json`):
```json
{
  "methods": [
    {
      "fqn": "eu.wdaqua.qanary.web.QanaryWebConfiguration",
      "methodName": "addViewControllers",
      "returnType": "void",
      "sourceCode": "public void addViewControllers(ViewControllerRegistry registry) {      registry.addViewController(\"/static\").setViewName(\"static\");  }",
      "parameterTypes": [
        "org.springframework.web.servlet.config.annotation.ViewControllerRegistry"
      ],
      "javadoc": "Adds view controllers for static resources."
    }
  ]
}
```
#### Via JsonMethodFileReader

With this Processor, we also provide a FileReader (JsonMethodFileReader) that returns a MethodInfo object 
for a requested method. With `JsonMethodFileReader.getMethod(fqn, methodName, params)` the requested method
will be searched for.

#### Own implementation

If you rather implement the retrieval function yourself, you can take the following snippet as a starting point:

```java
    public static String getFileContent(String fqn) throws IOException {
    String resourcePath = "/jsons/" + fqn + ".json"; // Move the "jsons" to a final variable

    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
         BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
        return reader.lines().collect(Collectors.joining("\n"));
    }
}
```
