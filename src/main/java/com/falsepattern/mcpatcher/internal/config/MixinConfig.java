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

@Config.Comment("Game-Start Settings")
@Config.LangKey
@Config(modid = Tags.MOD_ID,
        category = "01_mixins")
@Config.RequiresMcRestart
@NoArgsConstructor
public final class MixinConfig {
    //@formatter:off
    @Config.Comment({"Resource pack overlay system. Currently used for implementing ctm_compact support.",
                     "Disable if you're getting weird texture loading problems."})
    @Config.LangKey
    @Config.Name("resourcePackOverlayMixins")
    @Config.DefaultBoolean(true)
    public static boolean resourcePackOverlayMixins;

    @Config.Comment({"Disable this if you don't want connected texture mixins to land.",
                     "This is required for connectedTextures."})
    @Config.LangKey
    @Config.Name("connectedTexturesMixins")
    @Config.DefaultBoolean(true)
    public static boolean connectedTexturesMixins;

    @Config.Comment({"Disable this if you don't want better glass mixins to land.",
                     "This is required for betterGlass."})
    @Config.LangKey
    @Config.Name("betterGlassMixins")
    @Config.DefaultBoolean(true)
    public static boolean betterGlassMixins;

    @Config.Comment({"Set to Disabled this if you don't want custom item texture mixins to land.",
                     " - Weak: Will still load if some fail to apply",
                     " - Regular: Will crash the game on failure",
                     " - Epic: Uses ASM to rename certain methods",
                     "This is required for customItemTextures."})
    @Config.LangKey
    @Config.Name("customItemTexturesMixins")
    @Config.DefaultEnum("Epic")
    public static CITMixinStrength customItemTexturesMixins;

    @Config.Comment({"Disable this if you don't want random mob mixins to land.",
                     "This is required for randomMobs."})
    @Config.LangKey
    @Config.Name("randomMobsMixins")
    @Config.DefaultBoolean(true)
    public static boolean randomMobsMixins;
    //@formatter:off

    public enum CITMixinStrength {
        Disabled,
        Weak,
        Regular,
        Epic,
    }

    // region Init
    static {
        ConfigurationManager.selfInit();
    }

    /**
     * This is here to make the static initializer run
     */
    public static void init() {
    }
    // endregion
}
