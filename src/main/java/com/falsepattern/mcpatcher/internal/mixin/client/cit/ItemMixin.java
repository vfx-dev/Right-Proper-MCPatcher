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

package com.falsepattern.mcpatcher.internal.mixin.client.cit;

import com.falsepattern.mcpatcher.internal.config.MCPatcherConfig;
import com.falsepattern.mcpatcher.internal.modules.cit.CITEngine;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

@Mixin(Item.class)
public abstract class ItemMixin {
    @WrapMethod(method = "getIconIndex(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/IIcon;")
    private IIcon replaceIcon(ItemStack itemStack, Operation<IIcon> original) {
        if (MCPatcherConfig.isCustomItemTexturesEnabled()) {
            return CITEngine.replaceIcon(itemStack, original.call(itemStack));
        } else {
            return original.call(itemStack);
        }
    }
}
