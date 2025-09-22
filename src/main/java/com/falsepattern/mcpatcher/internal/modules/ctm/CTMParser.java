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

package com.falsepattern.mcpatcher.internal.modules.ctm;

import com.falsepattern.mcpatcher.internal.modules.common.CommonParser;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;

import static com.falsepattern.mcpatcher.internal.modules.common.CommonParser.LOG;

public final class CTMParser {
    private CTMParser() {
        throw new AssertionError("Utility class");
    }

    @Contract("!null, _ -> !null")
    public static @Nullable ObjectList<String> parseMatchTiles(@Nullable String str, @NotNull String basePath) {
        if (str == null) {
            return null;
        }
        ObjectList<String> names = new ObjectArrayList<>(StringUtils.split(str));

        val len = names.size();
        for (int i = 0; i < len; ++i) {
            String iconName = names.get(i);
            if (iconName.endsWith(".png")) {
                iconName = iconName.substring(0, iconName.length() - 4);
            }

            iconName = fixResourcePath(iconName, basePath);
            names.set(i, iconName);
        }

        return names;
    }

    public static @NotNull String parseName(@NotNull String path) {
        String str = path;
        int pos = path.lastIndexOf(47);
        if (pos >= 0) {
            str = path.substring(pos + 1);
        }

        int pos2 = str.lastIndexOf(46);
        if (pos2 >= 0) {
            str = str.substring(0, pos2);
        }

        return str;
    }

    public static @NotNull String parseBasePath(@NotNull String path) {
        int pos = path.lastIndexOf(47);
        return pos < 0 ? "" : path.substring(0, pos);
    }

    @Contract("!null, _ -> !null")
    public static @Nullable ObjectList<String> parseTileNames(@Nullable String str, @NotNull String basePath) {
        if (str == null) {
            return null;
        }
        val list = new ObjectArrayList<String>();
        val iconStrs = StringUtils.split(str, " ,");

        for (val iconStr : iconStrs) {
            if (iconStr.contains("-")) {
                val subStrs = StringUtils.split(iconStr, "-");
                if (subStrs.length == 2) {
                    int min = CommonParser.parseInt(subStrs[0], -1);
                    int max = CommonParser.parseInt(subStrs[1], -1);
                    if (min >= 0 && max >= 0) {
                        if (min > max) {
                            LOG.warn("Invalid interval: {}, when parsing: {}", iconStr, str);
                            continue;
                        }

                        for (int n = min; n <= max; ++n) {
                            list.add(String.valueOf(n));
                        }
                        continue;
                    }
                }
            }

            list.add(iconStr);
        }

        val len = list.size();

        for (int i = 0; i < len; ++i) {
            var iconName = list.get(i);
            iconName = fixResourcePath(iconName, basePath);
            if (!iconName.startsWith(basePath) &&
                !iconName.startsWith("textures/") &&
                !iconName.startsWith("mcpatcher/")) {
                iconName = basePath + "/" + iconName;
            }

            if (iconName.endsWith(".png")) {
                iconName = iconName.substring(0, iconName.length() - 4);
            }

            val pathBlocks = "textures/blocks/";
            if (iconName.startsWith(pathBlocks)) {
                iconName = iconName.substring(pathBlocks.length());
            }

            if (iconName.startsWith("/")) {
                iconName = iconName.substring(1);
            }

            list.set(i, iconName);
        }

        return list;
    }

    public static @NotNull Symmetry parseSymmetry(@Nullable String str) {
        if (str == null) {
            return Symmetry.None;
        } else {
            str = str.trim();
            if (str.equals("opposite")) {
                return Symmetry.Opposite;
            } else if (str.equals("all")) {
                return Symmetry.All;
            } else {
                LOG.warn("Unknown symmetry: {}", str);
                return Symmetry.None;
            }
        }
    }

    public static int parseFacesMask(@Nullable String str) {
        if (str == null) {
            return Side.MASK_ALL;
        } else {
            String[] faceStrs = StringUtils.split(str, " ,");
            int facesMask = 0;

            for (val faceStr : faceStrs) {
                facesMask |= parseFaceMask(faceStr);
            }

            return facesMask;
        }
    }

