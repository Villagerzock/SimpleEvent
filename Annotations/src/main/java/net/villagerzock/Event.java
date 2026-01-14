package net.villagerzock;

public @interface Event {
    boolean value() default false;
}
