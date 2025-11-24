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
import com.falsepattern.mcpatcher.internal.modules.overlay.ResourceGenerator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@Getter
@Accessors(fluent = true,
           chain = false)
public abstract class CITPropsSingle implements Comparable<CITPropsSingle> {
    protected final String name;

    protected final @Nullable String texture;
    protected final Object2ObjectMap<String, String> altTextures;
    protected final int weight;
    protected final ObjectSet<Item> items;

    protected final @Nullable DamageRangeList damage;
    protected final int damageMask;
    protected final IntRange.@Nullable List stackSize;

    protected final IntRange.@Nullable List enchantmentIDs;
    protected final IntRange.@Nullable List enchantmentLevels;

    @Unmodifiable
    protected final ObjectList<NBTRule> nbtRules;

    public CITPropsSingle(String name, Properties props) {
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

    public abstract boolean isValid();

    public abstract void load(TextureMap textureMap, @Nullable Map<ResourceLocation, ResourceGenerator> overlay);

    public abstract boolean shouldKeep();

    public boolean matches(ItemStack itemStack) {
        return matchDamage(itemStack) &&
               matchStackSize(itemStack) &&
               matchNbtRules(itemStack) &&
               matchEnchantments(itemStack);
    }

    protected boolean matchDamage(ItemStack itemStack) {
        if (damage == null) {
            return true;
        }
        return damage.isInRange(itemStack, damageMask);
    }

    protected boolean matchStackSize(ItemStack itemStack) {
        if (stackSize == null) {
            return true;
        }
        return stackSize.isInRange(itemStack.stackSize);
    }

    protected boolean matchNbtRules(ItemStack itemStack) {
        if (nbtRules.isEmpty()) {
            return true;
        }
        for (val nbtRule : nbtRules) {
            if (!nbtRule.match(itemStack.stackTagCompound)) {
                return false;
            }
        }
        return true;
    }

    protected boolean matchEnchantments(ItemStack itemStack) {
        if (enchantmentIDs == null && enchantmentLevels == null) {
            // No enchantments always matches
            return true;
        }

        val enchantList = getEnchantList(itemStack);
        if (enchantList == null) {
            // Lack of enchantments when some are expected doesn't match
            return false;
        }

        for (val enchant : enchantList) {
            try {
                if (enchantmentIDs != null) {
                    val id = enchant.getShort("id");
                    if (!enchantmentIDs.isInRange(id)) {
                        // Some id out of range, don't match
                        return false;
                    }
                }
                if (enchantmentLevels != null) {
                    val level = enchant.getShort("lvl");
                    if (!enchantmentLevels.isInRange(level)) {
                        // Some level out of range, don't match
                        return false;
                    }
                }
            } catch (RuntimeException e) {
                // Bad NBT means we bail and assume it doesn't match
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(@NotNull CITPropsSingle other) {
        // Highest number first
        val comp = Integer.compare(other.weight, this.weight);
        if (comp != 0) {
            return comp;
        }
        // Then natural (A-Z)
        return this.name.compareTo(other.name);
    }

    private static @Nullable List<NBTTagCompound> getEnchantList(ItemStack itemStack) {
        // Not all items have NBT
        val itemNbt = itemStack.stackTagCompound;
        if (itemNbt == null) {
            return null;
        }

        // Enchanted books have a separate tag
        final NBTBase enchantNbt;
        if (itemStack.getItem() == Items.enchanted_book) {
            enchantNbt = itemNbt.getTag("StoredEnchantments");
        } else {
            enchantNbt = itemNbt.getTag("ench");
        }

        if (enchantNbt instanceof NBTTagList) {
            //noinspection unchecked
            return ((NBTTagList) enchantNbt).tagList;
        }
        return null;
    }
}
