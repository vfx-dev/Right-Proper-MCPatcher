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

package com.falsepattern.mcpatcher.internal.mixin.mixins.client.cit.enchant;

import com.falsepattern.mcpatcher.internal.config.ModuleConfig;
import com.falsepattern.mcpatcher.internal.modules.cit.CITEngine;
import com.falsepattern.mcpatcher.internal.modules.cit.ICITArmorGlintRenderer;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

@Mixin(value = RendererLivingEntity.class,
       priority = 800) // Early for SwanSong compat, let it wrap our methods
public abstract class RendererLivingEntityMixin implements ICITArmorGlintRenderer {
    // TODO: This will require mod compat for ModernWarfare and CustomPlayerModels, see SwanSong hooks
    @WrapOperation(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V",
                            ordinal = 0),
                   require = 1)
    private void captureFirstModelRenderCall(final ModelBase instance,
                                             final Entity entity,
                                             final float limbSwing,
                                             final float limbSwingAmount,
                                             final float ageInTicks,
                                             final float netHeadYaw,
                                             final float headPitch,
                                             final float scale,
                                             final Operation<Void> original,
                                             @Share("renderFn") LocalRef<Runnable> renderFn) {
        if (ModuleConfig.customItemTextures && mcp$isCustomArmorTextureSupported()) {
            renderFn.set(() -> original.call(instance,
                                             entity,
                                             limbSwing,
                                             limbSwingAmount,
                                             ageInTicks,
                                             netHeadYaw,
                                             headPitch,
                                             scale));
            renderFn.get()
                    .run();
        } else {
            original.call(instance, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    //    @Definition(id = "j",
    //                local = @Local(type = int.class,
    //                               ordinal = 0))
    //    @Expression("(j & 15) == 15")
    @Expression("(? & 15) == 15") // TODO: Doesn't work out of dev if I specify the local, some deobf jank?
    @ModifyExpressionValue(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
                           at = @At("MIXINEXTRAS:EXPRESSION"),
                           require = 1)
    private boolean renderArmorGlint(boolean original,
                                     @Local(argsOnly = true) EntityLivingBase entity,
                                     @Local(argsOnly = true,
                                            ordinal = 1) float partialTick,
                                     @Local(ordinal = 1) int i,
                                     @Share("renderFn") LocalRef<Runnable> renderFn) {
        // TODO: Compat with any mod that wants to add a toggle to disable or alter armor glints?
        if (ModuleConfig.customItemTextures && original && mcp$isCustomArmorTextureSupported()) {
            if (renderFn.get() == null) {
                return true;
            }
            val itemStack = mcp$getArmorInSlot(entity, i);
            if (itemStack == null) {
                return true;
            }
            return !CITEngine.renderArmorGlint(entity, itemStack, partialTick, renderFn.get());
        } else {
            return original;
        }
    }
}
