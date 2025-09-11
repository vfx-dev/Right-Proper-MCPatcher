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

@Config.Comment("MCPatcher settings")
@Config.LangKey
@Config(modid = Tags.MOD_ID)
public class MCPatcherConfig {
    //@formatter:off
    @Config.Comment({"Runtime toggle for connected textures.",
                     "Requires connectedTexturesMixins enabled."})
    @Config.LangKey
    @Config.Name("connectedTextures")
    @Config.DefaultBoolean(true)
    public static boolean connectedTextures;

    @Config.Comment({"Disable this if you don't want connected texture mixins to land.",
                     "This force-disables connectedTextures."})
    @Config.LangKey
    @Config.Name("connectedTexturesMixins")
    @Config.RequiresMcRestart
    @Config.DefaultBoolean(true)
    public static boolean connectedTexturesMixins;

    @Config.Comment({"Runtime toggle for random mobs.",
                     "Requires randomMobsMixins to be enabled."})
    @Config.LangKey
    @Config.Name("randomMobs")
    @Config.DefaultBoolean(true)
    public static boolean randomMobs;

    @Config.Comment({"Disable this if you don't want random mob mixins to land.",
                     "This force-disables randomMobs."})
    @Config.LangKey
    @Config.Name("randomMobsMixins")
    @Config.RequiresMcRestart
    @Config.DefaultBoolean(true)
    public static boolean randomMobsMixins;

    @Config.Comment({"Resource pack overlay system. Currently used for implementing ctm_compact support.",
                     "Disable if you're getting weird texture loading problems."})
    @Config.LangKey
    @Config.Name("resourcePackOverlay")
    @Config.RequiresMcRestart
    @Config.DefaultBoolean(true)
    public static boolean resourcePackOverlay;

    @Config.Comment("Allow regular glass blocks, glass panes, and beacon glass to have semi-transparent textures")
    @Config.LangKey
    @Config.Name("betterGlass")
    @Config.DefaultBoolean(true)
    public static boolean betterGlass;
    //@formatter:on

    static {
        ConfigurationManager.selfInit();
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
