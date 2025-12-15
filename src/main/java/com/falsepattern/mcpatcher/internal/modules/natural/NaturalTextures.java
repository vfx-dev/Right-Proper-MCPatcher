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

public class NaturalTextures {
    public static void applyNaturalTexture(Block block, double x, double y, double z, IIcon texture,
                                           double[] vertexUs, double[] vertexVs) {
        // TODO filter for valid textures, select a random variation based on the xyz coords (+ maybe seed)
        remapQuadUVs(vertexUs, vertexVs, 0, true);
    }

    private static void remapQuadUVs(double[] vertexUs, double[] vertexVs, double rotationAngle, boolean mirrorHorizontal) {
        if(!ModuleConfig.naturalTextures) return;

        // TODO handle rotation

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