    public static int parseFaceMask(@NotNull String str) {
        str = str.toLowerCase();
        switch (str) {
            case "bottom":
                return Side.Bottom.mask;
            case "top":
                return Side.Top.mask;
            case "north":
                return Side.North.mask;
            case "south":
                return Side.South.mask;
            case "east":
                return Side.East.mask;
            case "west":
                return Side.West.mask;
            case "sides":
                return Side.MASK_SIDES;
            case "all":
                return Side.MASK_ALL;
            default:
                LOG.warn("Unknown face: {}", str);
                return Side.MASK_UNKNOWN;
        }
    }

    public static @NotNull Connect parseConnect(@Nullable String str) {
        if (str == null) {
            return Connect.None;
        } else {
            str = str.trim();
            switch (str) {
                case "block":
                    return Connect.Block;
                case "tile":
                    return Connect.Tile;
                case "material":
                    return Connect.Material;
                default:
                    LOG.warn("Unknown connect: {}", str);
                    return Connect.Unknown;
            }
        }
    }

    public static @Nullable ObjectList<@NotNull Block> parseBlockIds(@Nullable String str) {
        if (str == null) {
            return null;
        } else {
            ObjectList<@NotNull Block> list = new ObjectArrayList<>();
            String[] idStrs = StringUtils.split(str, " ,");

            for (val idStr : idStrs) {
                if (idStr.contains("-")) {
                    String[] subStrs = StringUtils.split(idStr, "-");
                    if (subStrs.length != 2) {
                        LOG.warn("Invalid interval: {}, when parsing: {}", idStr, str);
                    } else {
                        int min = parseBlockId(subStrs[0]);
                        int max = parseBlockId(subStrs[1]);
                        if (min >= 0 && max >= 0 && min <= max) {
                            for (int n = min; n <= max; ++n) {
                                val block = Block.getBlockById(n);
                                if (block != null) {
                                    list.add(block);
                                }
                            }
                        } else {
                            LOG.warn("Invalid interval: {}, when parsing: {}", idStr, str);
                        }
                    }
                } else {
                    int val = parseBlockId(idStr);
                    if (val < 0) {
                        LOG.warn("Invalid block ID: {}, when parsing: {}", idStr, str);
                    } else {
                        val block = Block.getBlockById(val);
                        if (block != null) {
                            list.add(block);
                        }
                    }
                }
            }

            return list;
        }
    }

    public static int parseBlockId(@Nullable String blockStr) {
        int val = CommonParser.parseInt(blockStr, -1);
        if (val >= 0) {
            return val;
        } else {
            Block block = Block.getBlockFromName(blockStr);
            return block != null ? Block.getIdFromBlock(block) : -1;
        }
    }

    public static @NotNull Method parseMethod(@Nullable String str) {
        if (str == null) {
            return Method.Ctm;
        }
        str = str.trim();
        switch (str) {
            case "ctm":
            case "glass":
                return Method.Ctm;
            case "ctm_compact":
                return Method.Compact;
            case "horizontal":
            case "bookshelf":
                return Method.Horizontal;
            case "vertical":
                return Method.Vertical;
            case "top":
                return Method.Top;
            case "random":
                return Method.Random;
            case "repeat":
                return Method.Repeat;
            case "fixed":
                return Method.Fixed;
            case "horizontal+vertical":
            case "h+v":
                return Method.HorizontalVertical;
            case "vertical+horizontal":
            case "v+h":
                return Method.VerticalHorizontal;
            default:
                LOG.warn("Unknown method: {}", str);
                return Method.None;
        }
    }

    private static @NotNull String fixResourcePath(@NotNull String path, @NotNull String basePath) {
        String strAssMc = "assets/minecraft/";
        if (path.startsWith(strAssMc)) {
            return path.substring(strAssMc.length());
        }
        if (path.startsWith("./")) {
            path = path.substring(2);
            if (!basePath.endsWith("/")) {
                basePath = basePath + "/";
            }

            return basePath + path;
        }
        if (path.startsWith("/~")) {
            path = path.substring(1);
        }

        String strMcpatcher = "mcpatcher/";
        if (path.startsWith("~/")) {
            path = path.substring(2);
            return strMcpatcher + path;
        } else {
            return path.startsWith("/") ? strMcpatcher + path.substring(1) : path;
        }
    }
}
