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

import com.falsepattern.mcpatcher.Tags;
import com.falsepattern.mcpatcher.internal.modules.common.MCPMath;
import com.falsepattern.mcpatcher.internal.modules.common.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

import java.util.Map;

public class NaturalTexturesEngine {

    static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + " NaturalTextures");

    private static Map<String, NaturalTexturesInfo> naturalTexturesInfo = null;

    public static void reloadNaturalTextureResources() {
        LOG.debug("Reloading Natural Textures");

        // Try reading from mcpatcher path first, fallback to optifine for backwards compatibility
        naturalTexturesInfo = NaturalTexturesParser.parseFirstAvailableResource(
                "minecraft:mcpatcher/natural.properties",
                "minecraft:optifine/natural.properties");
    }

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
    public static void applyNaturalTexture(Block block, int x, int y, int z, @NotNull Side side, @NotNull IIcon texture,
                                           double[] vertexUs, double[] vertexVs) {
        // Ignore blocks rendered in inventory / player's hand
        if (x == 0 && y == 0 && z == 0) return;

        String texName = texture.getIconName();
        if (!naturalTexturesInfo.containsKey(texName)) return;
        NaturalTexturesInfo texInfo = naturalTexturesInfo.get(texName);

        int rand = getRandom(x, y, z, side.ordinal());
        rotateQuadUVs(texInfo.getRadiansFromRandom(rand), texture, vertexUs, vertexVs);

        int rand2 = getRandom(x, y, z, rand);
        if(texInfo.getFlipFromRandom(rand2)) {
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
     * Rotates a quad's texture clockwise by an amount in radians. This method is not intended to be used for rotating
     * at angles other than 0, 90, 180 or 270 degrees, and such rotations will result in skewed UVs and in textures from
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
    private static void rotateQuadUVs(double rotationAngle, IIcon texture, double[] vertexUs, double[] vertexVs) {
        if(rotationAngle == 0D) return;

        float lengthU = texture.getMaxU() - texture.getMinU();
        float lengthV = texture.getMaxV() - texture.getMinV();

        float centerU = texture.getMinU() + lengthU / 2F;
        float centerV = texture.getMinV() + lengthV / 2F;

        rotationAngle %= 2D * Math.PI;

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

    private static int getRandom(int x , int y, int z, int salt) {
        int rand = MCPMath.intHash(salt + 37);
        rand = MCPMath.intHash(rand + x);
        rand = MCPMath.intHash(rand + z);
        rand = MCPMath.intHash(rand + y);
        return rand & Integer.MAX_VALUE;
    }
}
