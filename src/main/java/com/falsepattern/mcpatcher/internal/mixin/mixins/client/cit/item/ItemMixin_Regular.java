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
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

@Mixin(Item.class)
public abstract class ItemMixin_Regular {
    @WrapMethod(method = "getIconIndex(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/IIcon;",
                require = 1)
    private IIcon replaceIcon(ItemStack itemStack, Operation<IIcon> original) {
        if (ModuleConfig.isCustomItemTexturesEnabled()) {
            return CITEngine.replaceIcon(itemStack, original.call(itemStack));
        } else {
            return original.call(itemStack);
        }
    }

    @WrapMethod(method = "getIcon(Lnet/minecraft/item/ItemStack;ILnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/util/IIcon;",
                remap = false,
                require = 1)
    private IIcon replaceIcon(ItemStack stack,
                              int renderPass,
                              EntityPlayer player,
                              ItemStack usingItem,
                              int useRemaining,
                              Operation<IIcon> original) {
        if (ModuleConfig.isCustomItemTexturesEnabled()) {
            return CITEngine.replaceIcon(stack, original.call(stack, renderPass, player, usingItem, useRemaining));
        } else {
            return original.call(stack, renderPass, player, usingItem, useRemaining);
        }
    }

    @WrapMethod(method = "getIcon(Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/util/IIcon;",
                remap = false,
                require = 1)
    private IIcon replaceIcon(ItemStack stack, int pass, Operation<IIcon> original) {
        if (ModuleConfig.isCustomItemTexturesEnabled()) {
            return CITEngine.replaceIcon(stack, original.call(stack, pass));
        } else {
            return original.call(stack, pass);
        }
    }
}
