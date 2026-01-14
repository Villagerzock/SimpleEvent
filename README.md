# Simple Events

This library allows you to add really simple events to Java, it uses a JavaC Plugin to do so, similar to Lombok this Library uses that Plugin to Generate Fields and Methods
All you do is write a Method, i personally like to make that method native so i dont have to add a body but thats on you
making the method native is not a problem due to the method itself nolonger existing in the final compiled version
you just need to annotate the method with @Event

there is not yet a Maven Repository so you need to Compile yourself and add the Jar as A library

## Installation

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
