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

package com.falsepattern.mcpatcher.internal.mixin.mixins.client.cit.item;

import com.falsepattern.mcpatcher.internal.config.ModuleConfig;
import com.falsepattern.mcpatcher.internal.modules.cit.CITEngine;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;

@Mixin(RenderSnowball.class)
public abstract class RenderSnowballMixin {
    @WrapOperation(method = "doRender(Lnet/minecraft/entity/Entity;DDDFF)V",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/item/Item;getIconFromDamage(I)Lnet/minecraft/util/IIcon;"),
                   require = 1)
    private IIcon replaceIcon(Item item, int meta, Operation<IIcon> original, @Local(argsOnly = true) Entity entity) {
        if (ModuleConfig.isCustomItemTexturesEnabled() && entity instanceof EntityPotion) {
            val itemStack = ((EntityPotion) entity).potionDamage;
            return CITEngine.replaceIcon(itemStack, original.call(item, meta));
        } else {
            return original.call(item, meta);
        }
    }
}
