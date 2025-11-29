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

package com.falsepattern.mcpatcher.internal.mixin.mixins.client.cit.armor;

import com.falsepattern.mcpatcher.internal.config.ModuleConfig;
import com.falsepattern.mcpatcher.internal.modules.cit.CITEngine;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@Mixin(RenderBiped.class)
public abstract class RenderBipedMixin {
    @WrapMethod(method = "getArmorResource",
                remap = false,
                require = 1)
    private static ResourceLocation replaceArmorTexture(Entity entity,
                                                        ItemStack stack,
                                                        int slot,
                                                        String type,
                                                        Operation<ResourceLocation> original) {
        if (ModuleConfig.customItemTextures && entity instanceof EntityLivingBase) {
            return CITEngine.replaceArmorTexture(stack, original.call(entity, stack, slot, type));
        } else {
            return original.call(entity, stack, slot, type);
        }
    }
}
