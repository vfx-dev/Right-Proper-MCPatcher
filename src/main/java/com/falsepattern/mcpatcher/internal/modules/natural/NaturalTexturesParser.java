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

import com.falsepattern.mcpatcher.internal.config.ExtraConfig;
import com.falsepattern.mcpatcher.internal.modules.common.ResourceScanner;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Map;

import static com.falsepattern.mcpatcher.internal.modules.natural.NaturalTexturesEngine.LOG;

public class NaturalTexturesParser {
    public static Map<String, NaturalTexturesInfo> parseResourcePacksInOrder() {
        val map = new Object2ObjectOpenHashMap<String, NaturalTexturesInfo>();
        val packs = ResourceScanner.resourcePacks();

        for (val pack : packs) {
            if (pack == null) {
                continue;
            }
            val names = ResourceScanner.collectFiles(pack, "", "natural.properties", false);
            names.sort(Comparator.naturalOrder());
            for (val name : names) {
                // Prioritize mcpatcher path first, then optifine path for backwards compatibility
                if (!(name.equals("mcpatcher/natural.properties") || name.equals("optifine/natural.properties"))) {
                    continue;
                }

                val loc = new ResourceLocation(name);

                try (val br = new BufferedReader(new InputStreamReader(pack.getInputStream(loc)))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        parseLine(line, map);
                    }

                    LOG.debug("Loaded natural textures resource {} from pack {}", name, pack.getPackName());

                    if(!ExtraConfig.naturalTexturesStack) {
                        return map;
                    }
                } catch (IOException ignored) {
                    LOG.warn("Failed to read natural textures resource {} from pack {}. Skipping...", name, pack.getPackName());
                }
            }
        }

        return map;
    }

    private static void parseLine(String line, Map<String, NaturalTexturesInfo> map) {
        val parsedLine = line.trim();

        if(parsedLine.isEmpty() || StringUtils.startsWithAny(parsedLine, "#", "!")) return;

        boolean validParse = false;
        val parsedSections = StringUtils.split(parsedLine, "=");
        if (parsedSections != null && parsedSections.length == 2) {
            val key = parsedSections[0].trim();
            if (!key.isEmpty()) {
                val value = parsedSections[1].trim();
                if (!value.isEmpty()) {
                    buildInfo(key, value, map);
                    validParse = true;
                }
            }
        }

        if (!validParse) {
            LOG.warn("Skipping unparseable line in natural.properties: [line=\"{}\"]", line);
        }
    }

    private static void buildInfo(String key, String value, Map<String, NaturalTexturesInfo> map) {
        if (key.startsWith("minecraft:")) {
            key = key.substring(10);
        }
        if(map.containsKey(key)) {
            LOG.debug("Skipping pre-existing in for natural.properties: [entry={}]", key);
            return;
        }

        map.put(key, new NaturalTexturesInfo(value));
    }
}
