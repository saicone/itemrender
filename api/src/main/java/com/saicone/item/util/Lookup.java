package com.saicone.item.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

@ApiStatus.Internal
public class Lookup {

    Lookup() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle) {
        try {
            return (T) handle.invoke();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1) {
        try {
            return (T) handle.invoke(arg1);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2) {
        try {
            return (T) handle.invoke(arg1, arg2);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4, @Nullable Object arg5) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4, arg5);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4, @Nullable Object arg5, @Nullable Object arg6) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4, arg5, arg6);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4, @Nullable Object arg5, @Nullable Object arg6, @Nullable Object arg7) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4, arg5, arg6, arg7);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4, @Nullable Object arg5, @Nullable Object arg6, @Nullable Object arg7, @Nullable Object arg8) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4, @Nullable Object arg5, @Nullable Object arg6, @Nullable Object arg7, @Nullable Object arg8, @Nullable Object arg9) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4, @Nullable Object arg5, @Nullable Object arg6, @Nullable Object arg7, @Nullable Object arg8, @Nullable Object arg9, @Nullable Object arg10) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
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

    private static boolean matches(@NotNull Class<?>[] types, @NotNull Object[] objects) {
        for (int i = 0; i < types.length; i++) {
            if (!matches(types[i], objects[i])) {
                return false;
            }
        }
        return true;
    }

    @NotNull
    public static MethodHandle constructor(@NotNull Class<?> clazz, @NotNull Object... parameters) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == parameters.length && matches(constructor.getParameterTypes(), parameters)) {
                try {
                    constructor.setAccessible(true);
                    return MethodHandles.lookup().unreflectConstructor(constructor);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        }
        throw new AssertionError("Cannot find any constructor of " + Arrays.toString(parameters) + " inside " + clazz.getName());
    }

    @NotNull
    public static Optional<Field> field(@NotNull Class<?> clazz, @Nullable Object type, @NotNull String... aliases) {
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
        return field(clazz, type, aliases).map(field -> {
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
        return field(clazz, type, aliases).map(field -> {
            try {
                field.setAccessible(true);
                return MethodHandles.lookup().unreflectSetter(field);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }).orElseThrow(() -> new AssertionError("Cannot find any field of " + Arrays.toString(aliases) + " with type " + (type instanceof Object[] ? Arrays.toString((Object[]) type) : type) + " inside " + clazz.getName()));
    }
}
