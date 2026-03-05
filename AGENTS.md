# AGENTS.md - Coding Agent Guidelines

This document provides essential information for agentic coding systems working in this repository. It covers build commands, testing procedures, code style guidelines, and project conventions.

## Project Overview

This is a Java 17+ Maven project focused on advanced Java features including:
- Java Agent technology (static/dynamic loading)
- Concurrency programming patterns
- Java Records with Spring Boot integration
- Serialization examples (Jackson/Fastjson2)
- IO operations and file handling
- Spring Framework integration (Web, Data JPA, Properties)

The project uses Spring Boot 3.3.0 as the parent and includes various Spring starters.

## Build Commands

### Basic Build Operations

```bash
# Clean and compile
mvn clean compile

# Clean and package (creates JAR files)
mvn clean package

# Clean, compile, and run all tests
mvn clean test

# Clean, compile, package, and install to local repo
mvn clean install

# Skip tests during build
mvn clean package -DskipTests

# Run with specific Java version (if needed)
mvn clean package -Dmaven.compiler.source=17 -Dmaven.compiler.target=17
```

### Running Specific Applications

```bash
# Run Record demo application
java -cp target/classes com.advancedjava.interview.records.RecordDemoApp

# Run concurrency showcase
java -cp target/classes com.advancedjava.interview.concurrency.ConcurrencyFeatureShowcaseApp

# Run serialization demo
java -cp target/classes com.advancedjava.interview.serialization.JsonSerializationDemoApp

# Run Spring Boot application (if enabled)
mvn spring-boot:run
```

## Test Commands

### Running Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=CandidateProfileTest

# Run a specific test method
mvn test -Dtest=CandidateProfileTest#shouldTrimNameAndDefensivelyCopySkills

# Run tests matching pattern
mvn test -Dtest=*Record*Test

# Run tests with specific JVM arguments
mvn test -DargLine="-Xmx1g"

# Skip specific tests
mvn test -Dtest=!CandidateProfileTest
```

### Manual Test Compilation (for understanding)

The README shows manual compilation examples that may be useful for agents to understand dependencies:

```bash
# Compile and run record tests manually
mkdir -p /tmp/record-test-classes
javac -d /tmp/record-test-classes \
  -cp "$HOME/.m2/repository/junit/junit/4.13.1/junit-4.13.1.jar:$HOME/.m2/repository/org/hamcrest/hamcrest/2.2/hamcrest-2.2.jar" \
  src/main/java/com/advancedjava/interview/records/CandidateProfile.java \
  src/main/java/com/advancedjava/interview/records/CandidateProfileClassic.java \
  src/main/java/com/advancedjava/interview/records/InterviewResult.java \
  src/main/java/com/advancedjava/interview/records/InterviewResultClassic.java \
  src/test/java/com/advancedjava/interview/records/CandidateProfileTest.java
java -cp "/tmp/record-test-classes:$HOME/.m2/repository/junit/junit/4.13.1/junit-4.13.1.jar:$HOME/.m2/repository/org/hamcrest/hamcrest/2.2/hamcrest-2.2.jar" \
  org.junit.runner.JUnitCore com.advancedjava.interview.records.CandidateProfileTest
