package net.villagerzock.testing;

import net.villagerzock.Event;

public class Main {
    @Event
    public static native void foo(String bar);

    public static void main(String[] args) {
        System.out.println("Starting...");
        foo.addListener(bar ->{
            System.out.println("Running Event foo and value is: " + bar);
        });
        foo.emit("Hii");
        foo.emit("Hiiiiiii");
    }
}
