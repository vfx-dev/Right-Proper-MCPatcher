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

public class NaturalTexturesEngine {

    /**
     * Attempts to apply UV overrides to a quad's texture, based on certain criteria.
     * This method mutates the two arrays passed into it.
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
        if(!ModuleConfig.naturalTextures) return;
        // TODO filter for valid textures, select a random variation based on the xyz coords (+ maybe seed)


        int mod = (int) x % 4;

        double rotation = 0;
        switch (mod) {
            case 0:
                rotation = 0;
                break;

            case 1:
            case -1:
                rotation = Math.PI * 0.5D;
                break;

            case 2:
            case -2:
                rotation = Math.PI;
                break;

            case 3:
            case -3:
                rotation = Math.PI * 1.5D;
                break;
        }

        remapQuadUVs(rotation, texture, vertexUs, vertexVs);

        // TODO decide if mirror
        if(false) {
            mirrorQuadUVs(vertexUs);
        }
    }

    /**
     * Mirror the texture's U or V axis by swizzling the array's coordinates.
     * This method mutates the array passed into it.
     * @param vertexCoords An array of size 4, containing either the U or V coordinates of each quad vertex
     * The array values should adhere to the following order, relative to the texture file:
     * - Top Left
     * - Top Right
     * - Bottom Right
     * - Bottom Left
     */
    private static void mirrorQuadUVs(double[] vertexCoords) {
        double swap = vertexCoords[0];
        vertexCoords[0] = vertexCoords[1];
        vertexCoords[1] = swap;

        swap = vertexCoords[2];
        vertexCoords[2] = vertexCoords[3];
        vertexCoords[3] = swap;
    }

    /**
     * Rotates a quad textures clockwise by an amount in radians. This method is not intended to be used for rotating at angles
     * other than 0, 90, 180 or 270 degrees, and such rotations will result in skewed UVs and in textures from
     * other appearing on the corners of this quad when viewed in-game.
     * This method mutates the two arrays passed into it.
     * @param rotationAngle The angle, in radians, to rotate the UVs by.
     * @param texture The IIcon texture to use when calculating the rotation
     * @param vertexUs An array of size 4, containing the U coordinates of each quad vertex
     * @param vertexVs An array of size 4, containing the V coordinates of each quad vertex
     * For both arrays, the values adhere to the following order, relative to the texture file:
     * - Top Left
     * - Top Right
     * - Bottom Right
     * - Bottom Left
     */
    private static void remapQuadUVs(double rotationAngle, IIcon texture, double[] vertexUs, double[] vertexVs) {
        rotationAngle %= 2 * Math.PI;

        float lengthU = texture.getMaxU() - texture.getMinU();
        float lengthV = texture.getMaxV() - texture.getMinV();

        float centerU = texture.getMinU() + lengthU / 2F;
        float centerV = texture.getMinV() + lengthV / 2F;

        float rotSin = MathHelper.sin((float) rotationAngle);
        float rotCos = MathHelper.cos((float) rotationAngle);

        double aspectU = Math.abs(rotSin * ((lengthV / lengthU) - 1D)) + 1D;
        double aspectV = 1F / aspectU;

        // Rotate
        for(int i = 0; i < 4; i++) {
            double deltaU = (vertexUs[i] - centerU) * aspectU;
            double deltaV = (vertexVs[i] - centerV) * aspectV;

            vertexUs[i] = (rotCos * deltaU) + (rotSin * deltaV) + centerU;
            vertexVs[i] = (rotCos * deltaV) - (rotSin * deltaU) + centerV;
        }

    }

}
