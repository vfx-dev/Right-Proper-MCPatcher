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

package com.falsepattern.mcpatcher.internal.modules.overlay;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class OverlayResourceManager implements IResourceManager {
    private final IResourceManager backing;
    private final @Nullable Map<ResourceLocation, ResourceGenerator> overlayRes;
    private final Map<ResourceLocation, IResource> resolvedOverlayRes = new HashMap<>();

    @Override
    public Set<String> getResourceDomains() {
        return backing.getResourceDomains();
    }

    private IResource getResourceOverlay(ResourceLocation location) throws IOException {
        if (overlayRes == null) {
            return null;
        }
        val res = resolvedOverlayRes.get(location);
        if (res != null) {
            return res;
        }
        val gen = overlayRes.get(location);
        if (gen == null) {
            return null;
        }
        val genRes = gen.gen(this);
        resolvedOverlayRes.put(location, genRes);
        overlayRes.remove(location);
        return genRes;
    }

    @Override
    public IResource getResource(ResourceLocation location) throws IOException {
        val res = getResourceOverlay(location);
        return res != null ? res : backing.getResource(location);
    }

    @Override
    public List<IResource> getAllResources(ResourceLocation location) throws IOException {
        val out = new ArrayList<IResource>();
        val res = getResourceOverlay(location);
        if (res != null) {
            out.add(res);
        }
        out.addAll(backing.getAllResources(location));
        return out;
    }

}
