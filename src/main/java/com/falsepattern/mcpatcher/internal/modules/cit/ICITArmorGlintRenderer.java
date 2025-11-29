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

package com.falsepattern.mcpatcher.internal.modules.cit;

import com.falsepattern.mcpatcher.internal.mixin.mixins.client.cit.enchant.RendererLivingEntityMixin;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * An interface implemented on top of {@link RendererLivingEntity}, implying it supports custom armor glint rendering.
 * <p>
 * So logic can be activated in other classes such as {@link RenderBiped} and {@link RenderPlayer} with less boilerplate.
 */
public interface ICITArmorGlintRenderer {
    /**
     * @return {@code true} if this renderer supports CIT, otherwise {@code false}
     *
     * @apiNote Make this return {@code true} to activate the logic provided by {@link RendererLivingEntityMixin}
     */
    default boolean mcp$isCustomArmorTextureSupported() {
        return false;
    }

    /**
     * @param entity Target entity wearing the armor
     * @param slot   The armor slot (0-3) containing the armor
     *
     * @return Either the {@code ItemStack} found in {@code slot} or {@code null}
     */
    default @Nullable ItemStack mcp$getArmorInSlot(EntityLivingBase entity, int slot) {
        return null;
    }
}
