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

import com.falsepattern.mcpatcher.internal.modules.common.CommonParser;
import com.falsepattern.mcpatcher.internal.modules.common.IntRange;
import com.falsepattern.mcpatcher.internal.modules.common.MCPMath;
import com.falsepattern.mcpatcher.internal.modules.common.ResourceScanner;
import com.falsepattern.mcpatcher.internal.modules.common.WeightedRandom;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;

import java.io.IOException;
import java.util.Comparator;
import java.util.Properties;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MobInfo {
    private static final String ENTITY_PATH_PREFIX = "textures/entity/";
    private static final String MCPATCHER_MOB_PATH_PREFIX = "mcpatcher/mob/";

    /**
     * Aliases for properties so people can use just one
     */
    private static final String[] PROP_ALIASES = {"",
                                                  "_eyes",
                                                  "_overlay",
                                                  "_tame",
                                                  "_angry",
                                                  "_collar",
                                                  "_fur",
                                                  "_invulnerable",
                                                  "_shooting"};

    private final @NotNull String name;
    private final @NotNull ResourceLocation @NotNull [] textures;
    private final @Nullable WeightedRandom weights;
    private final @Nullable IntRange.List heights;
    private final @Nullable ObjectSet<BiomeGenBase> biomes;

    private static int getRandom(int x, int y, int z) {
        int rand = MCPMath.intHash(x);
        rand = MCPMath.intHash(rand + z);
        rand = MCPMath.intHash(rand + y);
        return rand;
    }

    public static @Nullable ObjectList<@NotNull MobInfo> getInfoFor(@NotNull ResourceLocation resource) {
        val domain = resource.getResourceDomain();
        if (!"minecraft".equals(domain)) {
            return null;
        }
        MobEngine.LOG.debug("Loading resources for: {}", resource);

        val path = resource.getResourcePath();
        if (path == null || !path.startsWith(ENTITY_PATH_PREFIX)) {
            return null;
        }
        var subPath = path.substring(ENTITY_PATH_PREFIX.length());
        if (subPath.endsWith(".png")) {
            subPath = subPath.substring(0, subPath.length() - 4);
        }
        val prefix = MCPATCHER_MOB_PATH_PREFIX + subPath;
        val packs = ResourceScanner.resourcePacks();
        for (val pack : packs) {
            if (pack == null) {
                continue;
            }
            val hasBaseTexture = pack.resourceExists(resource);
            ObjectList<MobInfo> result = null;
            for (val alias : PROP_ALIASES) {
                val aliasedPrefix = prefix.substring(0, prefix.length() - alias.length());
                val propFile = new ResourceLocation(aliasedPrefix + ".properties");
                if (!pack.resourceExists(propFile)) {
                    continue;
                }

                val properties = new Properties();
                try {
                    MobEngine.LOG.debug("From prop file: {}", propFile);
                    properties.load(pack.getInputStream(propFile));
                } catch (IOException e) {
                    CommonParser.LOG.warn("Failed to parse {}", propFile.toString());
                    CommonParser.LOG.warn("Stacktrace:", e);
                }
                result = fromProperties(properties, resource, prefix);
            }
            if (result == null) {
                val info = fromTextures(resource, pack, prefix);
                if (info != null) {
                    result = ObjectLists.singleton(info);
                }
            }
            if (result != null) {
                return result;
            }
            if (hasBaseTexture) {
                return null;
            }
        }
        return null;
    }

    private static @NotNull ObjectList<@NotNull MobInfo> fromProperties(@NotNull Properties props,
                                                                        @NotNull ResourceLocation resource,
                                                                        @NotNull String prefix) {
        val baseName = StringUtils.substringAfterLast(prefix, "/") + ".";
        val result = new ObjectArrayList<MobInfo>();
        for (var i = 1; i < Integer.MAX_VALUE; i++) {
            val skins = CommonParser.parseInts(props.getProperty("skins." + i), 0, 0xFFFF);
            if (skins == null) {
                break;
            }
            val weightInts = CommonParser.parseInts(props.getProperty("weights." + i), 0, 0xFFFF);
            val biomesList = CommonParser.parseBiomes(props.getProperty("biomes." + i));

            var heights = CommonParser.parseIntRanges(props.getProperty("heights." + i));
            if (heights == null) {
                val minHeight = CommonParser.parseInt(props.getProperty("minHeight." + i), Integer.MIN_VALUE);
                val maxHeight = CommonParser.parseInt(props.getProperty("maxHeight." + i), Integer.MAX_VALUE);
                if (minHeight != Integer.MIN_VALUE || maxHeight != Integer.MAX_VALUE) {
                    heights = new IntRange.List();
                    heights.add(new IntRange(minHeight, maxHeight));
                }
            }

            var weights = weightInts == null ? null : new WeightedRandom(weightInts, skins.size() + 1);
            val biomes = biomesList == null ? null : new ObjectOpenHashSet<>(biomesList);
            val skinsList = new ObjectArrayList<ResourceLocation>();
            val iter = skins.intIterator();
            while (iter.hasNext()) {
                val j = iter.nextInt();
                val replacementResource = new ResourceLocation(prefix + j + ".png");
                if (ResourceScanner.hasResource(replacementResource)) {
                    skinsList.add(replacementResource);
                    MobEngine.LOG.debug("Added custom texture: {}", replacementResource);
                } else if (j == 1) {
                    skinsList.add(resource);
                    MobEngine.LOG.debug("Added default texture: {}", resource);
                } else {
                    MobEngine.LOG.warn("Missing custom texture: {}", replacementResource);
                }
            }
            result.add(new MobInfo(baseName + i, skinsList.toArray(new ResourceLocation[0]), weights, heights, biomes));
        }
        result.add(new MobInfo(baseName + 0, new ResourceLocation[]{resource}, null, null, null));
        return result;
    }

    private static @Nullable MobInfo fromTextures(@NotNull ResourceLocation resource,
                                                  @NotNull IResourcePack pack,
                                                  @NotNull String prefix) {
        val names = ResourceScanner.collectFiles(pack, prefix, ".png", true);
        if (names.isEmpty()) {
            return null;
        }
        MobEngine.LOG.debug("From path directly");
        names.sort(Comparator.naturalOrder());
        val res = new ObjectArrayList<ResourceLocation>();
        res.add(resource);
        MobEngine.LOG.debug("Added default texture: {}", resource);
        for (val name : names) {
            val replacementResource = new ResourceLocation(name);
            if (ResourceScanner.hasResource(replacementResource)) {
                res.add(replacementResource);
                MobEngine.LOG.debug("Added custom texture: {}", replacementResource);
            } else {
                MobEngine.LOG.warn("Missing custom texture: {}", replacementResource);
            }
        }
        val name = StringUtils.substringAfterLast(prefix, "/") + ".0";
        return new MobInfo(name, res.toArray(new ResourceLocation[0]), null, null, null);
    }

    public ResourceLocation getTextureFor(TrackedEntity entity) {
        val rand = getRandom(entity.mcp$initialX(), entity.mcp$initialY(), entity.mcp$initialZ());
        int index;
        if (weights == null) {
            index = rand % textures.length;
        } else {
            index = weights.getIndex(rand) % textures.length;
        }
        return textures[index];
    }

    public boolean matches(TrackedEntity entity) {
        return (heights == null || heights.isInRange(entity.mcp$initialY())) &&
               (biomes == null || biomes.contains(entity.mcp$initialBiome()));
    }
}
