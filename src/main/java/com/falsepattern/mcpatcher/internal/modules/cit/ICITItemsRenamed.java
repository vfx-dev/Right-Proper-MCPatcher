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

import com.falsepattern.mcpatcher.internal.asm.CITIconReplacementInjector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

/**
 * Base methods get renamed by: {@link CITIconReplacementInjector}
 */
public interface ICITItemsRenamed {
    IIcon mcp$renamed$getIconIndex(ItemStack itemStack);

    IIcon mcp$renamed$getIcon(ItemStack stack,
                              int renderPass,
                              EntityPlayer player,
                              ItemStack usingItem,
                              int useRemaining);

    IIcon mcp$renamed$getIcon(ItemStack stack, int pass);
}
