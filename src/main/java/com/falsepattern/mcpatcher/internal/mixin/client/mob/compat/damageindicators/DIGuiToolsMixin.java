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

package com.falsepattern.mcpatcher.internal.mixin.client.mob.compat.damageindicators;

import DamageIndicatorsMod.gui.DIGuiTools;
import com.falsepattern.mcpatcher.internal.modules.mob.MobEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.EntityLivingBase;

@Mixin(DIGuiTools.class)
public abstract class DIGuiToolsMixin extends GuiIngame {
    public DIGuiToolsMixin(Minecraft p_i1036_1_) {
        super(p_i1036_1_);
    }

    @Inject(method = "renderEntity",
            at = @At("HEAD"),
            remap = false,
            require = 1)
    private static void preRenderEntity(EntityLivingBase el, CallbackInfo ci) {
        MobEngine.beginEntities();
        MobEngine.nextEntity(el);
    }

    @Inject(method = "renderEntity",
            at = @At("RETURN"),
            remap = false,
            require = 1)
    private static void postRenderEntity(EntityLivingBase el, CallbackInfo ci) {
        MobEngine.endEntities();
    }
}
