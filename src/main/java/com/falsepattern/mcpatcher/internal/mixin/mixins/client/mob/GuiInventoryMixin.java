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

package com.falsepattern.mcpatcher.internal.mixin.mixins.client.mob;

import com.falsepattern.mcpatcher.internal.config.ModuleConfig;
import com.falsepattern.mcpatcher.internal.modules.mob.MobEngine;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

@Mixin(GuiInventory.class)
public abstract class GuiInventoryMixin {
    @WrapOperation(method = "func_147046_a",
                   at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntityWithPosYaw(Lnet/minecraft/entity/Entity;DDDFF)Z"),
                   require = 1)
    private static boolean mob_wrapEntity(RenderManager instance,
                                          Entity entity,
                                          double x,
                                          double y,
                                          double z,
                                          float entityYaw,
                                          float partialTicks,
                                          Operation<Boolean> original) {
        if (ModuleConfig.randomMobs) {
            MobEngine.pushRenderingEntities();
            MobEngine.nextEntity(entity);
            val ret = original.call(instance, entity, x, y, z, entityYaw, partialTicks);
            MobEngine.popRenderingEntities();
            return ret;
        } else {
            return original.call(instance, entity, x, y, z, entityYaw, partialTicks);
        }
    }
}
