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

package com.falsepattern.mcpatcher.internal.mixin.client.overlay;

import com.falsepattern.mcpatcher.internal.modules.overlay.OverlayResourceManager;
import com.falsepattern.mcpatcher.internal.modules.overlay.ResourceGenerator;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

/**
 * Priority 900 so that applyOverlay lands before FalseTweaks Voxelizer TextureMapMixin loadExtraTexturesForLayers
 */
@Mixin(value = TextureMap.class,
       priority = 900)
public abstract class TextureMapMixin {
    @Unique
    private Map<ResourceLocation, ResourceGenerator> mcp$overlay;

    @ModifyVariable(method = "loadTextureAtlas",
                    at = @At(value = "INVOKE",
                             target = "Lnet/minecraft/client/renderer/texture/TextureMap;registerIcons()V",
                             shift = At.Shift.AFTER),
                    argsOnly = true,
                    require = 1)
    private IResourceManager applyOverlay(IResourceManager value) {
        val overlay = mcp$overlay;
        mcp$overlay = null;
        if (overlay != null) {
            return new OverlayResourceManager(value, overlay);
        }
        return value;
    }
}
