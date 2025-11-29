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

package com.falsepattern.mcpatcher.internal.asm;

import com.falsepattern.mcpatcher.Tags;
import com.falsepattern.mcpatcher.internal.config.MixinConfig;
import com.falsepattern.mcpatcher.internal.config.ModuleConfig;
import com.falsepattern.mcpatcher.internal.mixin.Mixin;
import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import lombok.val;
import org.intellij.lang.annotations.Language;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.List;
import java.util.Map;
import java.util.Set;

@IFMLLoadingPlugin.Name(Tags.MOD_NAME + "|ASM Plugin")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(200_000)
@IFMLLoadingPlugin.TransformerExclusions("com.falsepattern.mcpatcher.internal.asm")
public final class CoreLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
    @Language(value = "JAVA",
              prefix = "import ",
              suffix = ";")
    private static final String TWEAKER = "com.falsepattern.mcpatcher.internal.asm.MixinCompatHackTweaker";

    public CoreLoadingPlugin() {
        ModuleConfig.init();
    }

    @Override
    public String[] getASMTransformerClass() {
        if (MixinConfig.customItemTexturesMixins == MixinConfig.CITMixinStrength.Epic) {
            val mixinTweakClasses = GlobalProperties.<List<String>>get(MixinServiceLaunchWrapper.BLACKBOARD_KEY_TWEAKCLASSES);
            if (mixinTweakClasses != null) {
                mixinTweakClasses.add(TWEAKER);
            }
        }
        return new String[0];
    }

    @Override
    public String getMixinConfig() {
        return "mixins.mcpatcher.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return IMixins.getEarlyMixins(Mixin.class, loadedCoreMods);
    }

    // region Unused
    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
    // endregion
}
