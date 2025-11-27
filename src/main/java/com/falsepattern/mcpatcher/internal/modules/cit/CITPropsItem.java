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
import com.falsepattern.mcpatcher.internal.modules.overlay.ResourceGenerator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.Properties;

import static com.falsepattern.mcpatcher.internal.modules.cit.CITEngine.LOG;

public final class CITPropsItem extends CITPropsSingle {
    /**
     * Base icon to use when matched.
     */
    private @Nullable IIcon icon;
    /**
     * Map of alternate icons to replace.
     * <p>
     * Includes mappings of both A->B and B->B in case a lookup happens twice,
     * to avoid accidentally mapping back to {@link CITPropsItem#icon}.
     */
    @Unmodifiable
    private @Nullable Object2ObjectMap<IIcon, IIcon> altIcons;

    public CITPropsItem(String name, Properties props) {
        super(name, props);

        this.icon = null;
        this.altIcons = null;
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

    // region Init
    @SuppressWarnings("DuplicatedCode")
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

    @Override
    public void load(TextureMap textureMap, @Nullable Map<ResourceLocation, ResourceGenerator> overlay) {
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
                    // Required for double-lookups
                    altIcons.put(dst, dst);
                }
            }

            if (altIcons.isEmpty()) {
                altIcons = null;
            } else {
                altIcons = CollectionUtil.lockMap(altIcons);
            }
        }
    }

    @Override
    public boolean shouldKeep() {
        return icon != null || altIcons != null;
    }
    // endregion
}
