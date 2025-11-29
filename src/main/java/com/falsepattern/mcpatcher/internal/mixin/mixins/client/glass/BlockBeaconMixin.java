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

package com.falsepattern.mcpatcher.internal.mixin.mixins.client.glass;

import com.falsepattern.mcpatcher.internal.config.ModuleConfig;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.BlockBeacon;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;

@Mixin(BlockBeacon.class)
public abstract class BlockBeaconMixin extends BlockContainer {
    protected BlockBeaconMixin(Material materialIn) {
        super(materialIn);
    }

    @Override
    public int getRenderBlockPass() {
        return ModuleConfig.betterGlass ? 1 : super.getRenderBlockPass();
    }
}
