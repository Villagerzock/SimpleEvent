# Simple Events

**Simple Events** adds *real* events to Java using a **javac compiler plugin** – similar in spirit to Lombok, but focused entirely on events.

You declare an event using a normal Java method annotated with `@Event`.
At compile time, the method is **removed** and replaced with generated fields, listener interfaces, and emit logic.

No runtime magic.
No bytecode weaving at runtime.
Everything is generated at **compile time**.

---

## Example

```java
@Event
public static native void foo(String value);
```

The method **does not exist** in the final bytecode.

Instead, the compiler generates an event object which you can use like this:

```java
foo.addListener(v -> System.out.println("Listener: " + v));
foo.emit("Hello");
```

Declaring the method as `native` is recommended (but optional) to avoid writing a body.
This is safe because the method is removed entirely during compilation.

---

## IntelliJ IDEA Plugin

An IntelliJ plugin is available and **highly recommended**.

It provides:

- Syntax highlighting
- CodeVision hints (listener / emitter counts)
- Navigation to listeners and emit calls
- Error reporting for invalid usage

Get it from the JetBrains Marketplace:
https://plugins.jetbrains.com/plugin/29780-simpleevents-toolkit

---

## Installation

**Important**

Simple Events uses a **javac compiler plugin**.
It must be configured correctly so the compiler executes it automatically.

If you use IntelliJ IDEA *without* the plugin, you must configure the compiler options manually.
If the IntelliJ plugin is installed, this is handled automatically per module. (With atleast Plugin Version 1.0.2 or newer)

---

![](http://45.93.249.136:8080/api/badge/latest/releases/net/villagerzock/SimpleEvents/Compiler?name=SimpleEvents&prefix=v)

---

## Gradle

```groovy
tasks.withType(JavaCompile).configureEach {
    options.fork = true
    options.forkOptions.jvmArgs += [
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
    ]
    options.compilerArgs += [
        "-Xplugin:net.villagerzock.Event",
    ]
}
```

```groovy
dependencies {
    compileOnly "net.villagerzock.SimpleEvents:Compiler:${simple_events_version}"
    implementation "net.villagerzock.SimpleEvents:Annotations:${simple_events_version}"
}
```

---

## Maven

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.13.0</version>

      <configuration>
        <fork>true</fork>

        <compilerArgs>
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
          <arg>-Xplugin:net.villagerzock.Event</arg>
        </compilerArgs>
      </configuration>
    </plugin>
  </plugins>
</build>
```

```xml
<dependencies>
  <dependency>
    <groupId>net.villagerzock.SimpleEvents</groupId>
    <artifactId>Compiler</artifactId>
    <version>${simple_events_version}</version>
    <scope>provided</scope>
  </dependency>

  <dependency>
    <groupId>net.villagerzock.SimpleEvents</groupId>
    <artifactId>Annotations</artifactId>
    <version>${simple_events_version}</version>
  </dependency>
</dependencies>
```

> You will also need the Repository at http://45.93.249.136:8080/ with Allowed Insecure Protocol
