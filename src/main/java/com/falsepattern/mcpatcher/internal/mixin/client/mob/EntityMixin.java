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

package com.falsepattern.mcpatcher.internal.mixin.client.mob;

import com.falsepattern.lib.util.MathUtil;
import com.falsepattern.mcpatcher.internal.modules.common.MCPMath;
import com.falsepattern.mcpatcher.internal.modules.mob.TrackedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.UUID;

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
    public double posY;
    @Shadow
    public double posZ;

    @Shadow
    public abstract UUID getUniqueID();

    @Unique
    private int mcp$initX;
    @Unique
    private int mcp$initY;
    @Unique
    private int mcp$initZ;
    @Unique
    private BiomeGenBase mcp$initBiome;
    @Unique
    private boolean mcp$init;

    @Unique
    private int mcp$randomMobsSeed;
    @Unique
    private boolean mcp$randomMobsSeedInit;

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

    @Unique
    private void mcp$ensureRandomMobsSeed() {
        if (!mcp$randomMobsSeedInit) {
            UUID uuid = getUniqueID();
            int seed;
            if (uuid != null) {
                long most = uuid.getMostSignificantBits();
                long least = uuid.getLeastSignificantBits();
                seed = (int) (most ^ (most >>> 32) ^ least ^ (least >>> 32));
            } else {
                // Extremely rare fallback: entities without a UUID.
                // Fall back to initial position so we still get a stable value.
                mcp$ensureInited();
                seed = mcp$initX * 73428767 ^ mcp$initY * 9122713 ^ mcp$initZ;
            }
            // Hash and clamp to non-negative for safe modulo / weighting.
            mcp$randomMobsSeed = MCPMath.intHash(seed) & Integer.MAX_VALUE;
            mcp$randomMobsSeedInit = true;
        }
    }

    @Override
    public int mcp$randomMobsSeed() {
        mcp$ensureRandomMobsSeed();
        return mcp$randomMobsSeed;
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
