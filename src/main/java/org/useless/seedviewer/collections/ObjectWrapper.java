package org.useless.seedviewer.collections;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ObjectWrapper<T> {
    protected List<ValueListener<T>> listeners = new ArrayList<>();
    protected T value;
    public ObjectWrapper(T initialValue) {
        this.value = initialValue;
    }

    public void set(T value) {
        this.value = value;
        for (ValueListener<T> listener : listeners) {
            listener.onValueChanged(value);
        }
    }

    public T get() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectWrapper<?> that = (ObjectWrapper<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        if (value == null) return "null";
        return value.toString();
    }

    public ObjectWrapper<T> addChangeListener(@NotNull ValueListener<T> listener) {
        Objects.requireNonNull(listener);
        listeners.add(listener);
        return this;
    }

    public interface ValueListener<T> {
        void onValueChanged(T newValue);
    }
}
