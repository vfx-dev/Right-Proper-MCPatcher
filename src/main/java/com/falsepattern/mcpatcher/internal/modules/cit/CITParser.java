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

package com.falsepattern.mcpatcher.internal.modules.cit;

import com.falsepattern.mcpatcher.internal.modules.common.CollectionUtil;
import com.falsepattern.mcpatcher.internal.modules.common.CommonParser;
import com.falsepattern.mcpatcher.internal.modules.common.IntRange;
import com.falsepattern.mcpatcher.internal.modules.common.NBTRule;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.falsepattern.mcpatcher.internal.modules.common.CommonParser.LOG;

/*
TODO: We're not parsing paths correctly. Unsure what to follow as a "ground truth"
 One of these is sure to be correct:
 - https://bitbucket.org/prupe/mcpatcher/src/master/doc/cit_single.properties
 - https://optifine.readthedocs.io/syntax.html#paths-file-locations
 - https://github.com/mist475/MCPatcherForge/blob/9e40345f30a9cc26caa29bba6fa22e561307e51d/src/main/java/com/prupe/mcpatcher/mal/resource/TexturePackAPI.java#L205
*/
public final class CITParser {
    private CITParser() {
        throw new UnsupportedOperationException();
    }

    public static int parseWeight(Properties props) {
        return CommonParser.parseInt(props.getProperty("weight"), 0);
    }

    /**
     * @implNote No clamping for ID extender support
     */
    public static @Nullable DamageRangeList parseDamage(Properties props) {
        var str = props.getProperty("damage");
        if (str == null) {
            return null;
        }

        val isPercent = StringUtils.contains(str, '%');
        if (isPercent) {
            str = StringUtils.remove(str, '%');
        }

        val ranges = CommonParser.parseIntRanges(str);
        if (ranges == null) {
            return null;
        }
        return new DamageRangeList(ranges, isPercent);
    }

    /**
     * @implNote No clamping for ID extender support
     */
    public static int parseDamageMask(Properties props) {
        return CommonParser.parseInt(props.getProperty("damageMask"), 0xFFFFFFFF);
    }

    public static IntRange.@Nullable List parseStackSize(Properties props) {
        return CommonParser.parseIntRanges(props.getProperty("stackSize"));
    }

    public static IntRange.@Nullable List parseEnchantmentIDs(Properties props) {
        return CommonParser.parseIntRanges(props.getProperty("enchantmentIDs"));
    }

    public static IntRange.@Nullable List parseEnchantmentLevels(Properties props) {
        return CommonParser.parseIntRanges(props.getProperty("enchantmentLevels"));
    }

    public static ObjectList<NBTRule> parseNbtRules(Properties props) {
        val list = new ObjectArrayList<NBTRule>();

        //noinspection unchecked
        val entries = (Set<Map.Entry<String, String>>) (Object) props.entrySet();
        for (val entry : entries) {
            val rule = NBTRule.create(entry.getKey(), entry.getValue());
            if (rule != null) {
                list.add(rule);
            }
        }

        return CollectionUtil.lockList(list);
    }

    public static @NotNull ObjectSet<Item> parseItems(Properties props) {
        val set = new ObjectArraySet<Item>();
        parseItems(set, props.getProperty("items"));
        if (set.isEmpty()) {
            parseItems(set, props.getProperty("matchItems"));
        }
        return CollectionUtil.lockSet(set);
    }

    public static @Nullable String parseTexture(String name, Properties props) {
        var texture = parseTexture(name, props.getProperty("source"));
        if (texture == null) {
            texture = parseTexture(name, props.getProperty("texture"));
        }
        if (texture == null) {
            texture = parseTexture(name, props.getProperty("tile"));
        }
        return texture;
    }


    public static Object2ObjectMap<String, String> parseAltTextures(String name, Properties props) {
        val map = new Object2ObjectArrayMap<String, String>();

        //noinspection unchecked
        val entries = (Set<Map.Entry<String, String>>) (Object) props.entrySet();
        for (val entry : entries) {
            val key = entry.getKey();

            final String str;
            if (key.startsWith("source.")) {
                str = key.substring("source.".length());
            } else if (key.startsWith("texture.")) {
                str = key.substring("texture.".length());
            } else if (key.startsWith("tile.")) {
                str = key.substring("tile.".length());
            } else {
                continue;
            }

            val value = entry.getValue();
            val texture = parseTexture(name, value);
            if (texture != null) {
                map.put(str, texture);
            }
        }

        return CollectionUtil.lockMap(map);
    }

    private static void parseItems(ObjectSet<Item> set, @Nullable String str) {
        if (str == null) {
            return;
        }

        val itemNames = StringUtils.split(str);
        for (val itemName : itemNames) {
            val item = findItem(itemName);
            if (item != null) {
                set.add(item);
            }
        }
    }

    private static @Nullable Item findItem(@NotNull String itemName) {
        val split = StringUtils.split(itemName, ':');

        final String modId;
        final String name;
        if (split.length == 1) {
            modId = "minecraft";
            name = split[0];
        } else if (split.length == 2) {
            modId = split[0];
            name = split[1];
        } else {
            LOG.warn("Invalid item name: {}", itemName);
            return null;
        }

        val item = GameRegistry.findItem(modId, name);
        if (item == null) {
            LOG.warn("Item not found: {}", itemName);
            return null;
        }
        return item;
    }

    private static @Nullable String parseTexture(String name, @Nullable String str) {
        if (str == null) {
            return null;
        }

        final String texture;
        if (StringUtils.contains(str, '/')) {
            texture = str;
        } else {
            texture = StringUtils.substringBeforeLast(name, "/") + "/" + str;
        }
        if (texture.endsWith(".png")) {
            return texture.substring(0, texture.length() - ".png".length());
        } else {
            return texture;
        }
    }
}
