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

package com.falsepattern.mcpatcher.internal.config;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.mcpatcher.Tags;
import lombok.NoArgsConstructor;

@Config.Comment("Runtime Module Toggles")
@Config.LangKey
@Config(modid = Tags.MOD_ID,
        category = "00_modules")
@NoArgsConstructor
public final class ModuleConfig {
    //@formatter:off
    @Config.Comment({"Runtime toggle for connected textures.",
                     "Requires connectedTexturesMixins enabled."})
    @Config.LangKey
    @Config.Name("connectedTextures")
    @Config.DefaultBoolean(true)
    public static boolean connectedTextures;

    @Config.Comment({"Allow regular glass blocks, glass panes, and beacon glass to have semi-transparent textures.",
                     "Requires betterGlassMixins enabled."})
    @Config.LangKey
    @Config.Name("betterGlass")
    @Config.DefaultBoolean(true)
    public static boolean betterGlass;

    @Config.Comment({"Runtime toggle for random mobs.",
                     "Requires randomMobsMixins to be enabled."})
    @Config.LangKey
    @Config.Name("randomMobs")
    @Config.DefaultBoolean(true)
    public static boolean randomMobs;

    @Config.Comment({"Runtime toggle for custom item textures.",
                     "Requires customItemTexturesMixins enabled."})
    @Config.LangKey
    @Config.Name("customItemTextures")
    @Config.DefaultBoolean(true)
    public static boolean customItemTextures;
    //@formatter:on

    public static boolean isConnectedTexturesEnabled() {
        return MixinConfig.connectedTexturesMixins && connectedTextures;
    }

    public static boolean isBetterGlassEnabled() {
        return MixinConfig.betterGlassMixins && betterGlass;
    }

    public static boolean isCustomItemTexturesEnabled() {
        return MixinConfig.customItemTexturesMixins != MixinConfig.CITMixinStrength.Disabled && customItemTextures;
    }

    public static boolean isRandomMobsEnabled() {
        return MixinConfig.randomMobsMixins && randomMobs;
    }

    // region Init
    static {
        ConfigurationManager.selfInit();
        MixinConfig.init();
    }

    /**
     * This is here to make the static initializer run
     */
    public static void init() {

    }
    // endregion
}
