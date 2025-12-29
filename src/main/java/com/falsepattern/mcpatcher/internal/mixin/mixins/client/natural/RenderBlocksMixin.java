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
import com.falsepattern.mcpatcher.internal.modules.common.Side;
import com.falsepattern.mcpatcher.internal.modules.natural.NaturalTexturesEngine;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
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
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixin {

    @Unique
    private final double[] mcp$vertexUs = new double[4];

    @Unique
    private final double[] mcp$vertexVs = new double[4];

    @Unique
    private void mcp$captureVertexes(Block block, int x, int y, int z, Side side, @Nullable IIcon texture,
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

    /**  Standard block render: UV Capturing Mixins */

    // Down
    @Inject(method = "renderFaceYNeg", require = 1, at = @At(value = "FIELD",
                                                target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                                opcode = Opcodes.GETFIELD))
    private void swizzleVerticesYNeg(Block block, double x, double y, double z, IIcon texture, CallbackInfo ci,
                                   @Local(ordinal = 3) double d3, @Local(ordinal = 4) double d4, @Local(ordinal = 5) double d5,
                                   @Local(ordinal = 6) double d6, @Local(ordinal = 7) double d7, @Local(ordinal = 8) double d8,
                                   @Local(ordinal = 9) double d9, @Local(ordinal = 10) double d10) {

        if(!ModuleConfig.naturalTextures) return;
        mcp$captureVertexes(block, (int) x, (int) y, (int) z, Side.YNeg, texture, d8, d10, d4, d6, d7, d9, d3, d5);

        double swap = mcp$vertexUs[0];
        mcp$vertexUs[0] = mcp$vertexUs[3];
        mcp$vertexUs[3] = swap;

        swap = mcp$vertexVs[0];
        mcp$vertexVs[0] = mcp$vertexVs[3];
        mcp$vertexVs[3] = swap;
    }

    // Up
    @Inject(method = "renderFaceYPos", require = 1, at = @At(value = "FIELD",
                                                target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                                opcode = Opcodes.GETFIELD))
    private void swizzleVerticesYPos(Block block, double x, double y, double z, IIcon texture, CallbackInfo ci,
                                   @Local(ordinal = 3) double d3, @Local(ordinal = 4) double d4, @Local(ordinal = 5) double d5,
                                   @Local(ordinal = 6) double d6, @Local(ordinal = 7) double d7, @Local(ordinal = 8) double d8,
                                   @Local(ordinal = 9) double d9, @Local(ordinal = 10) double d10) {

        if(!ModuleConfig.naturalTextures) return;
        mcp$captureVertexes(block, (int) x, (int) y, (int) z, Side.YPos, texture, d4, d6, d3, d5, d7, d9, d8, d10);

        double swap = mcp$vertexUs[0];
        mcp$vertexUs[0] = mcp$vertexUs[1];
        mcp$vertexUs[1] = swap;

        swap = mcp$vertexVs[0];
        mcp$vertexVs[0] = mcp$vertexVs[1];
        mcp$vertexVs[1] = swap;
    }

    // North
    @Inject(method = "renderFaceZNeg", require = 1, at = @At(value = "FIELD",
                                                target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                                opcode = Opcodes.GETFIELD))
    private void swizzleVerticesZNeg(Block block, double x, double y, double z, IIcon texture, CallbackInfo ci,
                                   @Local(ordinal = 3) double d3, @Local(ordinal = 4) double d4, @Local(ordinal = 5) double d5,
                                   @Local(ordinal = 6) double d6, @Local(ordinal = 7) double d7, @Local(ordinal = 8) double d8,
                                   @Local(ordinal = 9) double d9, @Local(ordinal = 10) double d10) {

        if(!ModuleConfig.naturalTextures) return;
        mcp$captureVertexes(block, (int) x, (int) y, (int) z, Side.ZNeg, texture, d3, d5, d7, d9, d4, d6, d8, d10);

        double swap = mcp$vertexUs[1];
        mcp$vertexUs[1] = mcp$vertexUs[2];
        mcp$vertexUs[2] = swap;

        swap = mcp$vertexVs[1];
        mcp$vertexVs[1] = mcp$vertexVs[2];
        mcp$vertexVs[2] = swap;
    }

    // South
    @Inject(method = "renderFaceZPos", require = 1, at = @At(value = "FIELD",
                                                target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                                opcode = Opcodes.GETFIELD))
    private void swizzleVerticesZPos(Block block, double x, double y, double z, IIcon texture, CallbackInfo ci,
                                   @Local(ordinal = 3) double d3, @Local(ordinal = 4) double d4, @Local(ordinal = 5) double d5,
                                   @Local(ordinal = 6) double d6, @Local(ordinal = 7) double d7, @Local(ordinal = 8) double d8,
                                   @Local(ordinal = 9) double d9, @Local(ordinal = 10) double d10) {

        if(!ModuleConfig.naturalTextures) return;
        mcp$captureVertexes(block, (int) x, (int) y, (int) z, Side.ZPos, texture, d3, d5, d4, d6, d8, d10, d7, d9);

        double swap = mcp$vertexUs[2];
        mcp$vertexUs[2] = mcp$vertexUs[3];
        mcp$vertexUs[3] = swap;

        swap = mcp$vertexVs[2];
        mcp$vertexVs[2] = mcp$vertexVs[3];
        mcp$vertexVs[3] = swap;
    }

    // West
    @Inject(method = "renderFaceXNeg", require = 1, at = @At(value = "FIELD",
                                                target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                                opcode = Opcodes.GETFIELD))
    private void swizzleVerticesXNeg(Block block, double x, double y, double z, IIcon texture, CallbackInfo ci,
                                   @Local(ordinal = 3) double d3, @Local(ordinal = 4) double d4, @Local(ordinal = 5) double d5,
                                   @Local(ordinal = 6) double d6, @Local(ordinal = 7) double d7, @Local(ordinal = 8) double d8,
                                   @Local(ordinal = 9) double d9, @Local(ordinal = 10) double d10) {

        if(!ModuleConfig.naturalTextures) return;
        mcp$captureVertexes(block, (int) x, (int) y, (int) z, Side.XNeg, texture, d3, d5, d7, d9, d4, d6, d8, d10);

        double swap = mcp$vertexUs[1];
        mcp$vertexUs[1] = mcp$vertexUs[2];
        mcp$vertexUs[2] = swap;

        swap = mcp$vertexVs[1];
        mcp$vertexVs[1] = mcp$vertexVs[2];
        mcp$vertexVs[2] = swap;
    }

    // East
    @Inject(method = "renderFaceXPos", require = 1, at = @At(value = "FIELD",
                                                target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                                opcode = Opcodes.GETFIELD))
    private void swizzleVerticesXPos(Block block, double x, double y, double z, IIcon texture, CallbackInfo ci,
                                   @Local(ordinal = 3) double d3, @Local(ordinal = 4) double d4, @Local(ordinal = 5) double d5,
                                   @Local(ordinal = 6) double d6, @Local(ordinal = 7) double d7, @Local(ordinal = 8) double d8,
                                   @Local(ordinal = 9) double d9, @Local(ordinal = 10) double d10) {

        if(!ModuleConfig.naturalTextures) return;
        mcp$captureVertexes(block, (int) x, (int) y, (int) z, Side.XPos, texture, d3, d5, d7, d9, d4, d6, d8, d10);

        double swap = mcp$vertexUs[1];
        mcp$vertexUs[1] = mcp$vertexUs[2];
        mcp$vertexUs[2] = swap;

        swap = mcp$vertexVs[1];
        mcp$vertexVs[1] = mcp$vertexVs[2];
        mcp$vertexVs[2] = swap;
    }

    /**  Standard block render: UV apply Mixins */
    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    require = 6, ordinal = 3, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          opcode = Opcodes.GETFIELD,
                                          shift = At.Shift.AFTER))
    private double overwriteUD3(double d3) {
        return ModuleConfig.naturalTextures ? mcp$vertexUs[0] : d3;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    require = 6, ordinal = 4, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          opcode = Opcodes.GETFIELD,
                                          shift = At.Shift.AFTER))
    private double overwriteUD4(double d4) {
        return ModuleConfig.naturalTextures ? mcp$vertexUs[1] : d4;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    require = 6, ordinal = 5, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          opcode = Opcodes.GETFIELD,
                                          shift = At.Shift.AFTER))
    private double overwriteVD5(double d5) {
        return ModuleConfig.naturalTextures ? mcp$vertexVs[0] : d5;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    require = 6, ordinal = 6, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          opcode = Opcodes.GETFIELD,
                                          shift = At.Shift.AFTER))
    private double overwriteVD6(double d6) {
        return ModuleConfig.naturalTextures ? mcp$vertexVs[1] : d6;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    require = 6, ordinal = 7, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          opcode = Opcodes.GETFIELD,
                                          shift = At.Shift.AFTER))
    private double overwriteUD7(double d7) {
        return ModuleConfig.naturalTextures ? mcp$vertexUs[2] : d7;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    require = 6, ordinal = 8, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          opcode = Opcodes.GETFIELD,
                                          shift = At.Shift.AFTER))
    private double overwriteUD8(double d8) {
        return ModuleConfig.naturalTextures ? mcp$vertexUs[3] : d8;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    require = 6, ordinal = 9, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          opcode = Opcodes.GETFIELD,
                                          shift = At.Shift.AFTER))
    private double overwriteVD9(double d9) {
        return ModuleConfig.naturalTextures ? mcp$vertexVs[2] : d9;
    }

    @ModifyVariable(method = {"renderFaceXNeg", "renderFaceXPos",
                              "renderFaceYNeg", "renderFaceYPos",
                              "renderFaceZNeg", "renderFaceZPos"},
                    require = 6, ordinal = 10, at = @At(value = "FIELD",
                                          target = "Lnet/minecraft/client/renderer/RenderBlocks;enableAO:Z",
                                          opcode = Opcodes.GETFIELD,
                                          shift = At.Shift.AFTER))
    private double overwriteVD10(double d10) {
        return ModuleConfig.naturalTextures ? mcp$vertexVs[3] : d10;
    }

    /** Vines */
    @Expression("(? & 2) != 0") // East
    @ModifyExpressionValue(method = "renderBlockVine", require = 1, at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean swizzleVerticesVineXPos(boolean original, Block block, int x, int y, int z,
                                        @Local(ordinal = 0) IIcon texture, @Local Tessellator tessellator,
                                        @Local(ordinal = 0) double minU, @Local(ordinal = 1) double minV,
                                        @Local(ordinal = 2) double maxU, @Local(ordinal = 3) double maxV,
                                        @Local(ordinal = 4) double offset) {
        if(!original || !ModuleConfig.naturalTextures) return original;

        mcp$captureVertexes(block, x, y, z, Side.XPos, texture, minU, minV, maxU, maxV, minU, maxV, maxU, minV);

        // Front face
        tessellator.addVertexWithUV(x + offset, y + 1, z + 1, mcp$vertexUs[0], mcp$vertexVs[0]);
        tessellator.addVertexWithUV(x + offset, y + 0, z + 1, mcp$vertexUs[2], mcp$vertexVs[2]);
        tessellator.addVertexWithUV(x + offset, y + 0, z + 0, mcp$vertexUs[1], mcp$vertexVs[1]);
        tessellator.addVertexWithUV(x + offset, y + 1, z + 0, mcp$vertexUs[3], mcp$vertexVs[3]);

        // Back face
        tessellator.addVertexWithUV(x + offset, y + 1, z + 0, mcp$vertexUs[3], mcp$vertexVs[3]);
        tessellator.addVertexWithUV(x + offset, y + 0, z + 0, mcp$vertexUs[1], mcp$vertexVs[1]);
        tessellator.addVertexWithUV(x + offset, y + 0, z + 1, mcp$vertexUs[2], mcp$vertexVs[2]);
        tessellator.addVertexWithUV(x + offset, y + 1, z + 1, mcp$vertexUs[0], mcp$vertexVs[0]);

        return false;
    }

    @Expression("(? & 8) != 0") // West
    @ModifyExpressionValue(method = "renderBlockVine", require = 1, at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean swizzleVerticesVineXNeg(boolean original, Block block, int x, int y, int z,
                                            @Local(ordinal = 0) IIcon texture, @Local Tessellator tessellator,
                                            @Local(ordinal = 0) double minU, @Local(ordinal = 1) double minV,
                                            @Local(ordinal = 2) double maxU, @Local(ordinal = 3) double maxV,
                                            @Local(ordinal = 4) double offset) {
        if(!original || !ModuleConfig.naturalTextures) return original;

        mcp$captureVertexes(block, x, y, z, Side.XNeg, texture, maxU, minV, minU, minV, minU, maxV, maxU, maxV);

        // Front face
        tessellator.addVertexWithUV((x + 1) - offset, y + 0, z + 1, mcp$vertexUs[3], mcp$vertexVs[3]);
        tessellator.addVertexWithUV((x + 1) - offset, y + 1, z + 1, mcp$vertexUs[0], mcp$vertexVs[0]);
        tessellator.addVertexWithUV((x + 1) - offset, y + 1, z + 0, mcp$vertexUs[1], mcp$vertexVs[1]);
        tessellator.addVertexWithUV((x + 1) - offset, y + 0, z + 0, mcp$vertexUs[2], mcp$vertexVs[2]);

        // Back face
        tessellator.addVertexWithUV((x + 1) - offset, y + 0, z + 0, mcp$vertexUs[2], mcp$vertexVs[2]);
        tessellator.addVertexWithUV((x + 1) - offset, y + 1, z + 0, mcp$vertexUs[1], mcp$vertexVs[1]);
        tessellator.addVertexWithUV((x + 1) - offset, y + 1, z + 1, mcp$vertexUs[0], mcp$vertexVs[0]);
        tessellator.addVertexWithUV((x + 1) - offset, y + 0, z + 1, mcp$vertexUs[3], mcp$vertexVs[3]);

        return false;
    }

    @Expression("(? & 4) != 0") // South
    @ModifyExpressionValue(method = "renderBlockVine", require = 1, at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean swizzleVerticesVineZPos(boolean original, Block block, int x, int y, int z,
                                            @Local(ordinal = 0) IIcon texture, @Local Tessellator tessellator,
                                            @Local(ordinal = 0) double minU, @Local(ordinal = 1) double minV,
                                            @Local(ordinal = 2) double maxU, @Local(ordinal = 3) double maxV,
                                            @Local(ordinal = 4) double offset) {
        if(!original || !ModuleConfig.naturalTextures) return original;

        mcp$captureVertexes(block, x, y, z, Side.ZPos, texture, minU, minV, maxU, minV, maxU, maxV, minU, maxV);

        // Front face
        tessellator.addVertexWithUV(x + 1, y + 0, z + offset, mcp$vertexUs[2], mcp$vertexVs[2]);
        tessellator.addVertexWithUV(x + 1, y + 1, z + offset, mcp$vertexUs[1], mcp$vertexVs[1]);
        tessellator.addVertexWithUV(x + 0, y + 1, z + offset, mcp$vertexUs[0], mcp$vertexVs[0]);
        tessellator.addVertexWithUV(x + 0, y + 0, z + offset, mcp$vertexUs[3], mcp$vertexVs[3]);

        // Back face
        tessellator.addVertexWithUV(x + 0, y + 0, z + offset, mcp$vertexUs[3], mcp$vertexVs[3]);
        tessellator.addVertexWithUV(x + 0, y + 1, z + offset, mcp$vertexUs[0], mcp$vertexVs[0]);
        tessellator.addVertexWithUV(x + 1, y + 1, z + offset, mcp$vertexUs[1], mcp$vertexVs[1]);
        tessellator.addVertexWithUV(x + 1, y + 0, z + offset, mcp$vertexUs[2], mcp$vertexVs[2]);

        return false;
    }

    @Expression("(? & 1) != 0") // North
    @ModifyExpressionValue(method = "renderBlockVine", require = 1, at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean swizzleVerticesVineZNeg(boolean original, Block block, int x, int y, int z,
                                            @Local(ordinal = 0) IIcon texture, @Local Tessellator tessellator,
                                            @Local(ordinal = 0) double minU, @Local(ordinal = 1) double minV,
                                            @Local(ordinal = 2) double maxU, @Local(ordinal = 3) double maxV,
                                            @Local(ordinal = 4) double offset) {
        if(!original || !ModuleConfig.naturalTextures) return original;

        mcp$captureVertexes(block, x, y, z, Side.ZNeg, texture, maxU, maxV, minU, maxV, minU, minV, maxU, minV);

        // Front face
        tessellator.addVertexWithUV(x + 1, y + 1, (z + 1) - offset, mcp$vertexUs[2], mcp$vertexVs[2]);
        tessellator.addVertexWithUV(x + 1, y + 0, (z + 1) - offset, mcp$vertexUs[1], mcp$vertexVs[1]);
        tessellator.addVertexWithUV(x + 0, y + 0, (z + 1) - offset, mcp$vertexUs[0], mcp$vertexVs[0]);
        tessellator.addVertexWithUV(x + 0, y + 1, (z + 1) - offset, mcp$vertexUs[3], mcp$vertexVs[3]);

        // Back face
        tessellator.addVertexWithUV(x + 0, y + 1, (z + 1) - offset, mcp$vertexUs[3], mcp$vertexVs[3]);
        tessellator.addVertexWithUV(x + 0, y + 0, (z + 1) - offset, mcp$vertexUs[0], mcp$vertexVs[0]);
        tessellator.addVertexWithUV(x + 1, y + 0, (z + 1) - offset, mcp$vertexUs[1], mcp$vertexVs[1]);
        tessellator.addVertexWithUV(x + 1, y + 1, (z + 1) - offset, mcp$vertexUs[2], mcp$vertexVs[2]);

        return false;
    }

    @Definition(id = "blockAccess", field = "Lnet/minecraft/client/renderer/RenderBlocks;blockAccess:Lnet/minecraft/world/IBlockAccess;")
    @Definition(id = "getBlock", method = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;")
    @Definition(id = "isBlockNormalCube", method = "Lnet/minecraft/block/Block;isBlockNormalCube()Z")
    @Expression("this.blockAccess.getBlock(?, ?, ?).isBlockNormalCube()") // North
    @ModifyExpressionValue(method = "renderBlockVine",
                           require = 1,
                           at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean swizzleVerticesVineYNeg(boolean original, Block block, int x, int y, int z,
                                            @Local(ordinal = 0) IIcon texture, @Local Tessellator tessellator,
                                            @Local(ordinal = 0) double minU, @Local(ordinal = 1) double minV,
                                            @Local(ordinal = 2) double maxU, @Local(ordinal = 3) double maxV,
                                            @Local(ordinal = 4) double offset) {
        if(!original || !ModuleConfig.naturalTextures) return original;

        mcp$captureVertexes(block, x, y, z, Side.YNeg, texture, maxU, maxV, minU, maxV, minU, minV, maxU, minV);

        // Front face (bottom facing vine has no backface by default)
        tessellator.addVertexWithUV(x + 1, (y + 1) - offset, z + 0, mcp$vertexUs[2], mcp$vertexVs[2]);
        tessellator.addVertexWithUV(x + 1, (y + 1) - offset, z + 1, mcp$vertexUs[1], mcp$vertexVs[1]);
        tessellator.addVertexWithUV(x + 0, (y + 1) - offset, z + 1, mcp$vertexUs[0], mcp$vertexVs[0]);
        tessellator.addVertexWithUV(x + 0, (y + 1) - offset, z + 0, mcp$vertexUs[3], mcp$vertexVs[3]);

        return false;
    }
}
