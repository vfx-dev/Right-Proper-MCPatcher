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

package com.falsepattern.mcpatcher.internal.mixin.client.ctm;

import com.falsepattern.mcpatcher.internal.modules.ctm.CTMEngine;
import com.falsepattern.mcpatcher.internal.modules.overlay.ResourceGenerator;
import lombok.val;
import org.spongepowered.asm.mixin.Dynamic;
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
    @Dynamic
    @Shadow(remap = false)
    private Map<ResourceLocation, ResourceGenerator> mcp$overlay;

    @Inject(method = "registerIcons",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/entity/RenderManager;updateIcons(Lnet/minecraft/client/renderer/texture/IIconRegister;)V",
                     shift = At.Shift.AFTER),
            require = 1)
    private void updateIconsCTM(CallbackInfo ci) {
        mcp$overlay = null;
        val overlay = new HashMap<ResourceLocation, ResourceGenerator>();
        CTMEngine.updateIcons((TextureMap) (Object) this, overlay);
        mcp$overlay = overlay;
    }
}
