package de.is24.deadcode4j.analyzer.webxml;

/**
 * Representation of a {@code context-param} or {@code init-param} node.
 * {@code Param} is a value-based class.
 *
 * @since 2.1.0
 */
public class Param {
    private final String name;
    private final String value;

    /**
     * Creates a new {@code Param} object.
     * @param name the text of the {@code param-name} node.
     * @param value the text of the {@code param-value} node.
     */
    public Param(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the text of the {@code param-name} node.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the text of the {@code param-value} node.
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!getClass().isInstance(o)) {
            return false;
        }

        Param param = (Param) o;

        if (name != null ? !name.equals(param.name) : param.name != null) {
            return false;
        }
        return !(value != null ? !value.equals(param.value) : param.value != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Param{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}