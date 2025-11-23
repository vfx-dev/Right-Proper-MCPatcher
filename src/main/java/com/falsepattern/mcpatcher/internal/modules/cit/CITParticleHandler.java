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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

/**
 * Used to render {@link EntityBreakingFX} with the correct icon, as in vanilla the only context provided to is the {@link Item} and it's associated {@code int meta}.
 */
public final class CITParticleHandler {
    private static @Nullable Item lastItem;
    private static @Nullable IIcon lastIcon;

    private CITParticleHandler() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param itemStack {@link ItemStack} to be rendered as a particle
     *
     * @implSpec Called before any {@link EntityBreakingFX} are rendered.
     */
    public static void set(@NotNull ItemStack itemStack) {
        lastItem = itemStack.getItem();
        lastIcon = CITEngine.replaceIcon(itemStack, itemStack.getIconIndex());
    }

    /**
     * Resets the tracked {@link Item} and associated {@link IIcon}
     *
     * @implSpec Called after {@link EntityBreakingFX} are done rendering, and on a resource reload.
     */
    public static void reset() {
        lastItem = null;
        lastIcon = null;
    }

    /**
     * @param item     the {@link Item} being rendered as a particle
     * @param original the original icon for the given {@link Item}
     *
     * @return The stored {@link IIcon} if the {@link Item}, otherwise the original {@link IIcon}
     *
     * @implSpec Called when calling {@link EntityBreakingFX#setParticleIcon}
     */
    public static IIcon get(@NotNull Item item, @NotNull IIcon original) {
        if (item == lastItem && lastIcon != null) {
            return lastIcon;
        } else {
            return original;
        }
    }
}