```

### Testing Framework

- **Primary**: JUnit 4.13.1
- **Assertions**: JUnit Assert + Hamcrest matchers
- **Test Runner**: Maven Surefire Plugin
- **Test Location**: `src/test/java`
- **No test resources**: Tests don't use external resources

## Code Style Guidelines

### General Principles

1. **Follow existing patterns**: Always examine nearby files before making changes
2. **Java 17+ features encouraged**: Use records, sealed classes, pattern matching, etc.
3. **Spring Boot conventions**: Follow Spring Boot best practices when working with Spring components
4. **Educational focus**: Code should be clear and demonstrate concepts effectively

### Package Structure

```
com.advancedjava
├── agent/                    # Java Agent implementations
├── concurrency/              # Concurrency examples
│   ├── lock/                 # Lock implementations
│   ├── forkjoin/             # Fork/Join framework
│   └── thread/               # Thread management
├── demo/                     # Demo applications
├── io/                       # IO operations
│   └── file/                 # File-specific operations
├── interview/                # Interview-focused examples
│   ├── records/              # Java Records examples
│   ├── serialization/        # Serialization examples  
│   ├── concurrency/          # Concurrency interview demos
│   └── frameworks/           # Framework integrations
│       ├── spring/           # Spring Boot examples
│       ├── springdata/       # Spring Data JPA examples
│       └── jdk/              # JDK-specific examples
└── springai/                 # Spring AI examples (currently commented)
```

### Imports and Dependencies

**Required imports order** (based on observed patterns):
1. Standard Java packages (`java.*`, `javax.*`)
2. Third-party libraries (`org.junit`, `com.alibaba.fastjson2`, etc.)
3. Project-specific imports (`com.advancedjava.*`)

**Key dependencies to use**:
- **Testing**: `org.junit.Assert`, `org.junit.Test`
- **Collections**: Standard Java collections (ArrayList, List.of, etc.)
- **JSON**: Fastjson2 (`com.alibaba.fastjson2`) and Jackson (when needed)
- **Concurrency**: Standard `java.util.concurrent` packages
- **Spring**: Only when working in framework-related packages

**Avoid adding new dependencies** unless absolutely necessary and consistent with the educational purpose.

### Formatting Conventions

**Indentation**: 4 spaces (no tabs)
**Line length**: No strict limit, but prefer reasonable lengths
**Braces**: Always use braces, even for single statements
**Spacing**: 
- Space after keywords (`if `, `for `, `while `)
- Space around operators (`a + b`, not `a+b`)
- No space between method name and parentheses (`method()`)
- Space after commas in parameter lists

**Example format**:
```java
public class Example {
    public void demonstrateFormatting() {
        if (someCondition) {
            for (int i = 0; i < 10; i++) {
                String result = processItem(items.get(i));
                if (result != null && result.length() > 0) {
                    handleResult(result);
                }
            }
        }
    }
}
```

### Naming Conventions

**Classes**: PascalCase - `CandidateProfile`, `ConcurrencyFeatureShowcaseApp`
**Methods**: camelCase - `shouldTrimNameAndDefensivelyCopySkills()`, `run()`
**Variables**: camelCase - `candidateProfile`, `sourceSkills`
**Constants**: UPPER_SNAKE_CASE - Not commonly used, prefer `final` variables
**Test methods**: Should start with `should` or describe behavior clearly - `shouldUseValueBasedEquality()`

**Package names**: All lowercase with dot-separated words - `com.advancedjava.interview.records`

### Types and Declarations

**Records usage**:
- Use records for immutable data carriers
- Include validation in compact constructors
- Use defensive copying for mutable fields
- Provide static factory methods when appropriate

**Classic POJOs**:
- Include validation in constructors/setters
- Implement proper `equals()`, `hashCode()`, `toString()`
- Use defensive copying for mutable fields
- Provide meaningful business methods

**Generics**: Use when appropriate, especially in collection-heavy code
**Var keyword**: Acceptable for complex generic type inference

### Error Handling

**Exception types**:
- Use standard Java exceptions (`IllegalArgumentException`, `UnsupportedOperationException`)
- Create custom exceptions only when they add significant value
- Prefer unchecked exceptions for programming errors

**Validation patterns**:
```java
// Constructor validation
public CandidateProfile(String name, int yearsOfExperience, List<String> skills) {
    if (name == null || name.trim().isEmpty()) {
        throw new IllegalArgumentException("Name cannot be null or empty");
    }
    if (yearsOfExperience < 0) {
        throw new IllegalArgumentException("Years of experience cannot be negative");
    }
    // Defensive copy
    this.skills = List.copyOf(skills);
}

// Method validation  
public void addSkill(String skill) {
    if (skill == null || skill.trim().isEmpty()) {
        throw new IllegalArgumentException("Skill cannot be null or empty");
    }
    // implementation
}
```

**Test error handling**:
- Use custom `assertThrows` helper method for exception testing
- Always include meaningful failure messages in assertions

### Documentation

**Class-level Javadoc**: Required for public classes, especially in demo applications
**Method-level Javadoc**: Required for public methods that aren't self-explanatory
**Field-level Javadoc**: Generally not needed for simple fields

**Chinese comments**: Some files contain Chinese comments for educational purposes. Maintain consistency within each file.

**README updates**: When adding significant new functionality, update the README.md structure and examples.

## Special Considerations

### Java Agent Development

When working with Agent code (`com.advancedjava.agent`):
- Understand both static (`premain`) and dynamic (`agentmain`) loading
- Use Javassist for bytecode manipulation
- Handle attach permissions appropriately
- The main agent class is `MethodAgentMain.java`

### Concurrency Examples

Concurrency demos should:
- Clearly demonstrate the concept being taught
- Include proper error handling and resource cleanup
- Use realistic scenarios where possible
- Follow Java concurrency best practices

### Spring Integration

When working with Spring components:
- Use proper Spring annotations (`@RestController`, `@ConfigurationProperties`, etc.)
- Follow Spring Boot conventions for configuration
- Use constructor injection over field injection when possible
- Respect Spring's component lifecycle

### Record Compatibility

Records should be designed to work well with:
- JSON serialization (Fastjson2 and Jackson)
- Spring Data JPA projections
- Standard Java reflection
- Traditional POJO equivalents (for comparison)

## Verification Commands

After making changes, always verify with:

```bash
# Compile check
mvn compile

# Test execution  
mvn test

# Full build
mvn package

# Specific test verification (replace TestClassName)
mvn test -Dtest=TestClassName
```

## Performance and Security

**Performance considerations**:
- Use appropriate concurrent collections for multi-threaded scenarios
- Avoid unnecessary object creation in loops
- Use `List.of()` and similar for immutable collections

**Security considerations**:
- Never log sensitive information
- Validate all inputs in public APIs
- Use defensive copying for mutable parameters
- Handle resources properly (close streams, etc.)

## Additional Notes

- **No build tools beyond Maven**: Don't introduce Gradle, Bazel, etc.
- **Java version locked**: Must remain compatible with Java 17+
- **Educational purpose**: Code clarity trumps micro-optimizations
- **Consistency over cleverness**: Prefer readable, maintainable code
- **Test coverage**: New functionality should include appropriate tests

This repository serves as both a learning resource and demonstration of advanced Java features. All contributions should maintain this educational focus while following established conventions.