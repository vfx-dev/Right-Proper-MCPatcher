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

package com.falsepattern.mcpatcher.internal;

import com.falsepattern.mcpatcher.Tags;
import com.falsepattern.mcpatcher.internal.config.MCPatcherConfig;
import com.falsepattern.mcpatcher.internal.modules.mob.MobEngine;
import lombok.val;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class Proxy {
    public void postInit(FMLPostInitializationEvent event) {
    }

    public static class Server extends Proxy {

    }

    public static class Client extends Proxy {
        private boolean connectedTextures;
        private boolean randomMobs;
        @Override
        public void postInit(FMLPostInitializationEvent event) {
            val mc = Minecraft.getMinecraft();
            val resMan = (IReloadableResourceManager) mc.getResourceManager();
            resMan.registerReloadListener(this::reloadResources);
            connectedTextures = MCPatcherConfig.connectedTextures;
            randomMobs = MCPatcherConfig.randomMobs;
            FMLCommonHandler.instance().bus().register(this);
        }

        @SubscribeEvent
        public void onConfigCache(ConfigChangedEvent.PostConfigChangedEvent e) {
            if (!Tags.MOD_ID.equals(e.modID))
                return;
            //Refresh resources when:
            // - Connected textures are enabled/disabled
            // - Random mobs are disabled (textures are dynamically loaded,
            //   so we reload when disabling to clear vram)
            if (connectedTextures != MCPatcherConfig.connectedTextures ||
                (randomMobs && !MCPatcherConfig.randomMobs)) {
                Minecraft.getMinecraft().scheduleResourcesRefresh();
            }
            connectedTextures = MCPatcherConfig.connectedTextures;
            randomMobs = MCPatcherConfig.randomMobs;
        }

        private void reloadResources(IResourceManager resourceManager) {
            Share.log.debug("Reloading Resources");
            if (MCPatcherConfig.randomMobs && MCPatcherConfig.randomMobsMixins) {
                MobEngine.reloadResources();
            }
        }
    }
}
