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
import com.falsepattern.mcpatcher.internal.modules.common.Identity2ObjectHashMap;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.Properties;

import static com.falsepattern.mcpatcher.internal.modules.cit.CITEngine.LOG;

@Getter
@Accessors(fluent = true,
           chain = false)
public final class CITItemInfo implements Comparable<CITItemInfo> {
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

    @Unmodifiable
    private final ObjectList<NBTRule> nbtRules;

    private @Nullable IIcon icon;
    @Unmodifiable
    private @Nullable Object2ObjectMap<IIcon, IIcon> altIcons;

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

        this.icon = null;
        this.altIcons = null;
    }

    // TODO
    public boolean matches(ItemStack itemStack) {
        return false;
    }

    public IIcon getIcon(IIcon original) {
        final IIcon replacement;
        if (altIcons != null) {
            replacement = altIcons.getOrDefault(original, icon);
        } else {
            replacement = icon;
        }

        if (replacement != null) {
            return replacement;
        } else {
            return original;
        }
    }

    @Override
    public int compareTo(@NotNull CITItemInfo other) {
        // Highest number first
        val comp = Integer.compare(other.weight, this.weight);
        if (comp != 0) {
            return comp;
        }
        // Then natural (A-Z)
        return this.name.compareTo(other.name);
    }

    public boolean isValid() {
        if (items.isEmpty()) {
            LOG.warn("No valid items defined for: {}", name);
            return false;
        }
        if (texture == null && altTextures.isEmpty()) {
            LOG.warn("No valid textures defined for: {}", name);
            return false;
        }
        return true;
    }

    public void updateIcons(TextureMap textureMap, @Nullable Map<ResourceLocation, ResourceGenerator> overlay) {
        if (texture == null) {
            icon = null;
        } else {
            icon = textureMap.registerIcon(texture);
        }

        if (altTextures.isEmpty()) {
            altIcons = null;
        } else {
            altIcons = new Identity2ObjectHashMap<>();

            for (val entry : altTextures.entrySet()) {
                val srcName = entry.getKey();
                val dstName = entry.getValue();

                val src = textureMap.getTextureExtry(srcName);
                if (src != null) {
                    val dst = textureMap.registerIcon(dstName);
                    altIcons.put(src, dst);
                }
            }

            if (altIcons.isEmpty()) {
                altIcons = null;
            } else {
                altIcons = CollectionUtil.lockMap(altIcons);
            }
        }
    }

    public boolean hasIcons() {
        return icon != null || altIcons != null;
    }
}
