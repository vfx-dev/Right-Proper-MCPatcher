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
import com.falsepattern.mcpatcher.internal.modules.common.ResourceScanner;
import com.falsepattern.mcpatcher.internal.modules.overlay.ResourceGenerator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.Properties;

import static com.falsepattern.mcpatcher.internal.modules.cit.CITEngine.LOG;

public final class CITPropsArmor extends CITPropsSingle {
    /**
     * Base texture to use when matched.
     *
     * @implNote Base textures for armor are technically not part of the spec, but I don't car
     */
    private @Nullable ResourceLocation texLoc;
    /**
     * Map of alternate textures to replace.
     * <p>
     * Includes mappings of both A->B and B->B in case a lookup happens twice,
     * to avoid accidentally mapping back to {@link CITPropsArmor#texLoc}.
     */
    @Unmodifiable
    private @Nullable Object2ObjectMap<ResourceLocation, ResourceLocation> altTexLoc;

    public CITPropsArmor(String name, Properties props) {
        super(name, props);

        this.texLoc = null;
        this.altTexLoc = null;
    }

    public ResourceLocation getTexture(ResourceLocation original) {
        final ResourceLocation replacement;
        if (altTexLoc != null) {
            replacement = altTexLoc.getOrDefault(original, texLoc);
        } else {
            replacement = texLoc;
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
            texLoc = null;
        } else {
            texLoc = new ResourceLocation(texture + ".png");
        }

        if (altTextures.isEmpty()) {
            altTexLoc = null;
        } else {
            altTexLoc = new Object2ObjectOpenHashMap<>();

            for (val entry : altTextures.entrySet()) {
                val srcName = entry.getKey();
                val dstName = entry.getValue();

                // TODO: Vanilla has this fancy prefix, do we always expect mods to do the same?
                val src = new ResourceLocation("textures/models/armor/" + srcName + ".png");
                if (ResourceScanner.hasResource(src)) {
                    val dst = new ResourceLocation(dstName + ".png");
                    altTexLoc.put(src, dst);
                    // Required for double-lookups
                    altTexLoc.put(dst, dst);
                }
            }

            if (altTexLoc.isEmpty()) {
                altTexLoc = null;
            } else {
                altTexLoc = CollectionUtil.lockMap(altTexLoc);
            }
        }
    }

    @Override
    public boolean shouldKeep() {
        return texLoc != null || altTexLoc != null;
    }
    // endregion
}
