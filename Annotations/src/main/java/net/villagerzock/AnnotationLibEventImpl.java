package net.villagerzock;

import java.util.concurrent.CopyOnWriteArrayList;

public class AnnotationLibEventImpl<T> {
    protected final CopyOnWriteArrayList<T> listeners = new CopyOnWriteArrayList<>();

    public void addListener(T listener){
        if (listener == null) return;
        if (!listeners.contains(listener)) listeners.add(listener);
    }
    public void removeListener(T listener){
        if (listener == null) return;
        listeners.remove(listener);
    }

    protected void catchException(Throwable throwable, String eventName){
        System.err.println("An error occured while running event " + eventName);
        throwable.printStackTrace();
    }
}
