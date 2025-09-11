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

package com.falsepattern.mcpatcher.internal.mixin.client.mob;

import com.falsepattern.mcpatcher.internal.config.MCPatcherConfig;
import com.falsepattern.mcpatcher.internal.modules.mob.MobEngine;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {
    @Inject(method = "renderEntities(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/culling/ICamera;F)V",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                     args = "ldc=entities",
                     shift = At.Shift.AFTER),
            require = 1)
    private void mob_beginEntities(CallbackInfo ci) {
        if (MCPatcherConfig.randomMobs) {
            MobEngine.beginEntities();
        }
    }

    @Inject(method = "renderEntities(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/culling/ICamera;F)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntitySimple(Lnet/minecraft/entity/Entity;F)Z",
                     ordinal = 1),
            require = 1)
    private void mob_nextEntity(CallbackInfo ci, @Local(ordinal = 0) Entity entity) {
        if (MCPatcherConfig.randomMobs) {
            MobEngine.nextEntity(entity);
        }
    }

    @Inject(method = "renderEntities(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/culling/ICamera;F)V",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                     args = "ldc=blockentities"),
            require = 1)
    private void mob_endEntities(CallbackInfo ci) {
        if (MCPatcherConfig.randomMobs) {
            MobEngine.endEntities();
        }
    }
}
