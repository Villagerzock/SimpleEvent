package net.villagerzock;

import java.util.ArrayList;
import java.util.List;

public class AnnotationLibEventImpl<T> {
    protected final List<T> listeners = new ArrayList<>();

    public void addListener(T listener){
        if (!listeners.contains(listener)) listeners.add(listener);
    }
    public void removeListener(T listener){
        listeners.remove(listener);
    }
}
