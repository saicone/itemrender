/*
 * This file is part of mcode, licensed under the MIT License
 *
 * Copyright (c) Rubenicos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.saicone.item.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public enum MC {

    V_1_7(null, 3, 1, null, 1),
    V_1_7_1(null, 3, 1, null, 1),
    V_1_7_2(null, 4, 1, null, 1),
    V_1_7_3(null, 4, 1, null, 2),
    V_1_7_4(null, 4, 1, null, 2),
    V_1_7_5(null, 4, 1, null, 2),
    V_1_7_6(null, 5, 1, null, 3),
    V_1_7_7(null, 5, 1, null, 3),
    V_1_7_8(null, 5, 1, null, 3),
    V_1_7_9(null, 5, 1, null, 3),
    V_1_7_10(null, 5, 1, null, 4),

    V_1_8(null, 47, 1, null, 1),
    V_1_8_1(null, 47, 1, null, 1),
    V_1_8_2(null, 47, 1, null, 1),
    V_1_8_3(null, 47, 1, null, 2),
    V_1_8_4(null, 47, 1, null, 2),
    V_1_8_5(null, 47, 1, null, 2),
    V_1_8_6(null, 47, 1, null, 2),
    V_1_8_7(null, 47, 1, null, 2),
    V_1_8_8(null, 47, 1, null, 3),
    V_1_8_9(null, 47, 1, null, 3),

    V_1_9(169, 107, 2, null, 1),
    V_1_9_1(175, 108, 2, null, 1),
    V_1_9_2(176, 109, 2, null, 1),
    V_1_9_3(183, 110, 2, null, 2),
    V_1_9_4(184, 110, 2, null, 2),

    V_1_10(510, 210, 2, null, 1),
    V_1_10_1(511, 210, 2, null, 1),
    V_1_10_2(512, 210, 2, null, 1),

    V_1_11(819, 315, 3, null, 1),
    V_1_11_1(921, 316, 3, null, 1),
    V_1_11_2(922, 316, 3, null, 1),

    V_1_12(1139, 335, 3, null, 1),
    V_1_12_1(1241, 338, 3, 3, 1),
    V_1_12_2(1343, 340, 3, 3, 1),

    V_1_13(1519, 393, 4, 4, 1),
    V_1_13_1(1628, 401, 4, 4, 2),
    V_1_13_2(1631, 404, 4, 4, 2),

    V_1_14(1952, 477, 4, 4, 1),
    V_1_14_1(1957, 480, 4, 4, 1),
    V_1_14_2(1963, 485, 4, 4, 1),
    V_1_14_3(1968, 490, 4, 4, 1),
    V_1_14_4(1976, 498, 4, 4, 1),

    V_1_15(2225, 573, 5, 5, 1),
    V_1_15_1(2227, 575, 5, 5, 1),
    V_1_15_2(2230, 578, 5, 5, 1),

    V_1_16(2566, 735, 5, 5, 1),
    V_1_16_1(2567, 736, 5, 5, 1),
    V_1_16_2(2578, 751, 6, 6, 2),
    V_1_16_3(2580, 753, 6, 6, 2),
    V_1_16_4(2584, 754, 6, 6, 3),
    V_1_16_5(2586, 754, 6, 6, 3),

    V_1_17(2724, 755, 7, 7, 1),
    V_1_17_1(2730, 756, 7, 7, 1),

    V_1_18(2860, 757, 8, 8, 1),
    V_1_18_1(2866, 757, 8, 8, 1),
    V_1_18_2(2975, 758, 8, 9, 2),

    V_1_19(3105, 759, 9, 10, 1),
    V_1_19_1(3117, 760, 9, 10, 1),
    V_1_19_2(3120, 760, 9, 10, 1),
    V_1_19_3(3218, 761, 12, 10, 2),
    V_1_19_4(3337, 762, 13, 12, 3),

    V_1_20(3463, 763, 15, 15, 1),
    V_1_20_1(3465, 763, 15, 15, 1),
    V_1_20_2(3578, 764, 18, 18, 2),
    V_1_20_3(3698, 765, 22, 26, 3),
    V_1_20_4(3700, 765, 22, 26, 3),
    V_1_20_5(3837, 766, 32, 41, 4),
    V_1_20_6(3839, 766, 32, 41, 4),

    V_1_21(3953, 767, 34, 48, 1),
    V_1_21_1(3955, 767, 34, 48, 1),
    V_1_21_2(4080, 768, 42, 57, 2),
    V_1_21_3(4082, 768, 42, 57, 2),
    V_1_21_4(4189, 769, 46, 61, 3),
    V_1_21_5(4324, 770, 55, 71, 4),
    V_1_21_6(4435, 771, 63, 80, 5),
    V_1_21_7(4438, 772, 64, 81, 5),
    V_1_21_8(4440, 772, 64, 81, 5);

    public static final MC[] VALUES = values();
    private static final MC VERSION;

    static {
        MC serverVersion = null;
        try {
            serverVersion = fromString(Bukkit.getServer().getBukkitVersion());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // For some reason the server version cannot be parsed, so internal methods will be used
        if (serverVersion == null) {
            final String serverPackage = Bukkit.getServer().getClass().getPackage().getName();
            if (serverPackage.startsWith("org.bukkit.craftbukkit.v1_")) {
                // Bad accuracy
                serverVersion = from(MC::bukkitPackage, serverPackage.split("\\.")[3]);
            } else {
                serverVersion = from(MC::dataVersion, getDataVersion(serverPackage));
            }
        }
        VERSION = serverVersion;
    }

    private final Integer dataVersion;
    private final int protocol;
    private final int resourcePackFormat;
    private final Integer dataPackFormat;
    private final int revision;

    private final int major;
    private final int feature;
    private final int minor;

    private final float featRevision;
    private final int fullRevision;
    private final String bukkitPackage;

    private transient final boolean legacy;
    private transient final boolean flat;
    private transient final boolean universal;
    private transient final boolean component;

    @NotNull
    public static MC version() {
        return VERSION;
    }

    @NotNull
    public static MC latest() {
        return VALUES[VALUES.length - 1];
    }

    MC(@Nullable Integer dataVersion, int protocol, int resourcePackFormat, @Nullable Integer dataPackFormat, int revision) {
        this.dataVersion = dataVersion;
        this.protocol = protocol;
        this.resourcePackFormat = resourcePackFormat;
        this.dataPackFormat = dataPackFormat;
        this.revision = revision;

        final String[] split = this.name().split("_");

        this.major = Integer.parseInt(split[1]);
        this.feature = Integer.parseInt(split[2]);
        this.minor = split.length > 3 ? Integer.parseInt(split[3]) : 0;

        final String featureFormatted = feature >= 10 ? String.valueOf(feature) : "0" + feature;
        final String revisionFormatted = revision >= 10 ? String.valueOf(revision) : "0" + revision;

        this.featRevision = Float.parseFloat(feature + "." + revisionFormatted);
        this.fullRevision = Integer.parseInt(major + featureFormatted + revisionFormatted);
        this.bukkitPackage = "v" + major + "_" + feature + "_R" + revision;

        this.legacy = feature <= 12;
        this.flat = feature >= 13;
        this.universal = feature >= 17;
        this.component = dataVersion != null && dataVersion >= 3837;
    }

    public boolean isLegacy() {
        return legacy;
    }

    public boolean isFlat() {
        return flat;
    }

    public boolean isUniversal() {
        return universal;
    }

    public boolean isComponent() {
        return component;
    }

    public boolean isOlderThan(@NotNull MC version) {
        return this.ordinal() < version.ordinal();
    }

    public boolean isOlderThanOrEquals(@NotNull MC version) {
        return this.ordinal() <= version.ordinal();
    }

    public boolean isNewerThan(@NotNull MC version) {
        return this.ordinal() > version.ordinal();
    }

    public boolean isNewerThanOrEquals(@NotNull MC version) {
        return this.ordinal() >= version.ordinal();
    }

    public boolean isBetween(@NotNull MC version1, @NotNull MC version2) {
        return (this.ordinal() >= version1.ordinal() && this.ordinal() <= version2.ordinal()) || (this.ordinal() >= version2.ordinal() && this.ordinal() <= version1.ordinal());
    }

    @Nullable
    public Integer dataVersion() {
        return dataVersion;
    }

    public int protocol() {
        return protocol;
    }

    public int resourcePackFormat() {
        return resourcePackFormat;
    }

    @Nullable
    public Integer dataPackFormat() {
        return dataPackFormat;
    }

    public int revision() {
        return revision;
    }

    public int major() {
        return major;
    }

    public int feature() {
        return feature;
    }

    public int minor() {
        return minor;
    }

    public float featRevision() {
        return featRevision;
    }

    public int fullRevision() {
        return fullRevision;
    }

    @NotNull
    public String bukkitPackage() {
        return bukkitPackage;
    }

    @NotNull
    public String formatted() {
        return major + "." + feature + "." + minor;
    }

    @Override
    public String toString() {
        return name().substring(2).replace('_', '.');
    }

    @Nullable
    public static MC fromString(@NotNull String s) {
        final String[] split = s.replace("MC:", "").trim().split("\\.");
        if (split.length < 2) {
            return null;
        }
        final int major = Integer.parseInt(split[0]);
        final int feature;
        final int minor;
        if (split[1].contains("-") || split[1].contains("_")) {
            feature = Integer.parseInt(split[1].split("[-_]")[0]);
            minor = 0;
        } else {
            feature = Integer.parseInt(split[1]);
            minor = split.length > 2 ? Integer.parseInt(split[2].split("[-_]")[0]) : 0;
        }
        for (MC value : VALUES) {
            if (value.major == major && value.feature == feature && value.minor == minor) {
                return value;
            }
        }
        return null;
    }

    @Nullable
    public static <T> MC from(@NotNull Function<@NotNull MC, @Nullable T> valueFunction, @Nullable T t) {
        final boolean isNumber = t instanceof Number;
        for (int i = VALUES.length; i-- > 0; ) {
            final MC value = VALUES[i];
            final T obj = valueFunction.apply(value);
            if (isNumber && obj instanceof Number) {
                if (((Number) obj).doubleValue() >= ((Number) t).doubleValue()) {
                    return value;
                }
            } else if (Objects.equals(t, valueFunction.apply(value))) {
                return value;
            }
        }
        return null;
    }

    public static int compare(@NotNull MC version1, @NotNull MC version2) {
        return Integer.compare(version1.ordinal(), version2.ordinal());
    }

    @NotNull
    public static <T> Supplier<T> supply(@Nullable T t, @NotNull MC version, @Nullable T... def) {
        return () -> {
            if (MC.version().isNewerThanOrEquals(version)) {
                return t;
            } else {
                return def.length > 0 ? def[0] : null;
            }
        };
    }

    @NotNull
    public static <T> Supplier<T> supply(@Nullable T t1, @NotNull MC version1, @Nullable T t2, @NotNull MC version2, @Nullable T... def) {
        return () -> {
            if (MC.version().isNewerThanOrEquals(version1)) {
                return t1;
            } else if (MC.version().isNewerThanOrEquals(version2)) {
                return t2;
            } else {
                return def.length > 0 ? def[0] : null;
            }
        };
    }

    @NotNull
    public static <T> Supplier<T> supply(@Nullable T t1, @NotNull MC version1, @Nullable T t2, @NotNull MC version2, @Nullable T t3, @NotNull MC version3, @Nullable T... def) {
        return () -> {
            if (MC.version().isNewerThanOrEquals(version1)) {
                return t1;
            } else if (MC.version().isNewerThanOrEquals(version2)) {
                return t2;
            } else if (MC.version().isNewerThanOrEquals(version3)) {
                return t3;
            } else {
                return def.length > 0 ? def[0] : null;
            }
        };
    }

    @Nullable
    @Contract("!null, _ -> !null; _, !null -> !null")
    public static MC max(@Nullable MC version1, @Nullable MC version2) {
        if (version1 == null) {
            return version2;
        } else if (version2 == null) {
            return version1;
        }
        return version1.ordinal() > version2.ordinal() ? version1 : version2;
    }

    @Nullable
    @Contract("!null, _ -> !null; _, !null -> !null")
    public static MC min(@Nullable MC version1, @Nullable MC version2) {
        if (version1 == null) {
            return version2;
        } else if (version2 == null) {
            return version1;
        }
        return version1.ordinal() < version2.ordinal() ? version1 : version2;
    }

    private static int getDataVersion(String serverPackage) {
        try {
            final Class<?> magicNumbersClass = Class.forName(serverPackage + ".util.CraftMagicNumbers");
            final Object craftMagicNumbers = magicNumbersClass.getDeclaredField("INSTANCE").get(null);
            return (int) magicNumbersClass.getDeclaredMethod("getDataVersion").invoke(craftMagicNumbers);
        } catch (Throwable t) {
            t.printStackTrace();
            return Integer.MAX_VALUE;
        }
    }
}
