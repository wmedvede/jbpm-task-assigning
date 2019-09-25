package org.jbpm.services.task.assigning.model;

import java.util.Objects;

public class TypedLabel {

    enum Type {
        SKILL,
        AFFINITY
    }

    private Type type;
    private String value;

    private TypedLabel(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public static TypedLabel newSkill(String value) {
        return new TypedLabel(Type.SKILL, value);
    }

    public static TypedLabel newAffinity(String value) {
        return new TypedLabel(Type.SKILL, value);
    }

    public String getValue() {
        return value;
    }

    public boolean isSkill() {
        return Type.SKILL == type;
    }

    public boolean isAffinity() {
        return Type.AFFINITY == type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypedLabel)) {
            return false;
        }
        TypedLabel that = (TypedLabel) o;
        return type == that.type &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
