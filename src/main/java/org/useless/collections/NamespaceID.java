package org.useless.collections;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class NamespaceID {
    private final @NotNull String combined;
    public final @NotNull String namespace;
    public final @NotNull String value;

    public NamespaceID(@NotNull String namespace, @NotNull String value) {
        validateNamespace(namespace);
        validateValue(value);

        this.combined = namespace + ":" + value;
        this.namespace = namespace;
        this.value = value;
    }

    public NamespaceID(@NotNull String formattedString) {
        validateFormattedString(formattedString);

        int colonIndex = formattedString.indexOf(":");
        String _namespace = formattedString.substring(0, colonIndex);
        String _value = formattedString.substring(colonIndex + 1);

        validateNamespace(_namespace);
        validateValue(_value);

        this.combined = formattedString;
        this.namespace = _namespace;
        this.value = _value;
    }

    private void validateNamespace(String namespace) {
        Objects.requireNonNull(namespace, "Namespace cannot be null!");
        if (namespace.isEmpty()) throw new IllegalArgumentException("Namespace cannot be empty!");
        if (namespace.contains(":")) throw new IllegalArgumentException("Namespace cannot contain ':'!");
    }

    private void validateValue(String value) {
        Objects.requireNonNull(value, "Value cannot be null!");
        if (value.isEmpty()) throw new IllegalArgumentException("Value cannot be empty!");
        if (value.contains(":")) throw new IllegalArgumentException("Value cannot contain ':'!");
    }

    private void validateFormattedString(String formatted) {
        Objects.requireNonNull(formatted, "String must not be null!");
        if (!formatted.contains(":")) throw new IllegalArgumentException("String must contain a separator of ':'");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o.getClass() == this.getClass()) {
            return combined.equals(((NamespaceID) o).combined);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return combined.hashCode();
    }

    @Override
    public String toString() {
        return combined;
    }
}
