package com.motorcardgame.engine.rule.registry.describe;

import java.util.List;

/**
 * Describe un campo de configuración de una capacidad: su nombre JSON, tipo, si es obligatorio,
 * valor por defecto (si aplica) y valores posibles (solo para {@link FieldKind#ENUM}).
 */
public record FieldDescriptor(
        String name,
        FieldKind kind,
        boolean required,
        String defaultValue,
        List<String> enumValues) {

    public static FieldDescriptor zoneRef(String name) {
        return new FieldDescriptor(name, FieldKind.ZONE_REF, true, null, null);
    }

    public static FieldDescriptor optionalZoneRef(String name) {
        return new FieldDescriptor(name, FieldKind.ZONE_REF, false, null, null);
    }

    public static FieldDescriptor text(String name) {
        return new FieldDescriptor(name, FieldKind.TEXT, true, null, null);
    }

    public static FieldDescriptor optionalText(String name) {
        return new FieldDescriptor(name, FieldKind.TEXT, false, null, null);
    }

    public static FieldDescriptor integer(String name) {
        return new FieldDescriptor(name, FieldKind.INTEGER, true, null, null);
    }

    public static FieldDescriptor bool(String name) {
        return new FieldDescriptor(name, FieldKind.BOOLEAN, true, null, null);
    }

    public static FieldDescriptor scalar(String name) {
        return new FieldDescriptor(name, FieldKind.SCALAR, true, null, null);
    }

    public static FieldDescriptor enumField(String name, List<String> values, String defaultValue) {
        return new FieldDescriptor(name, FieldKind.ENUM, defaultValue == null, defaultValue, values);
    }

    public static FieldDescriptor condition(String name) {
        return new FieldDescriptor(name, FieldKind.CONDITION, true, null, null);
    }

    public static FieldDescriptor conditionList(String name) {
        return new FieldDescriptor(name, FieldKind.CONDITION_LIST, true, null, null);
    }

    public static FieldDescriptor action(String name) {
        return new FieldDescriptor(name, FieldKind.ACTION, true, null, null);
    }

    public static FieldDescriptor actionList(String name) {
        return new FieldDescriptor(name, FieldKind.ACTION_LIST, true, null, null);
    }
}
