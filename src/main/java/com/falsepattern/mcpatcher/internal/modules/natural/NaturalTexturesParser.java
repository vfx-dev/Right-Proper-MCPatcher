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

import com.falsepattern.mcpatcher.internal.modules.common.ResourceScanner;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.val;

import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import static com.falsepattern.mcpatcher.internal.modules.natural.NaturalTexturesEngine.LOG;

public class NaturalTexturesParser {
    private static Pattern emptyLinePattern;
    private static Pattern commentLinePattern;
    private static Pattern entryLinePattern;

    public static Map<String, NaturalTexturesInfo> parseFirstAvailableResource(String... resourceNames) {
        val map = new Object2ObjectOpenHashMap<String, NaturalTexturesInfo>();
        for (val resourceName : resourceNames) {
            IResource resource;
            try {
                resource = ResourceScanner.getResource(new ResourceLocation(resourceName));
                if(resource == null) {
                    continue;
                }
            } catch (IOException ignored) {
                continue;
            }

            try (val br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                compilePatterns();
                String line;
                while ((line = br.readLine()) != null) {
                    parseLine(line, map);
                }

                LOG.debug("Loaded natural textures resource {}", resourceName);
                return map;
            } catch (IOException ignored) {
                LOG.warn("Failed to read natural textures resource {}. Skipping...", resourceName);
            } finally {
                clearPatterns();
            }
        }
        LOG.debug("No natural textures resource available. Resources searched: {}", Arrays.toString(resourceNames));
        return map;
    }

    private static void compilePatterns() {
        emptyLinePattern = Pattern.compile("^\\s+$");
        commentLinePattern = Pattern.compile("(?s)^\\s*[#!].*$");

        // Capture anything that looks like: "(some_characters)=(some_characters)",
        // with optional spaces around (and inside) the two capture groups.
        entryLinePattern = Pattern.compile(
                "^\\s*([\\S&&[^=]]|[\\S&&[^=]][^=]*[\\S&&[^=]])\\s*=\\s*([\\S&&[^=]]|[\\S&&[^=]][^=]*[\\S&&[^=]])\\s*$");
    }

    private static void clearPatterns() {
        emptyLinePattern = null;
        commentLinePattern = null;
        entryLinePattern = null;
    }

    private static void parseLine(String line, Map<String, NaturalTexturesInfo> map) {
        // Is empty line
        if (line.isEmpty() || emptyLinePattern.matcher(line).matches()) return;

        // Is a comment line
        if (commentLinePattern.matcher(line).matches()) return;

        // Capture anything that looks like: "(some_characters)=(some_characters)",
        // with optional spaces around (and inside) the two capture groups.
        val match = entryLinePattern.matcher(line);
        if (match.matches()) {
            buildInfo(match.group(1), match.group(2), map);
        } else {
            LOG.warn("Skipping unparseable line in natural.properties: [line=\"{}\"]", line);
        }
    }

    private static void buildInfo(String key, String value, Map<String, NaturalTexturesInfo> map) {
        if (key.startsWith("minecraft:")) {
            key = key.substring(10);
        }
        if(map.containsKey(key)) {
            LOG.warn("Duplicate entry for found in natural.properties: [entry={}]", key);
            return;
        }

        map.put(key, new NaturalTexturesInfo(value));
    }
}
