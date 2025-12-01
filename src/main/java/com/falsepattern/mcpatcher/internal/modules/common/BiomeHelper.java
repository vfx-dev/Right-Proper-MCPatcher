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

package com.falsepattern.mcpatcher.internal.modules.common;

import com.falsepattern.mcpatcher.Tags;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.val;
import lombok.var;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.biome.BiomeGenBase;

public final class BiomeHelper {
    private static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + " BiomeHelper");

    private static final Object2ObjectMap<String, BiomeGenBase> name2Biome = new Object2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<BiomeGenBase> id2Biome = new Int2ObjectOpenHashMap<>();

    private BiomeHelper() {
        throw new UnsupportedOperationException();
    }

    public static void mapBiomes() {
        LOG.info("Mapping biomes");
        val biomes = BiomeGenBase.getBiomeGenArray();

        name2Biome.clear();
        id2Biome.clear();

        var numFound = 0;
        val mappedList = new ObjectArrayList<BiomeGenBase>(biomes.length);
        val unmappedList = new ObjectArrayList<BiomeGenBase>(biomes.length);
        for (var index = 0; index < biomes.length; index++) {
            try {
                val biome = biomes[index];
                if (biome == null) {
                    continue;
                }
                val name = biome.biomeName;
                val id = biome.biomeID;
                numFound++;
                LOG.trace("Found biome: [name={},id={},index={}]", name, id, index);

                var mapped = false;
                if (name != null) {
                    val nameKey = name.replace(" ", "")
                                      .toLowerCase();
                    name2Biome.put(nameKey, biome);
                    LOG.trace("Mapped biome [nameKey={}]", nameKey);
                    mapped = true;
                } else {
                    LOG.warn("Biome with null name: [name={},id={},index={}] name will not be mapped", name, id, index);
                }

                if (index == id) {
                    id2Biome.put(id, biome);
                    LOG.trace("Mapped biome [id={}]", id);
                    mapped = true;
                } else if (biomes[id] == biome) {
                    id2Biome.put(id, biome);
                    LOG.trace("Mapped biome [id={}], potential duplicate?", id);
                    mapped = true;
                } else {
                    LOG.warn("Biome with index mismatch: [name={},id={},index={}] id will not be mapped", name, id, index);
                }

                if (mapped) {
                    LOG.debug("Mapped biome: [name={},id={},index={}]", name, id, index);
                    mappedList.add(biome);
                } else {
                    LOG.warn("Failed to map biome: [name={},id={},index={}]", name, id, index);
                    unmappedList.add(biome);
                }
            } catch (RuntimeException e) {
                LOG.error("Exception collecting biome index: {}", index);
                LOG.error("Trace: ", e);
            }
        }
        LOG.info("Done mapping biomes");

        if (!mappedList.isEmpty()) {
            LOG.info("----");
            LOG.info("Successfully Mapped: [{}]", mappedList.size());
            for (val biome : mappedList) {
                //noinspection LoggingSimilarMessage
                LOG.info("\t[name={},id={}]", biome.biomeName, biome.biomeID);
            }
            LOG.info("----");
        }

        if (!unmappedList.isEmpty()) {
            LOG.info("----");
            LOG.info("Failed to Map: [{}]", unmappedList.size());
            for (val biome : unmappedList) {
                //noinspection LoggingSimilarMessage
                LOG.info("\t[name={},id={}]", biome.biomeName, biome.biomeID);
            }
            LOG.info("----");
        }
        LOG.info("Mapped biomes: [found={},mapped={},unmapped={}]", numFound, mappedList.size(), unmappedList.size());
    }

    public static @Nullable BiomeGenBase biomeFromName(@Nullable String name) {
        if (name == null) {
            return null;
        }
        val nameKey = name.replace(" ", "")
                          .toLowerCase();
        return name2Biome.get(nameKey);
    }

    public static @Nullable BiomeGenBase biomeFromId(int id) {
        return id2Biome.get(id);
    }
}
