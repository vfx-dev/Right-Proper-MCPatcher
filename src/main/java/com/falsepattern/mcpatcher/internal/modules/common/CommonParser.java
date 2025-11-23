/*
 * Right Proper MCPatcher
 *
 * Copyright (C) 2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.mcpatcher.internal.modules.common;

import com.falsepattern.mcpatcher.Tags;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.biome.BiomeGenBase;

public class CommonParser {
    public static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + " Parser");
    public static final long PARSE_SENTINEL_VALUE = 0xFF_00000000L;

    private CommonParser() {
        throw new AssertionError("Utility class");
    }

    public static int parseInt(@Nullable String str) {
        return parseInt(str, -1);
    }

    public static int parseInt(@Nullable String str, int defVal) {
        if (str == null) {
            return defVal;
        } else {
            if (str.startsWith("(") && str.endsWith(")")) {
                str = str.substring(1, str.length() - 1);
            }
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                return defVal;
            }
        }
    }

    public static long parseIntSentinel(@NotNull String str) {
        if (str.startsWith("(") && str.endsWith(")")) {
            str = str.substring(1, str.length() - 1);
        }

        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ignored) {
            return PARSE_SENTINEL_VALUE;
        }
    }

    public static float parseFloat(@Nullable String str, float defVal) {
        if (str == null) {
            return defVal;
        } else {
            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException ignored) {
                return defVal;
            }
        }
    }

    public static boolean parseBoolean(@Nullable String str) {
        return parseBoolean(str, false);
    }

    public static boolean parseBoolean(@Nullable String str, boolean def) {
        return str != null ? str.equalsIgnoreCase("true") : def;
    }

    @Contract("!null -> !null; null -> null")
    public static @Nullable IntList parseInts(@Nullable String str, int minBound, int maxBound) {
        if (str == null) {
            return null;
        }
        IntList list = new IntArrayList();
        String[] intStrs = StringUtils.split(str, " ,");

        for (val intStr : intStrs) {
            val range = tokenizeRange(intStr);
            if (range == null) {
                LOG.warn("when parsing: {}", str);
                continue;
            }
            long min = (range.start != null) ? parseIntSentinel(range.start) : minBound;
            long max = (range.end != null) ? parseIntSentinel(range.end) : maxBound;
            if (min != PARSE_SENTINEL_VALUE && max != PARSE_SENTINEL_VALUE && min <= max) {
                for (int n = (int) min; n <= max; n++) {
                    list.add(n);
                }
            } else {
                LOG.warn("Invalid inverval: {}, when parsing: {}", intStr, str);
            }
        }
        return list;
    }

    public static IntRange.@Nullable List parseIntRanges(@Nullable String str) {
        if (str == null) {
            return null;
        }
        IntRange.List list = new IntRange.List();
        String[] parts = StringUtils.split(str, " ,");

        for (val part : parts) {
            val ri = parseIntRange(part);
            if (ri == null) {
                return null;
            }

            list.add(ri);
        }

        return list;
    }

    public static @Nullable IntRange parseIntRange(@Nullable String str) {
        if (str == null) {
            return null;
        }
        val range = tokenizeRange(str);
        if (range == null) {
            return null;
        }
        long min = (range.start != null) ? parseIntSentinel(range.start) : Integer.MIN_VALUE;
        long max = (range.end != null) ? parseIntSentinel(range.end) : Integer.MAX_VALUE;
        if (min != PARSE_SENTINEL_VALUE && max != PARSE_SENTINEL_VALUE) {
            return new IntRange((int) min, (int) max);
        } else {
            LOG.warn("Invalid range: {}", str);
            return null;
        }
    }

    private static @Nullable TokenizedRange tokenizeRange(String str) {
        String start = null;
        String end = null;
        val len = str.length();
        var inParen = false;
        var preDash = true;
        int dashOffset = -1;
        for (int i = 0; i < len; i++) {
            val ch = str.charAt(i);
            switch (ch) {
                case '(':
                    if (inParen) {
                        LOG.warn("Nested parentheses are not supported: {}", str);
                        return null;
                    }
                    inParen = true;
                    break;
                case ')':
                    if (!inParen) {
                        LOG.warn("Closing ) without a matching (: {}", str);
                        return null;
                    }
                    inParen = false;
                    break;
                case '-':
                    if (inParen) {
                        continue;
                    }
                    if (!preDash) {
                        LOG.warn("Multiple - separators: {}", str);
                        return null;
                    }
                    dashOffset = i + 1;
                    if (i != 0) {
                        start = str.substring(0, i);
                    }
                    continue;
            }
        }
        if (dashOffset == -1) {
            return new TokenizedRange(str, str);
        }
        if (dashOffset < len) {
            end = str.substring(dashOffset, len);
        }
        return new TokenizedRange(start, end);
    }

    @Contract("!null -> !null")
    public static @Nullable ObjectList<BiomeGenBase> parseBiomes(@Nullable String str) {
        if (str == null) {
            return null;
        }
        val biomeNames = StringUtils.split(str);
        val list = new ObjectArrayList<BiomeGenBase>();

        for (val biomeName : biomeNames) {
            val biome = findBiome(biomeName);
            if (biome == null) {
                LOG.warn("Biome not found: {}", biomeName);
            } else {
                list.add(biome);
            }
        }

        return list;
    }

    public static @Nullable BiomeGenBase findBiome(@NotNull String biomeName) {
        biomeName = biomeName.toLowerCase();
        val biomeList = BiomeGenBase.getBiomeGenArray();

        for (val biome : biomeList) {
            if (biome == null) {
                continue;
            }
            val name = biome.biomeName.replace(" ", "")
                                      .toLowerCase();
            if (name.equals(biomeName)) {
                return biome;
            }
        }

        return null;
    }

    @RequiredArgsConstructor
    private static class TokenizedRange {
        public final String start;
        public final String end;
    }
}
