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

package com.falsepattern.mcpatcher.internal.modules.natural;

import com.falsepattern.mcpatcher.internal.config.ModuleConfig;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

public class NaturalTextures {

    /**
     * Apply UV overrides to a quad's texture, based on whether  the texture is
     * in the list of Natural Textures, it's XYZ coordinates and the block's side.
     * This method mutates the two arrays passed to it.
     * @param block The block this texture applies to
     * @param x The X coordinate of the block
     * @param y The Y coordinate of the block
     * @param z The Z coordinate of the block
     * @param side The side of the block being rendered
     * @param texture The texture to be used
     * @param vertexUs An array of size 4, containing the U coordinates of each quad vertex
     * @param vertexVs An array of size 4, containing the V coordinates of each quad vertex
     * For both arrays, the values adhere to the following order, relative to the texture file:
     * - Top Left
     * - Top Right
     * - Bottom Right
     * - Bottom Left
     */
    public static void applyNaturalTexture(Block block, double x, double y, double z, ForgeDirection side, IIcon texture,
                                           double[] vertexUs, double[] vertexVs) {
        // TODO filter for valid textures, select a random variation based on the xyz coords (+ maybe seed)
        remapQuadUVs(vertexUs, vertexVs, Math.PI, false, texture);
    }

    private static void remapQuadUVs(double[] vertexUs, double[] vertexVs, double rotationAngle, boolean mirrorHorizontal, IIcon texture) {
        if(!ModuleConfig.naturalTextures) return;

        if(mirrorHorizontal) {
            double swap = vertexUs[0];
            vertexUs[0] = vertexUs[1];
            vertexUs[1] = swap;

            swap = vertexUs[2];
            vertexUs[2] = vertexUs[3];
            vertexUs[3] = swap;
        }
    }

}
