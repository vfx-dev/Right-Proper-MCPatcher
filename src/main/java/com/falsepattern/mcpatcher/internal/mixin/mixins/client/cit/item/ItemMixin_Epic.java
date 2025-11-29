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
import com.falsepattern.mcpatcher.internal.modules.cit.ICITItemsRenamed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

@Mixin(Item.class)
public abstract class ItemMixin_Epic implements ICITItemsRenamed {
    @Shadow
    public abstract IIcon getIconFromDamage(int p_77617_1_);

    @Shadow
    public abstract IIcon getIconFromDamageForRenderPass(int p_77618_1_, int p_77618_2_);

    /**
     * @author Ven
     * @reason Capture and redirect to renamed
     */
    @Overwrite
    public IIcon getIconIndex(ItemStack itemStack) {
        if (ModuleConfig.isCustomItemTexturesEnabled()) {
            return CITEngine.replaceIcon(itemStack, mcp$renamed$getIconIndex(itemStack));
        } else {
            return mcp$renamed$getIconIndex(itemStack);
        }
    }

    /**
     * @author Ven
     * @reason Capture and redirect to renamed
     */
    @Overwrite(remap = false)
    public IIcon getIcon(ItemStack stack, int pass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
        if (ModuleConfig.isCustomItemTexturesEnabled()) {
            return CITEngine.replaceIcon(stack, mcp$renamed$getIcon(stack, pass, player, usingItem, useRemaining));
        } else {
            return mcp$renamed$getIcon(stack, pass, player, usingItem, useRemaining);
        }
    }

    /**
     * @author Ven
     * @reason Capture and redirect to renamed
     */
    @Overwrite(remap = false)
    public IIcon getIcon(ItemStack stack, int pass) {
        if (ModuleConfig.isCustomItemTexturesEnabled()) {
            return CITEngine.replaceIcon(stack, mcp$renamed$getIcon(stack, pass));
        } else {
            return mcp$renamed$getIcon(stack, pass);
        }
    }

    @Override
    public IIcon mcp$renamed$getIconIndex(ItemStack itemStack) {
        return this.getIconFromDamage(itemStack.getItemDamage());
    }

    @Override
    public IIcon mcp$renamed$getIcon(ItemStack stack,
                                     int renderPass,
                                     EntityPlayer player,
                                     ItemStack usingItem,
                                     int useRemaining) {
        return mcp$renamed$getIcon(stack, renderPass);
    }

    @Override
    public IIcon mcp$renamed$getIcon(ItemStack stack, int pass) {
        return this.getIconFromDamageForRenderPass(stack.getItemDamage(), pass);
    }
}
