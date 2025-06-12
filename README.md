# Source code retrieval Annotation processor

## Goal

This annotation processor aims to parse source code at compile-time to provide it during runtime.

## Usage

### Implementation

To use this annotation processor for your application, add the dependency as well as the maven-compiler plugin with the annotationProcessor as annotationProcessorPath.

**Dependency:**

```xml
<dependency>
  <groupId>org.wseresearch</groupId>
  <artifactId>source-code-processor</artifactId>
  <version>0.0.1</version>
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
