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

package com.falsepattern.mcpatcher.internal.mixin.mixins.client.mob;

import com.falsepattern.lib.util.MathUtil;
import com.falsepattern.mcpatcher.internal.modules.mob.TrackedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

@Mixin(Entity.class)
public abstract class EntityMixin implements TrackedEntity {
    @Shadow
    public int serverPosX;
    @Shadow
    public int serverPosY;
    @Shadow
    public int serverPosZ;
    @Shadow
    public World worldObj;
    @Shadow
    public double posX;
    @Shadow
    public double posZ;
    @Shadow
    public double posY;
    @Unique
    private int mcp$initX;
    @Unique
    private int mcp$initY;
    @Unique
    private int mcp$initZ;
    @Unique
    private BiomeGenBase mcp$initBiome;
    @Unique
    private boolean mcp$init = false;

    @Unique
    private void mcp$ensureInited() {
        if (worldObj == null) {
            return;
        }
        if (!mcp$init) {
            mcp$initX = MathUtil.floor(posX);
            mcp$initY = MathUtil.floor(posY);
            mcp$initZ = MathUtil.floor(posZ);
            mcp$initBiome = worldObj.getBiomeGenForCoords(mcp$initX, mcp$initZ);
            mcp$init = true;
        }
    }

    @Override
    public int mcp$initialX() {
        mcp$ensureInited();
        return mcp$initX;
    }

    @Override
    public int mcp$initialY() {
        mcp$ensureInited();
        return mcp$initY;
    }

    @Override
    public int mcp$initialZ() {
        mcp$ensureInited();
        return mcp$initZ;
    }

    @Override
    public BiomeGenBase mcp$initialBiome() {
        mcp$ensureInited();
        return mcp$initBiome;
    }
}
