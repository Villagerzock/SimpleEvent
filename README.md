# Simple Events

This library allows you to add really simple events to Java, it uses a JavaC Plugin to do so, similar to Lombok this Library uses that Plugin to Generate Fields and Methods
All you do is write a Method, i personally like to make that method native so i dont have to add a body but thats on you
making the method native is not a problem due to the method itself nolonger existing in the final compiled version
you just need to annotate the method with **@Event**

there is not yet a Maven Repository so you need to Compile yourself and add the Jar as A library

## IntelliJ Plugin
There is an IntelliJ Plugin for Highligting in the IDE.
<div id="rc-page"><main><div style="display: inline-block;"><div class="ideButton--lDjVRlr rs-text-2 rs-text-2_theme_light" data-control-type="install" role="button" tabindex="0"><a class="ideText--wkfD_FU wt-text-2" href="https://plugins.jetbrains.com/plugin/29780-simpleevents-toolkit" rel="noopener noreferrer" target="_blank">Get from Marketplace</a></div></div></main></div>

## Installation
for the Installation its important to implement the Plugin the Right way so it gets Executed automatically, if you want to run your Program with for example the IntelliJ Run File System you'll need to Specify them in IntelliJ, if you use the IntelliJ Plugin said thing will be done Automatically as soon as my Library is installed in one or more Module/s, ofcourse only for that/those one or more Module/s

![](http://45.93.249.136:8080/api/badge/latest/releases/net/villagerzock/SimpleEvents/Compiler?name=SimpleEvents&prefix=v)

**Gradle:**
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
    compileOnly 'net.villagerzock.SimpleEvents:Compiler:${simple_events_version}'
    implementation 'net.villagerzock.SimpleEvents:Annotations:${simple_events_version}'
}
```
**Maven**
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
          <!-- JVM args for the forked javac process -->
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
          <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>

          <!-- javac option -->
          <arg>-Xplugin:net.villagerzock.Event</arg>
        </compilerArgs>
      </configuration>
    </plugin>
  </plugins>
</build>

```
```xml
<dependencies>
  <!-- Gradle: compileOnly -->
  <dependency>
    <groupId>net.villagerzock.SimpleEvents</groupId>
    <artifactId>Compiler</artifactId>
    <version>${simple_events_version}</version>
    <scope>provided</scope>
  </dependency>

  <!-- Gradle: implementation -->
  <dependency>
    <groupId>net.villagerzock.SimpleEvents</groupId>
    <artifactId>Annotations</artifactId>
    <version>${simple_events_version}</version>
  </dependency>
</dependencies>

```

> You will also need the Repository at http://45.93.249.136:8080/ with Allowed Insecure Protocol
