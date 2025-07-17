package com.saicone.item.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class FieldLookup {

    FieldLookup() {
    }

    private static boolean matches(@NotNull Class<?> type, @Nullable Object object) {
        if (object instanceof Class<?>) {
            return type.equals(object);
        } else if (object instanceof String) {
            return type.getName().endsWith((String) object);
        } else if (object instanceof String[]) {
            for (int i = 0; i < ((String[]) object).length; i++) {
                if (type.getName().endsWith(((String[]) object)[i])) {
                    return true;
                }
            }
            return false;
        } else if (object instanceof Iterable) {
            for (Object o : (Iterable<?>) object) {
                if (matches(type, o)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @NotNull
    public static Optional<Field> find(@NotNull Class<?> clazz, @Nullable Object type, @NotNull String... aliases) {
        for (@NotNull String alias : aliases) {
            try {
                final Field field = clazz.getDeclaredField(alias);
                if (matches(field.getType(), type)) {
                    return Optional.of(field);
                }
            } catch (NoSuchFieldException ignored) { }
        }
        if (type != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (matches(field.getType(), type)) {
                    return Optional.of(field);
                }
            }
        }
        return Optional.empty();
    }

    @NotNull
    public static MethodHandle getter(@NotNull Class<?> clazz, @Nullable Object type, @NotNull String... aliases) {
        return find(clazz, type, aliases).map(field -> {
            try {
                field.setAccessible(true);
                return MethodHandles.lookup().unreflectGetter(field);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }).orElseThrow(() -> new AssertionError("Cannot find any field of " + Arrays.toString(aliases) + " with type " + (type instanceof Object[] ? Arrays.toString((Object[]) type) : type) + " inside " + clazz.getName()));
    }

    @NotNull
    public static MethodHandle setter(@NotNull Class<?> clazz, @Nullable Object type, @NotNull String... aliases) {
        return find(clazz, type, aliases).map(field -> {
            try {
                field.setAccessible(true);
                return MethodHandles.lookup().unreflectSetter(field);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }).orElseThrow(() -> new AssertionError("Cannot find any field of " + Arrays.toString(aliases) + " with type " + (type instanceof Object[] ? Arrays.toString((Object[]) type) : type) + " inside " + clazz.getName()));
    }
}
