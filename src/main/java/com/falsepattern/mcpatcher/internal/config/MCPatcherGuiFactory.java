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

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.SimpleGuiConfig;
import com.falsepattern.lib.config.SimpleGuiFactory;
import com.falsepattern.mcpatcher.Tags;
import lombok.NoArgsConstructor;
import lombok.val;

import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;

@NoArgsConstructor
public final class MCPatcherGuiFactory implements SimpleGuiFactory {
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return MCPatcherGuiConfig.class;
    }

    private static Class<?>[] getConfigClasses() {
        val result = new ArrayList<Class<?>>();
        result.add(ModuleConfig.class);
        result.add(MixinConfig.class);
        return result.toArray(new Class<?>[0]);
    }

    public static class MCPatcherGuiConfig extends SimpleGuiConfig {
        public MCPatcherGuiConfig(GuiScreen parent) throws ConfigException {
            super(parent, Tags.MOD_ID, Tags.MOD_NAME, getConfigClasses());
        }
    }
}
