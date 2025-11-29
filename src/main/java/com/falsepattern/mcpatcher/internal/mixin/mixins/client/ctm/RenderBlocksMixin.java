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

package com.falsepattern.mcpatcher.internal.mixin.mixins.client.ctm;

import com.falsepattern.mcpatcher.internal.config.ModuleConfig;
import com.falsepattern.mcpatcher.internal.modules.ctm.CTMEngine;
import com.falsepattern.mcpatcher.internal.modules.ctm.PaneRenderHelper;
import com.falsepattern.mcpatcher.internal.modules.ctm.Side;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import lombok.val;
import lombok.var;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBeacon;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockRailBase;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import java.lang.ref.SoftReference;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixin {
    @Shadow
    public IIcon overrideBlockTexture;

    @Shadow
    public IBlockAccess blockAccess;
    @Unique
    private SoftReference<PaneRenderHelper> mcp$paneRenderer;

    @ModifyVariable(method = "renderBlockBrewingStand",
                    at = @At(value = "INVOKE",
                             target = "Lnet/minecraft/client/renderer/RenderBlocks;hasOverrideBlockTexture()Z",
                             ordinal = 0),
                    require = 1)
    private IIcon modifyIconBrewingStand(IIcon icon,
                                         @Local(argsOnly = true) BlockBrewingStand block,
                                         @Local(ordinal = 0,
                                                argsOnly = true) int x,
                                         @Local(ordinal = 1,
                                                argsOnly = true) int y,
                                         @Local(ordinal = 2,
                                                argsOnly = true) int z) {
        if (ModuleConfig.connectedTextures && overrideBlockTexture == null) {
            return CTMEngine.getCTMIconMultiPass(blockAccess, block, x, y, z, null, icon);
        }
        return icon;
    }

    @ModifyVariable(method = "renderBlockMinecartTrack",
                    at = @At(value = "INVOKE",
                             target = "Lnet/minecraft/client/renderer/RenderBlocks;hasOverrideBlockTexture()Z",
                             ordinal = 0),
                    require = 1)
    private IIcon modifyIconMinecartTrack(IIcon icon,
                                          @Local(argsOnly = true) BlockRailBase block,
                                          @Local(ordinal = 0,
                                                 argsOnly = true) int x,
                                          @Local(ordinal = 1,
                                                 argsOnly = true) int y,
                                          @Local(ordinal = 2,
                                                 argsOnly = true) int z) {
        if (ModuleConfig.connectedTextures && overrideBlockTexture == null) {
            return CTMEngine.getCTMIconMultiPass(blockAccess, block, x, y, z, Side.YPos, icon);
        }
        return icon;
    }

    @ModifyVariable(method = "renderBlockLadder",
                    at = @At(value = "INVOKE",
                             target = "Lnet/minecraft/client/renderer/RenderBlocks;hasOverrideBlockTexture()Z",
                             ordinal = 0),
                    require = 1)
    private IIcon modifyIconLadder(IIcon icon,
                                   @Local(argsOnly = true) Block block,
                                   @Local(ordinal = 0,
                                          argsOnly = true) int x,
                                   @Local(ordinal = 1,
                                          argsOnly = true) int y,
                                   @Local(ordinal = 2,
                                          argsOnly = true) int z) {
        if (ModuleConfig.connectedTextures && overrideBlockTexture == null) {
            val meta = blockAccess.getBlockMetadata(x, y, z);
            return CTMEngine.getCTMIconMultiPass(blockAccess, block, x, y, z, Side.fromMCDirection(meta), icon);
        }
        return icon;
    }

    @ModifyVariable(method = "renderBlockVine",
                    at = @At(value = "INVOKE",
                             target = "Lnet/minecraft/client/renderer/RenderBlocks;hasOverrideBlockTexture()Z",
                             ordinal = 0),
                    require = 1)
    private IIcon modifyIconVine(IIcon icon,
                                 @Local(argsOnly = true) Block block,
                                 @Local(ordinal = 0,
                                        argsOnly = true) int x,
                                 @Local(ordinal = 1,
                                        argsOnly = true) int y,
                                 @Local(ordinal = 2,
                                        argsOnly = true) int z) {
        if (ModuleConfig.connectedTextures && overrideBlockTexture == null) {
            val meta = blockAccess.getBlockMetadata(x, y, z);
            var side = Side.YNeg;
            if ((meta & 1) != 0) {
                side = Side.ZNeg;
            } else if ((meta & 2) != 0) {
                side = Side.XPos;
            } else if ((meta & 4) != 0) {
                side = Side.ZPos;
            } else if ((meta & 8) != 0) {
                side = Side.XNeg;
            }
            return CTMEngine.getCTMIconMultiPass(blockAccess, block, x, y, z, side, icon);
        }
        return icon;
    }

    @Inject(method = "renderBlockStainedGlassPane",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    public void renderBlockStainedGlassPane(Block block, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        PaneRenderHelper renderer = null;
        if (mcp$paneRenderer != null) {
            renderer = mcp$paneRenderer.get();
        }
        if (renderer == null) {
            renderer = new PaneRenderHelper();
            mcp$paneRenderer = new SoftReference<>(renderer);
        }
        renderer.renderGlassPane((RenderBlocks) (Object) this, block, x, y, z);
        cir.setReturnValue(true);
    }

    @Inject(method = "renderBlockPane",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    public void renderBlockPane(BlockPane block, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        PaneRenderHelper renderer = null;
        if (mcp$paneRenderer != null) {
            renderer = mcp$paneRenderer.get();
        }
        if (renderer == null) {
            renderer = new PaneRenderHelper();
            mcp$paneRenderer = new SoftReference<>(renderer);
        }
        renderer.renderGlassPane((RenderBlocks) (Object) this, block, x, y, z);
        cir.setReturnValue(true);
    }

    @ModifyVariable(method = "renderCrossedSquares",
                    at = @At(value = "STORE",
                             opcode = Opcodes.ASTORE,
                             ordinal = 0),
                    require = 1)
    private IIcon modifyIconCrossedSquares(IIcon icon,
                                           @Local(argsOnly = true) Block block,
                                           @Local(ordinal = 0,
                                                  argsOnly = true) int x,
                                           @Local(ordinal = 1,
                                                  argsOnly = true) int y,
                                           @Local(ordinal = 2,
                                                  argsOnly = true) int z) {
        if (ModuleConfig.connectedTextures && overrideBlockTexture == null) {
            return CTMEngine.getCTMIconMultiPass(blockAccess, block, x, y, z, Side.ZNeg, icon);
        }
        return icon;
    }

    @ModifyVariable(method = "renderBlockLilyPad",
                    at = @At(value = "CONSTANT",
                             args = "floatValue=0.015625",
                             ordinal = 0),
                    require = 1)
    private IIcon modifyIconLilyPad(IIcon icon,
                                    @Local(argsOnly = true) Block block,
                                    @Local(ordinal = 0,
                                           argsOnly = true) int x,
                                    @Local(ordinal = 1,
                                           argsOnly = true) int y,
                                    @Local(ordinal = 2,
                                           argsOnly = true) int z) {
        if (ModuleConfig.connectedTextures && overrideBlockTexture == null) {
            return CTMEngine.getCTMIconMultiPass(blockAccess, block, x, y, z, Side.YPos, icon);
        }
        return icon;
    }

    @WrapOperation(method = "renderBlockBeacon",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/RenderBlocks;setOverrideBlockTexture(Lnet/minecraft/util/IIcon;)V",
                            ordinal = 0),
                   slice = @Slice(from = @At(value = "FIELD",
                                             target = "Lnet/minecraft/init/Blocks;beacon:Lnet/minecraft/block/BlockBeacon;")),
                   require = 1)
    private void modifyIconBeacon(RenderBlocks self,
                                  IIcon icon,
                                  Operation<Void> original,
                                  @Local(argsOnly = true) BlockBeacon block,
                                  @Local(ordinal = 0,
                                         argsOnly = true) int x,
                                  @Local(ordinal = 1,
                                         argsOnly = true) int y,
                                  @Local(ordinal = 2,
                                         argsOnly = true) int z) {
        if (ModuleConfig.connectedTextures) {
            icon = CTMEngine.getCTMIconMultiPass(blockAccess, block, x, y, z, null, icon);
        }
        original.call(self, icon);
    }

    @ModifyVariable(method = "renderFaceXNeg",
                    at = @At("HEAD"),
                    argsOnly = true)
    private IIcon modifyIconXNeg(IIcon icon,
                                 @Local(argsOnly = true) Block block,
                                 @Local(ordinal = 0,
                                        argsOnly = true) double x,
                                 @Local(ordinal = 1,
                                        argsOnly = true) double y,
                                 @Local(ordinal = 2,
                                        argsOnly = true) double z) {
        if (ModuleConfig.connectedTextures && overrideBlockTexture == null) {
            return CTMEngine.getCTMIconMultiPass(blockAccess, block, (int) x, (int) y, (int) z, Side.XNeg, icon);
        }
        return icon;
    }

    @ModifyVariable(method = "renderFaceXPos",
                    at = @At("HEAD"),
                    argsOnly = true)
    private IIcon modifyIconXPos(IIcon icon,
                                 @Local(argsOnly = true) Block block,
                                 @Local(ordinal = 0,
                                        argsOnly = true) double x,
                                 @Local(ordinal = 1,
                                        argsOnly = true) double y,
                                 @Local(ordinal = 2,
                                        argsOnly = true) double z) {
        if (ModuleConfig.connectedTextures && overrideBlockTexture == null) {
            return CTMEngine.getCTMIconMultiPass(blockAccess, block, (int) x, (int) y, (int) z, Side.XPos, icon);
        }
        return icon;
    }

    @ModifyVariable(method = "renderFaceYNeg",
                    at = @At("HEAD"),
                    argsOnly = true)
    private IIcon modifyIconYNeg(IIcon icon,
                                 @Local(argsOnly = true) Block block,
                                 @Local(ordinal = 0,
                                        argsOnly = true) double x,
                                 @Local(ordinal = 1,
                                        argsOnly = true) double y,
                                 @Local(ordinal = 2,
                                        argsOnly = true) double z) {
        if (ModuleConfig.connectedTextures && overrideBlockTexture == null) {
            return CTMEngine.getCTMIconMultiPass(blockAccess, block, (int) x, (int) y, (int) z, Side.YNeg, icon);
        }
        return icon;
    }

    @ModifyVariable(method = "renderFaceYPos",
                    at = @At("HEAD"),
                    argsOnly = true)
    private IIcon modifyIconYPos(IIcon icon,
                                 @Local(argsOnly = true) Block block,
                                 @Local(ordinal = 0,
                                        argsOnly = true) double x,
                                 @Local(ordinal = 1,
                                        argsOnly = true) double y,
                                 @Local(ordinal = 2,
                                        argsOnly = true) double z) {
        if (ModuleConfig.connectedTextures && overrideBlockTexture == null) {
            return CTMEngine.getCTMIconMultiPass(blockAccess, block, (int) x, (int) y, (int) z, Side.YPos, icon);
        }
        return icon;
    }

    @ModifyVariable(method = "renderFaceZNeg",
                    at = @At("HEAD"),
                    argsOnly = true)
    private IIcon modifyIconZNeg(IIcon icon,
                                 @Local(argsOnly = true) Block block,
                                 @Local(ordinal = 0,
                                        argsOnly = true) double x,
                                 @Local(ordinal = 1,
                                        argsOnly = true) double y,
                                 @Local(ordinal = 2,
                                        argsOnly = true) double z) {
        if (ModuleConfig.connectedTextures && overrideBlockTexture == null) {
            return CTMEngine.getCTMIconMultiPass(blockAccess, block, (int) x, (int) y, (int) z, Side.ZNeg, icon);
        }
        return icon;
    }

    @ModifyVariable(method = "renderFaceZPos",
                    at = @At("HEAD"),
                    argsOnly = true)
    private IIcon modifyIconZPos(IIcon icon,
                                 @Local(argsOnly = true) Block block,
                                 @Local(ordinal = 0,
                                        argsOnly = true) double x,
                                 @Local(ordinal = 1,
                                        argsOnly = true) double y,
                                 @Local(ordinal = 2,
                                        argsOnly = true) double z) {
        if (ModuleConfig.connectedTextures && overrideBlockTexture == null) {
            return CTMEngine.getCTMIconMultiPass(blockAccess, block, (int) x, (int) y, (int) z, Side.ZPos, icon);
        }
        return icon;
    }
}
