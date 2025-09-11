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
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.biome.BiomeGenBase;

public class CommonParser {
    public static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + " Parser");

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
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                return defVal;
            }
        }
    }

    public static boolean parseBoolean(@Nullable String str) {
        return str != null && str.equalsIgnoreCase("true");
    }

    @Contract("!null -> !null; null -> null")
    public static @Nullable IntList parseInts(@Nullable String str) {
        if (str == null) {
            return null;
        }
        IntList list = new IntArrayList();
        String[] intStrs = StringUtils.split(str, " ,");

        for (val intStr : intStrs) {
            if (intStr.contains("-")) {
                String[] subStrs = StringUtils.split(intStr, "-");
                if (subStrs.length != 2) {
                    LOG.warn("Invalid interval: {}, when parsing: {}", intStr, str);
                } else {
                    int min = parseInt(subStrs[0], -1);
                    int max = parseInt(subStrs[1], -1);
                    if (min >= 0 && max >= 0 && min <= max) {
                        for (int n = min; n <= max; ++n) {
                            list.add(n);
                        }
                    } else {
                        LOG.warn("Invalid interval: {}, when parsing: {}", intStr, str);
                    }
                }
            } else {
                int val = parseInt(intStr, -1);
                if (val < 0) {
                    LOG.warn("Invalid number: {}, when parsing: {}", intStr, str);
                } else {
                    list.add(val);
                }
            }
        }

        return list;
    }

    public static IntRange.@Nullable List parseIntRanges(@Nullable String str) {
        if (str == null) {
            return null;
        } else {
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
    }

    public static @Nullable IntRange parseIntRange(@Nullable String str) {
        if (str == null) {
            return null;
        } else if (str.indexOf(45) >= 0) {
            String[] parts = StringUtils.split(str, "-");
            if (parts.length != 2) {
                LOG.warn("Invalid range: {}", str);
                return null;
            } else {
                int min = parseInt(parts[0], -1);
                int max = parseInt(parts[1], -1);
                if (min >= 0 && max >= 0) {
                    return new IntRange(min, max);
                } else {
                    LOG.warn("Invalid range: {}", str);
                    return null;
                }
            }
        } else {
            int val = parseInt(str, -1);
            if (val < 0) {
                LOG.warn("Invalid integer: {}", str);
                return null;
            } else {
                return new IntRange(val, val);
            }
        }
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
}
