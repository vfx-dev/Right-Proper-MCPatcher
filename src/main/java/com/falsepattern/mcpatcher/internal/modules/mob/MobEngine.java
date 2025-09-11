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

package com.falsepattern.mcpatcher.internal.modules.mob;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * @see <a href="https://bitbucket.org/prupe/mcpatcher/src/master/doc/mob.properties">MCPatcher mob.properties</a>
 */
public class MobEngine {
    private static final Object2ObjectMap<ResourceLocation, ObjectList<MobInfo>> cache = new Object2ObjectOpenHashMap<>();
    private static final ObjectSet<ResourceLocation> negativeCache = new ObjectOpenHashSet<>();
    private static boolean isActive = false;
    private static @Nullable TrackedEntity currentEntity;

    /**
     * @implNote Called after resources have been reloaded.
     */
    public static void reloadResources() {
        isActive = false;
        currentEntity = null;

        negativeCache.clear();
        cache.clear();
    }

    /**
     * Used as a hook when binding a texture while rendering a mob,
     * allowing this module to replace it with an alternative.
     *
     * @param original The original texture
     *
     * @return Either a new {@link ResourceLocation} or the {@code original}
     *
     * @implNote Called before a texture is bound, if {@link #isActive()} returns {@code true}.
     */
    public static ResourceLocation getTexture(ResourceLocation original) {
        assert isActive : "Must be active";
        assert currentEntity != null : "Must have entity";

        if (negativeCache.contains(original)) {
            return original;
        }
        val infos = cache.computeIfAbsent(original, MobInfo::getInfoFor);

        if (infos == null) {
            negativeCache.add(original);
            return original;
        }

        for (val info : infos) {
            if (info.matches(currentEntity)) {
                return info.getTextureFor(currentEntity);
            }
        }

        return original;
    }

    /**
     * Used to guard calls to functions which may alter state such as: {@link #getTexture(ResourceLocation)}
     *
     * @return {@code true} if currently active.
     */
    public static boolean isActive() {
        return isActive;
    }

    /**
     * @implNote Called before entities start rendering.
     */
    public static void beginEntities() {
        isActive = true;
        currentEntity = null;
    }

    /**
     * @implNote Called before rendering the next entity.
     */
    public static void nextEntity(Entity entity) {
        assert isActive : "Must be active";

        if (entity instanceof TrackedEntity) {
            currentEntity = (TrackedEntity) entity;
        }
    }

    /**
     * @implNote Called after entities have finished rendering.
     */
    public static void endEntities() {
        isActive = false;
        currentEntity = null;
    }
}
