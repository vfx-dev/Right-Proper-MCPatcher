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

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.FMLFileResourcePack;
import cpw.mods.fml.client.FMLFolderResourcePack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.falsepattern.mcpatcher.internal.modules.common.CommonParser.LOG;
import static net.minecraft.client.Minecraft.getMinecraft;

public class ResourceScanner {
    private static final String PREFIX_ASSETS = "assets/minecraft/";

    public static @NotNull List<@Nullable IResourcePack> resourcePacks() {
        val manager = (FallbackResourceManager) ((SimpleReloadableResourceManager) getMinecraft().getResourceManager()).domainResourceManagers.get(
                "minecraft");
        return Lists.reverse(manager.resourcePacks);
    }


    public static boolean isFromDefaultResourcePack(@NotNull ResourceLocation loc) {
        val def = definingResourcePack(loc);
        if (def instanceof DefaultResourcePack) {
            return true;
        }
        if (def instanceof FMLFileResourcePack) {
            val fmlDef = (FMLFileResourcePack) def;
            val id = fmlDef.getFMLContainer()
                           .getModId();
            return "Forge".equals(id) || "FML".equals(id);
        }
        if (def instanceof FMLFolderResourcePack) {
            val fmlDef = (FMLFolderResourcePack) def;
            val id = fmlDef.getFMLContainer()
                           .getModId();
            return "Forge".equals(id) || "FML".equals(id);
        }
        return false;
    }

    public static @Nullable IResourcePack definingResourcePack(@NotNull ResourceLocation loc) {
        for (val rp : resourcePacks()) {
            if (rp == null) {
                continue;
            }
            if (rp.resourceExists(loc)) {
                return rp;
            }
        }
        return null;
    }

    public static IResource getResource(ResourceLocation location) throws IOException {
        return getMinecraft().getResourceManager()
                             .getResource(location);
    }

    public static boolean hasResource(ResourceLocation location) {
        try {
            val res = getResource(location);
            return res != null;
        } catch (IOException ignored) {
            return false;
        }
    }

    public static @NotNull ObjectList<@NotNull String> collectFiles(@NotNull IResourcePack pack,
                                                                    @Nullable String prefix,
                                                                    @Nullable String suffix,
                                                                    boolean digitsOnly) {
        if (!(pack instanceof AbstractResourcePack)) {
            return ObjectLists.emptyList();
        }
        File tpFile = ((AbstractResourcePack) pack).resourcePackFile;
        if (tpFile == null) {
            return ObjectLists.emptyList();
        } else if (tpFile.isDirectory()) {
            val res = new ObjectArrayList<String>();
            collectFilesFolder(tpFile, "", prefix, suffix, digitsOnly, res);
            return res;
        } else if (tpFile.isFile()) {
            val res = new ObjectArrayList<String>();
            collectFilesZIP(tpFile, prefix, suffix, digitsOnly, res);
            return res;
        } else {
            return ObjectLists.emptyList();
        }
    }

    private static void collectFilesFolder(@NotNull File tpFile,
                                           @NotNull String basePath,
                                           @Nullable String prefix,
                                           @Nullable String suffix,
                                           boolean digitsOnly,
                                           @NotNull ObjectList<@NotNull String> output) {
        File[] files = tpFile.listFiles();
        if (files == null) {
            return;
        }
        for (val file : files) {
            if (file.isFile()) {
                String name = basePath + file.getName();
                addIfMatches(name, prefix, suffix, digitsOnly, output);
            } else if (file.isDirectory()) {
                String dirPath = basePath + file.getName() + "/";
                collectFilesFolder(file, dirPath, prefix, suffix, digitsOnly, output);
            }
        }
    }

    private static void collectFilesZIP(@NotNull File tpFile,
                                        @Nullable String prefix,
                                        @Nullable String suffix,
                                        boolean digitsOnly,
                                        @NotNull ObjectList<@NotNull String> output) {
        try {
            ZipFile zf = new ZipFile(tpFile);
            val en = zf.entries();

            while (en.hasMoreElements()) {
                ZipEntry ze = en.nextElement();
                String name = ze.getName();
                addIfMatches(name, prefix, suffix, digitsOnly, output);
            }

            zf.close();
        } catch (IOException e) {
            LOG.warn("Error collecting files from zip", e);
        }
    }

    private static void addIfMatches(@NotNull String name,
                                     @Nullable String prefix,
                                     @Nullable String suffix,
                                     boolean digitsOnly,
                                     @NotNull ObjectList<@NotNull String> output) {
        if (name.startsWith(PREFIX_ASSETS)) {
            name = name.substring(PREFIX_ASSETS.length());
            if ((prefix == null || name.startsWith(prefix)) && (suffix == null || name.endsWith(suffix))) {
                if (digitsOnly) {
                    val subStr = name.substring(prefix != null ? prefix.length() : 0,
                                                suffix != null ? name.length() - suffix.length() : name.length());
                    if (!subStr.matches("^\\d+$")) {
                        return;
                    }
                }
                output.add(name);
            }
        }
    }
}
