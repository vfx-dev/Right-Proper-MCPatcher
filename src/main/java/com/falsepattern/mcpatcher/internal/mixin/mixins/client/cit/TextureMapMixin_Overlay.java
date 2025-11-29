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

package com.falsepattern.mcpatcher.internal.mixin.mixins.client.cit;

import com.falsepattern.mcpatcher.internal.modules.cit.CITEngine;
import com.falsepattern.mcpatcher.internal.modules.overlay.ResourceGenerator;
import lombok.val;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = TextureMap.class)
public abstract class TextureMapMixin_Overlay {
    @Final
    @Shadow
    private int textureType;

    @Dynamic
    @Shadow(remap = false)
    private Map<ResourceLocation, ResourceGenerator> mcp$overlay;

    @Inject(method = "registerIcons",
            at = @At("RETURN"),
            require = 1)
    private void updateIconsCIT(CallbackInfo ci) {
        if (this.textureType == 1) {
            mcp$overlay = null;
            val overlay = new HashMap<ResourceLocation, ResourceGenerator>();
            CITEngine.updateIcons((TextureMap) (Object) this, overlay);
            mcp$overlay = overlay;
        }
    }
}
