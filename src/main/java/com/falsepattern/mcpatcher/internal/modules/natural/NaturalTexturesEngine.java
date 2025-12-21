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
import com.falsepattern.mcpatcher.internal.modules.common.ResourceScanner;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;
import java.util.Properties;

public class NaturalTexturesEngine {

    static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + " NaturalTextures");

    private static final Object2ObjectOpenHashMap<String, NaturalTexturesInfo> naturalTexturesInfo = new Object2ObjectOpenHashMap<>();

    public static void updateIcons(TextureMap textureMap) {
        LOG.debug("Reloading Natural Textures");

        naturalTexturesInfo.clear();

        try {
            val resNatural = ResourceScanner.getResource(new ResourceLocation("minecraft:mcpatcher/natural.properties"));
            if(resNatural == null) {
                LOG.debug("No natural.properties file found");
                return;
            }
            val props = new Properties();
            props.load(resNatural.getInputStream());
            parseNaturalTextures(props);

            LOG.debug("Loaded custom natural.properties");

        } catch (IOException ignored) {
            LOG.debug("No natural.properties file found");
        }
    }

    private static void parseNaturalTextures(Properties props) {
        for (String name : props.stringPropertyNames()) {
            String value = props.getProperty(name);
            name = name.trim();
            if (name.startsWith("minecraft:")) {
               name = name.substring(10);
            }
            if(naturalTexturesInfo.containsKey(name)) {
                LOG.warn("Duplicate entry for found in natural.properties: [entry={}]", name);
                continue;
            }

            naturalTexturesInfo.put(name, new NaturalTexturesInfo(value));
        }
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
    public static void applyNaturalTexture(Block block, double x, double y, double z, @NotNull ForgeDirection side, @NotNull IIcon texture,
                                           double[] vertexUs, double[] vertexVs) {
        // TODO filter for valid textures, select a random variation based on the xyz coords (+ maybe seed)

        val texName = texture.getIconName();
        if (!naturalTexturesInfo.containsKey(texName)) return;

        val texInfo = naturalTexturesInfo.get(texName);

        switch (texInfo.rotation()) {
            case Two:
                rotateQuadUVs(Math.PI, texture, vertexUs, vertexVs);
                break;

            case Four:
                rotateQuadUVs(Math.PI / 2D, texture, vertexUs, vertexVs);
                break;

            case None:
            default:
                break;
        }

        if(texInfo.flipHorizontally()) {
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
        rotationAngle %= 2D * Math.PI;

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
