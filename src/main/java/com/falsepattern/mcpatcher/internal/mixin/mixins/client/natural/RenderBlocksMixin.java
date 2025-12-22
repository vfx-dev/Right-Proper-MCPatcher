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

package com.falsepattern.mcpatcher.internal.mixin.mixins.client.natural;

import com.falsepattern.mcpatcher.internal.config.ModuleConfig;
import com.falsepattern.mcpatcher.internal.modules.natural.NaturalTexturesEngine;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixin {

    @Unique
    private double[] mcp$vertexUs = new double[4];

    @Unique
    private double[] mcp$vertexVs = new double[4];

    @Unique
    private void mcp$captureVertexes(Block block, int x, int y, int z, ForgeDirection side, @Nullable IIcon texture,
                                     double uA, double vA, double uB, double vB,
                                     double uC, double vC, double uD, double vD) {
        mcp$vertexUs[0] = uA;
        mcp$vertexVs[0] = vA;

        mcp$vertexUs[1] = uB;
        mcp$vertexVs[1] = vB;

        mcp$vertexUs[2] = uC;
        mcp$vertexVs[2] = vC;

        mcp$vertexUs[3] = uD;
        mcp$vertexVs[3] = vD;

        if(texture == null) return;

        NaturalTexturesEngine.applyNaturalTexture(block, x, y, z, side, texture, mcp$vertexUs, mcp$vertexVs);
    }

    /** UV Capturing Mixins */

    // Down
    @Inject(method = "renderFaceYNeg", at = @At(value = "FIELD",
                                                target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                                shift = At.Shift.BEFORE,
                                                opcode = Opcodes.GETFIELD))
    private void swizzleVerticesYNeg(Block block, double x, double y, double z, IIcon texture, CallbackInfo ci,
                                   @Local(ordinal = 3) double d3, @Local(ordinal = 4) double d4, @Local(ordinal = 5) double d5,
                                   @Local(ordinal = 6) double d6, @Local(ordinal = 7) double d7, @Local(ordinal = 8) double d8,
                                   @Local(ordinal = 9) double d9, @Local(ordinal = 10) double d10) {

        if(!ModuleConfig.naturalTextures) return;
        mcp$captureVertexes(block, (int) x, (int) y, (int) z, ForgeDirection.DOWN, texture, d8, d10, d4, d6, d7, d9, d3, d5);

        double swap = mcp$vertexUs[0];
        mcp$vertexUs[0] = mcp$vertexUs[3];
        mcp$vertexUs[3] = swap;

        swap = mcp$vertexVs[0];
        mcp$vertexVs[0] = mcp$vertexVs[3];
        mcp$vertexVs[3] = swap;
    }

    // Up
    @Inject(method = "renderFaceYPos", at = @At(value = "FIELD",
                                                target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                                shift = At.Shift.BEFORE,
                                                opcode = Opcodes.GETFIELD))
    private void swizzleVerticesYPos(Block block, double x, double y, double z, IIcon texture, CallbackInfo ci,
                                   @Local(ordinal = 3) double d3, @Local(ordinal = 4) double d4, @Local(ordinal = 5) double d5,
                                   @Local(ordinal = 6) double d6, @Local(ordinal = 7) double d7, @Local(ordinal = 8) double d8,
                                   @Local(ordinal = 9) double d9, @Local(ordinal = 10) double d10) {

        if(!ModuleConfig.naturalTextures) return;
        mcp$captureVertexes(block, (int) x, (int) y, (int) z, ForgeDirection.UP, texture, d4, d6, d3, d5, d7, d9, d8, d10);

        double swap = mcp$vertexUs[0];
        mcp$vertexUs[0] = mcp$vertexUs[1];
        mcp$vertexUs[1] = swap;

        swap = mcp$vertexVs[0];
        mcp$vertexVs[0] = mcp$vertexVs[1];
        mcp$vertexVs[1] = swap;
    }

    // North
    @Inject(method = "renderFaceZNeg", at = @At(value = "FIELD",
                                                target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                                shift = At.Shift.BEFORE,
                                                opcode = Opcodes.GETFIELD))
    private void swizzleVerticesZNeg(Block block, double x, double y, double z, IIcon texture, CallbackInfo ci,
                                   @Local(ordinal = 3) double d3, @Local(ordinal = 4) double d4, @Local(ordinal = 5) double d5,
                                   @Local(ordinal = 6) double d6, @Local(ordinal = 7) double d7, @Local(ordinal = 8) double d8,
                                   @Local(ordinal = 9) double d9, @Local(ordinal = 10) double d10) {

        if(!ModuleConfig.naturalTextures) return;
        mcp$captureVertexes(block, (int) x, (int) y, (int) z, ForgeDirection.NORTH, texture, d3, d5, d7, d9, d4, d6, d8, d10);

        double swap = mcp$vertexUs[1];
        mcp$vertexUs[1] = mcp$vertexUs[2];
        mcp$vertexUs[2] = swap;

        swap = mcp$vertexVs[1];
        mcp$vertexVs[1] = mcp$vertexVs[2];
        mcp$vertexVs[2] = swap;
    }

    // South
    @Inject(method = "renderFaceZPos", at = @At(value = "FIELD",
                                                target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                                shift = At.Shift.BEFORE,
                                                opcode = Opcodes.GETFIELD))
    private void swizzleVerticesZPos(Block block, double x, double y, double z, IIcon texture, CallbackInfo ci,
                                   @Local(ordinal = 3) double d3, @Local(ordinal = 4) double d4, @Local(ordinal = 5) double d5,
                                   @Local(ordinal = 6) double d6, @Local(ordinal = 7) double d7, @Local(ordinal = 8) double d8,
                                   @Local(ordinal = 9) double d9, @Local(ordinal = 10) double d10) {

        if(!ModuleConfig.naturalTextures) return;
        mcp$captureVertexes(block, (int) x, (int) y, (int) z, ForgeDirection.SOUTH, texture, d3, d5, d4, d6, d8, d10, d7, d9);

        double swap = mcp$vertexUs[2];
        mcp$vertexUs[2] = mcp$vertexUs[3];
        mcp$vertexUs[3] = swap;

        swap = mcp$vertexVs[2];
        mcp$vertexVs[2] = mcp$vertexVs[3];
        mcp$vertexVs[3] = swap;
    }

    // West
    @Inject(method = "renderFaceXNeg", at = @At(value = "FIELD",
                                                target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                                shift = At.Shift.BEFORE,
                                                opcode = Opcodes.GETFIELD))
    private void swizzleVerticesXNeg(Block block, double x, double y, double z, IIcon texture, CallbackInfo ci,
                                   @Local(ordinal = 3) double d3, @Local(ordinal = 4) double d4, @Local(ordinal = 5) double d5,
                                   @Local(ordinal = 6) double d6, @Local(ordinal = 7) double d7, @Local(ordinal = 8) double d8,
                                   @Local(ordinal = 9) double d9, @Local(ordinal = 10) double d10) {

        if(!ModuleConfig.naturalTextures) return;
        mcp$captureVertexes(block, (int) x, (int) y, (int) z, ForgeDirection.WEST, texture, d3, d5, d7, d9, d4, d6, d8, d10);

        double swap = mcp$vertexUs[1];
        mcp$vertexUs[1] = mcp$vertexUs[2];
        mcp$vertexUs[2] = swap;

        swap = mcp$vertexVs[1];
        mcp$vertexVs[1] = mcp$vertexVs[2];
        mcp$vertexVs[2] = swap;
    }

    // East
    @Inject(method = "renderFaceXPos", at = @At(value = "FIELD",
                                                target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                                shift = At.Shift.BEFORE,
                                                opcode = Opcodes.GETFIELD))
    private void swizzleVerticesXPos(Block block, double x, double y, double z, IIcon texture, CallbackInfo ci,
                                   @Local(ordinal = 3) double d3, @Local(ordinal = 4) double d4, @Local(ordinal = 5) double d5,
                                   @Local(ordinal = 6) double d6, @Local(ordinal = 7) double d7, @Local(ordinal = 8) double d8,
                                   @Local(ordinal = 9) double d9, @Local(ordinal = 10) double d10) {

        if(!ModuleConfig.naturalTextures) return;
        mcp$captureVertexes(block, (int) x, (int) y, (int) z, ForgeDirection.EAST, texture, d3, d5, d7, d9, d4, d6, d8, d10);

        double swap = mcp$vertexUs[1];
        mcp$vertexUs[1] = mcp$vertexUs[2];
        mcp$vertexUs[2] = swap;

        swap = mcp$vertexVs[1];
        mcp$vertexVs[1] = mcp$vertexVs[2];
        mcp$vertexVs[2] = swap;
    }


    /** UV apply Mixins */
    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    ordinal = 3, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          shift = At.Shift.AFTER,
                                          opcode = Opcodes.GETFIELD))
    private double overwriteUD3(double d3) {
        return ModuleConfig.naturalTextures ? mcp$vertexUs[0] : d3;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    ordinal = 4, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          shift = At.Shift.AFTER,
                                          opcode = Opcodes.GETFIELD))
    private double overwriteUD4(double d4) {
        return ModuleConfig.naturalTextures ? mcp$vertexUs[1] : d4;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    ordinal = 5, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          shift = At.Shift.AFTER,
                                          opcode = Opcodes.GETFIELD))
    private double overwriteVD5(double d5) {
        return ModuleConfig.naturalTextures ? mcp$vertexVs[0] : d5;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    ordinal = 6, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          shift = At.Shift.AFTER,
                                          opcode = Opcodes.GETFIELD))
    private double overwriteVD6(double d6) {
        return ModuleConfig.naturalTextures ? mcp$vertexVs[1] : d6;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    ordinal = 7, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          shift = At.Shift.AFTER,
                                          opcode = Opcodes.GETFIELD))
    private double overwriteUD7(double d7) {
        return ModuleConfig.naturalTextures ? mcp$vertexUs[2] : d7;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    ordinal = 8, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          shift = At.Shift.AFTER,
                                          opcode = Opcodes.GETFIELD))
    private double overwriteUD8(double d8) {
        return ModuleConfig.naturalTextures ? mcp$vertexUs[3] : d8;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    ordinal = 9, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          shift = At.Shift.AFTER,
                                          opcode = Opcodes.GETFIELD))
    private double overwriteVD9(double d9) {
        return ModuleConfig.naturalTextures ? mcp$vertexVs[2] : d9;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    ordinal = 10, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          shift = At.Shift.AFTER,
                                          opcode = Opcodes.GETFIELD))
    private double overwriteVD10(double d10) {
        return ModuleConfig.naturalTextures ? mcp$vertexVs[3] : d10;
    }
}
