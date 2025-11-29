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

package com.falsepattern.mcpatcher.internal.mixin.mixins.client.cit.enchant;

import com.falsepattern.mcpatcher.internal.modules.cit.ICITArmorGlintRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

@Mixin(RenderPlayer.class)
public abstract class RenderPlayerMixin implements ICITArmorGlintRenderer {
    @Override
    public boolean mcp$isCustomArmorTextureSupported() {
        return true;
    }

    @Override
    public @Nullable ItemStack mcp$getArmorInSlot(EntityLivingBase entity, int slotId) {
        if (entity instanceof AbstractClientPlayer) {
            return ((AbstractClientPlayer) entity).inventory.armorItemInSlot(3 - slotId);
        }
        return null;
    }
}
