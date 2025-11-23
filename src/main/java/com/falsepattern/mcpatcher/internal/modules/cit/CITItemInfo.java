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

import com.falsepattern.mcpatcher.internal.modules.common.IntRange;
import com.falsepattern.mcpatcher.internal.modules.common.NBTRule;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.Item;

import java.util.Properties;

import static com.falsepattern.mcpatcher.internal.modules.cit.CITEngine.LOG;

@Getter
@Accessors(fluent = true,
           chain = false)
public final class CITItemInfo {
    private final String name;

    private final @Nullable String texture;
    private final Object2ObjectMap<String, String> altTextures;
    private final int weight;
    private final ObjectSet<Item> items;

    private final @Nullable DamageRangeList damage;
    private final int damageMask;
    private final IntRange.@Nullable List stackSize;

    private final IntRange.@Nullable List enchantmentIDs;
    private final IntRange.@Nullable List enchantmentLevels;

    private final ObjectList<NBTRule> nbtRules;

    public CITItemInfo(String name, Properties props) {
        this.name = name;

        this.texture = CITParser.parseTexture(name, props);
        this.altTextures = CITParser.parseAltTextures(name, props);
        this.weight = CITParser.parseWeight(props);
        this.items = CITParser.parseItems(props);

        this.damage = CITParser.parseDamage(props);
        this.damageMask = CITParser.parseDamageMask(props);
        this.stackSize = CITParser.parseStackSize(props);

        this.enchantmentIDs = CITParser.parseEnchantmentIDs(props);
        this.enchantmentLevels = CITParser.parseEnchantmentLevels(props);

        this.nbtRules = CITParser.parseNbtRules(props);
    }

    public boolean validate() {
        if (items.isEmpty()) {
            LOG.warn("No valid items defined for: {}", name);
            return false;
        }

        return true;
    }
}
