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

package com.falsepattern.mcpatcher.internal.mixin.client.mob;

import com.falsepattern.mcpatcher.internal.config.MCPatcherConfig;
import com.falsepattern.mcpatcher.internal.modules.mob.MobEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

@Mixin(value = TextureManager.class,
       priority = 800) // Early, so other injections get the right idea.
public abstract class TextureManagerMixin {
    @ModifyVariable(method = "bindTexture",
                    at = @At("HEAD"),
                    argsOnly = true,
                    require = 1)
    private ResourceLocation mob_getTexture(ResourceLocation original) {
        if (MCPatcherConfig.randomMobs && MobEngine.isActive()) {
            return MobEngine.getTexture(original);
        }
        return original;
    }
}
