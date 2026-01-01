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

@Config.Comment("Additional Runtime Configurations")
@Config.LangKey
@Config(modid = Tags.MOD_ID,
        category = "02_extras")
@NoArgsConstructor
public final class ExtraConfig {
    //@formatter:off
    @Config.LangKey
    @Config.Name("naturalTexturesStack")
    @Config.DefaultBoolean(true)
    public static boolean naturalTexturesStack;
    //@formatter:on

    // region Init
    static {
        ConfigurationManager.selfInit();
        ModuleConfig.init();
    }

    /**
     * This is here to make the static initializer run
     */
    public static void init() {

    }
    // endregion
}
