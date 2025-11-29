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

package com.falsepattern.mcpatcher.internal.mixin.mixins.client.mob.compat.damageindicators;

import DamageIndicatorsMod.gui.DIGuiTools;
import com.falsepattern.mcpatcher.internal.config.ModuleConfig;
import com.falsepattern.mcpatcher.internal.modules.mob.MobEngine;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

@Mixin(value = DIGuiTools.class,
       remap = false)
public abstract class DIGuiToolsMixin {
    @WrapOperation(method = "renderEntity",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V",
                            remap = true),
                   require = 1)
    private static void mob_wrapEntity(Render instance,
                                       Entity entity,
                                       double x,
                                       double y,
                                       double z,
                                       float entityYaw,
                                       float partialTicks,
                                       Operation<Void> original) {
        if (ModuleConfig.randomMobs) {
            MobEngine.pushRenderingEntities();
            MobEngine.nextEntity(entity);
            original.call(instance, entity, x, y, z, entityYaw, partialTicks);
            MobEngine.popRenderingEntities();
        } else {
            original.call(instance, entity, x, y, z, entityYaw, partialTicks);
        }
    }
}
