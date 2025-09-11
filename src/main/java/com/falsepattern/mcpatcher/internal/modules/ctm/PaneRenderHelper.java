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

package com.falsepattern.mcpatcher.internal.modules.ctm;

import com.falsepattern.mcpatcher.internal.Share;
import com.falsepattern.mcpatcher.internal.config.MCPatcherConfig;
import ganymedes01.etfuturum.configuration.configs.ConfigMixins;
import lombok.val;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.common.Loader;

/**
 * <img src="PaneRenderHelper.png" width=500></img>
 */
public class PaneRenderHelper {
    private static final double PANE_THICKNESS_HALF = 1.0 / 16.0;
    private static final double PANE_BELOW = 0.5 - PANE_THICKNESS_HALF;
    private static final double PANE_ABOVE = 0.5 + PANE_THICKNESS_HALF;
    private static final double PANE_BELOW_UV = 7.0;
    private static final double PANE_ABOVE_UV = 9.0;
    private static final int MASK_NONE = 0b0000;
    private static final int MASK_Z_POS = 0b0001;
    private static final int MASK_Z_NEG = 0b0010;
    private static final int MASK_Z = MASK_Z_POS | MASK_Z_NEG;
    private static final int MASK_X_POS = 0b0100;
    private static final int MASK_X_NEG = 0b1000;
    private static final int MASK_X = MASK_X_POS | MASK_X_NEG;
    private static final boolean thinPanes;

    static {
        boolean isThinPanes = false;
        try {
            if (Loader.isModLoaded("etfuturum")) {
                isThinPanes = EtFuturumCompat.thinPanes();
            }
        } catch (Throwable e) {
            Share.log.error("Failed to check Et Futurum Requiem thin panes", e);
        }
        thinPanes = isThinPanes;
    }

    private final SideIconData xNegData = new SideIconData();
    private final SideIconData xPosData = new SideIconData();
    private final SideIconData zNegData = new SideIconData();
    private final SideIconData zPosData = new SideIconData();
    private final TopIconData topData = new TopIconData();
    private boolean edgeYPos;
    private boolean edgeYNeg;
    private int yPosMask;
    private int yNegMask;
    private double minX;
    private double maxX;
    private double minZ;
    private double maxZ;
    private double belowX;
    private double aboveX;
    private double belowZ;
    private double aboveZ;
    private double yMax;
    private double yMin;
    private Tessellator tess;

    private static int getMask(IBlockAccess blockAccess, Block block, int x, int y, int z, boolean aux) {
        val connectXNeg = ((BlockPane) block).canPaneConnectToBlock(blockAccess.getBlock(x - 1, y, z));
        val connectXPos = ((BlockPane) block).canPaneConnectToBlock(blockAccess.getBlock(x + 1, y, z));
        val connectZNeg = ((BlockPane) block).canPaneConnectToBlock(blockAccess.getBlock(x, y, z - 1));
        val connectZPos = ((BlockPane) block).canPaneConnectToBlock(blockAccess.getBlock(x, y, z + 1));
        // xXzZ
        int mask = (connectXNeg ? MASK_X_NEG : 0) |
                   (connectXPos ? MASK_X_POS : 0) |
                   (connectZNeg ? MASK_Z_NEG : 0) |
                   (connectZPos ? MASK_Z_POS : 0);
        if (aux && mask == MASK_NONE && !thinPanes) {
            return MASK_X | MASK_Z;
        }
        return mask;
    }

    private void update(RenderBlocks rb, Block block, int x, int y, int z) {
        // Fetch it once for threaded tessellator overhead from falsetweaks
        tess = Tessellator.instance;
        val blockAccess = rb.blockAccess;

        val meta = blockAccess.getBlockMetadata(x, y, z);

        val edgeYPos = blockAccess.getBlock(x, y + 1, z) != block ||
                   blockAccess.getBlockMetadata(x, y + 1, z) != meta;
        if (!edgeYPos) {
            yPosMask = getMask(blockAccess, block, x, y + 1, z, true);
        }
        val edgeYNeg = blockAccess.getBlock(x, y - 1, z) != block ||
                   blockAccess.getBlockMetadata(x, y - 1, z) != meta;
        if (!edgeYNeg) {
            yNegMask = getMask(blockAccess, block, x, y - 1, z, true);
        }
        this.edgeYPos = edgeYPos;
        this.edgeYNeg = edgeYNeg;
        minX = x;
        maxX = (x + 1);
        minZ = z;
        maxZ = (z + 1);
        belowX = x + PANE_BELOW;
        aboveX = x + PANE_ABOVE;
        belowZ = z + PANE_BELOW;
        aboveZ = z + PANE_ABOVE;
        yMax = edgeYPos ? y + 0.999 : y + 1;
        yMin = edgeYNeg ? y + 0.001 : y;
        updateTextures(rb, blockAccess, block, meta, x, y, z);
    }

    private void updateTextures(RenderBlocks rb, IBlockAccess blockAccess, Block block, int meta, int x, int y, int z) {
        val isStained = block instanceof BlockStainedGlassPane;
        IIcon xNegIcon;
        IIcon xPosIcon;
        IIcon zNegIcon;
        IIcon zPosIcon;
        IIcon topIcon;
        if (rb.hasOverrideBlockTexture()) {
            assert rb.overrideBlockTexture != null;
            xNegIcon = xPosIcon = zNegIcon = zPosIcon = rb.overrideBlockTexture;
            topIcon = rb.overrideBlockTexture;
        } else {
            xNegIcon = xPosIcon = zNegIcon = zPosIcon = rb.getBlockIconFromSideAndMetadata(block, 0, meta);
            topIcon = isStained ? ((BlockStainedGlassPane) block).func_150104_b(meta)
                                : ((BlockPane) block).func_150097_e();
        }

        if (MCPatcherConfig.connectedTextures && rb.overrideBlockTexture == null) {
            xNegIcon = CTMEngine.getCTMIconMultiPass(blockAccess, block, x, y, z, Side.XNeg, xNegIcon);
            xPosIcon = CTMEngine.getCTMIconMultiPass(blockAccess, block, x, y, z, Side.XPos, xNegIcon);
            zNegIcon = CTMEngine.getCTMIconMultiPass(blockAccess, block, x, y, z, Side.ZNeg, zPosIcon);
            zPosIcon = CTMEngine.getCTMIconMultiPass(blockAccess, block, x, y, z, Side.ZPos, zPosIcon);
        }
        xNegData.update(xNegIcon, false);
        xPosData.update(xPosIcon, true);
        zNegData.update(zNegIcon, true);
        zPosData.update(zPosIcon, false);
        topData.update(topIcon);
    }

    public void renderGlassPane(RenderBlocks rb, Block block, int x, int y, int z) {
        update(rb, block, x, y, z);
        val blockAccess = rb.blockAccess;
        tess.setBrightness(block.getMixedBrightnessForBlock(blockAccess, x, y, z));
        int var7 = block.colorMultiplier(blockAccess, x, y, z);
        float red = (float) (var7 >> 16 & 0xFF) / 255.0F;
        float green = (float) (var7 >> 8 & 0xFF) / 255.0F;
        float blue = (float) (var7 & 0xFF) / 255.0F;
        if (EntityRenderer.anaglyphEnable) {
            float aR = (red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F;
            float aG = (red * 30.0F + green * 70.0F) / 100.0F;
            float aB = (red * 30.0F + blue * 70.0F) / 100.0F;
            red = aR;
            green = aG;
            blue = aB;
        }

        tess.setColorOpaque_F(red, green, blue);
        int mask = getMask(blockAccess, block, x, y, z, false);
        switch (mask) {
            case MASK_NONE:
                renderUnconnected();
                break;
            case MASK_Z_POS:
                renderConnectZPos();
                break;
            case MASK_Z_NEG:
                renderConnectZNeg();
                break;
            case MASK_Z:
                renderConnectZ();
                break;
            case MASK_X_POS:
                renderConnectXPos();
                break;
            case MASK_X_POS | MASK_Z_POS:
                renderConnectXPosZPos();
                break;
            case MASK_X_POS | MASK_Z_NEG:
                renderConnectXPosZNeg();
                break;
            case MASK_X_POS | MASK_Z:
                renderConnectXPosZ();
                break;
            case MASK_X_NEG:
                renderConnectXNeg();
                break;
            case MASK_X_NEG | MASK_Z_POS:
                renderConnectXNegZPos();
                break;
            case MASK_X_NEG | MASK_Z_NEG:
                renderConnectXNegZNeg();
                break;
            case MASK_X_NEG | MASK_Z:
                renderConnectXNegZ();
                break;
            case MASK_X:
                renderConnectX();
                break;
            case MASK_X | MASK_Z_POS:
                renderConnectXZPos();
                break;
            case MASK_X | MASK_Z_NEG:
                renderConnectXZNeg();
                break;
            case MASK_X | MASK_Z:
                renderConnectXZ();
                break;
        }
    }

    //region . shape

    private void renderUnconnected() {
        if (thinPanes) {
            // modern mc panes
            renderXNegBelowAbove();
            renderXPosBelowAbove();
            renderZNegBelowAbove();
            renderZPosBelowAbove();
            renderYCentered();
        } else {
            // legacy mc panes
            renderXNegBelowAbove(minX);
            renderXPosBelowAbove(maxX);
            renderZNegBelowAbove(minZ);
            renderZPosBelowAbove(maxZ);
            renderConnectXZ();
        }
    }

    //endregion

    //region I shapes (short and long)

    private void renderConnectZPos() {
        renderXNegBelowMax();
        renderXPosBelowMax();
        renderZNegBelowAbove();
        renderYZBelowMax();
    }

    private void renderConnectZNeg() {
        renderXNegMinAbove();
        renderXPosMinAbove();
        renderZPosBelowAbove();
        renderYZMinAbove();
    }

    private void renderConnectZ() {
        renderXNegMinMax();
        renderXPosMinMax();
        renderYZMinMax();
    }

    private void renderConnectXPos() {
        renderZNegBelowMax();
        renderZPosBelowMax();
        renderXNegBelowAbove();
        renderYXBelowMax();
    }

    private void renderConnectXNeg() {
        renderZNegMinAbove();
        renderZPosMinAbove();
        renderXPosBelowAbove();
        renderYXMinAbove();
    }

    private void renderConnectX() {
        renderZNegMinMax();
        renderZPosMinMax();
        renderYXMinMax();
    }

    //endregion

    //region L shapes

    private void renderConnectXNegZNeg() {
        renderXNegMinBelow();
        renderXPosMinAbove();
        renderZNegMinBelow();
        renderZPosMinAbove();
        renderYXMinBelow();
        renderYZMinAbove();
    }

    private void renderConnectXNegZPos() {
        renderXNegAboveMax();
        renderXPosBelowMax();
        renderZNegMinAbove();
        renderZPosMinBelow();
        renderYXMinBelow();
        renderYZBelowMax();
    }

    private void renderConnectXPosZNeg() {
        renderXNegMinAbove();
        renderXPosMinBelow();
        renderZNegAboveMax();
        renderZPosBelowMax();
        renderYXAboveMax();
        renderYZMinAbove();
    }

    private void renderConnectXPosZPos() {
        renderXNegBelowMax();
        renderXPosAboveMax();
        renderZNegBelowMax();
        renderZPosAboveMax();
        renderYXAboveMax();
        renderYZBelowMax();
    }

    //endregion

    //region T shapes

    private void renderConnectXZNeg() {
        renderXNegMinBelow();
        renderXPosMinBelow();
        renderZNegMinBelow();
        renderZNegAboveMax();
        renderZPosMinMax();
        renderYXMinMax();
        renderYZMinBelow();
    }

    private void renderConnectXZPos() {
        renderXNegAboveMax();
        renderXPosAboveMax();
        renderZNegMinMax();
        renderZPosMinBelow();
        renderZPosAboveMax();
        renderYXMinMax();
        renderYZAboveMax();
    }

    private void renderConnectXNegZ() {
        renderXNegMinBelow();
        renderXNegAboveMax();
        renderXPosMinMax();
        renderZNegMinBelow();
        renderZPosMinBelow();
        renderYXMinBelow();
        renderYZMinMax();
    }

    private void renderConnectXPosZ() {
        renderXNegMinMax();
        renderXPosMinBelow();
        renderXPosAboveMax();
        renderZNegAboveMax();
        renderZPosAboveMax();
        renderYXAboveMax();
        renderYZMinMax();
    }

    //endregion

    //region + shape

    private void renderConnectXZ() {
        renderXNegMinBelow();
        renderXNegAboveMax();
        renderXPosMinBelow();
        renderXPosAboveMax();
        renderZNegMinBelow();
        renderZNegAboveMax();
        renderZPosMinBelow();
        renderZPosAboveMax();
        renderYZMinMax();
        renderYXMinBelow();
        renderYXAboveMax();
    }

    //endregion

    //region X quads

    private void renderXNegMinBelow() {
        renderXNeg(belowX, minZ, belowZ, xNegData.uMin, xNegData.uBelow, xNegData.vMin, xNegData.vMax);
    }

    private void renderXNegMinAbove() {
        renderXNeg(belowX, minZ, aboveZ, xNegData.uMin, xNegData.uAbove, xNegData.vMin, xNegData.vMax);
    }

    private void renderXNegMinMax() {
        renderXNeg(belowX, minZ, maxZ, xNegData.uMin, xNegData.uMax, xNegData.vMin, xNegData.vMax);
    }

    private void renderXNegBelowAbove() {
        renderXNegBelowAbove(belowX);
    }

    private void renderXNegBelowAbove(double x) {
        renderXNeg(x, belowZ, aboveZ, xNegData.uBelow, xNegData.uAbove, xNegData.vMin, xNegData.vMax);
    }

    private void renderXNegBelowMax() {
        renderXNeg(belowX, belowZ, maxZ, xNegData.uBelow, xNegData.uMax, xNegData.vMin, xNegData.vMax);
    }

    private void renderXNegAboveMax() {
        renderXNeg(belowX, aboveZ, maxZ, xNegData.uAbove, xNegData.uMax, xNegData.vMin, xNegData.vMax);
    }

    private void renderXNeg(double x, double zMin, double zMax, double uMin, double uMax, double vMin, double vMax) {
        tess.addVertexWithUV(x, yMax, zMax, uMax, vMin);
        tess.addVertexWithUV(x, yMax, zMin, uMin, vMin);
        tess.addVertexWithUV(x, yMin, zMin, uMin, vMax);
        tess.addVertexWithUV(x, yMin, zMax, uMax, vMax);
    }

    private void renderXPosMinBelow() {
        renderXPos(aboveX, minZ, belowZ, xPosData.uMin, xPosData.uBelow, xPosData.vMin, xPosData.vMax);
    }

    private void renderXPosMinAbove() {
        renderXPos(aboveX, minZ, aboveZ, xPosData.uMin, xPosData.uAbove, xPosData.vMin, xPosData.vMax);
    }

    private void renderXPosMinMax() {
        renderXPos(aboveX, minZ, maxZ, xPosData.uMin, xPosData.uMax, xPosData.vMin, xPosData.vMax);
    }

    private void renderXPosBelowAbove() {
        renderXPosBelowAbove(aboveX);
    }

    private void renderXPosBelowAbove(double x) {
        renderXPos(x, belowZ, aboveZ, xPosData.uBelow, xPosData.uAbove, xPosData.vMin, xPosData.vMax);
    }

    private void renderXPosBelowMax() {
        renderXPos(aboveX, belowZ, maxZ, xPosData.uBelow, xPosData.uMax, xPosData.vMin, xPosData.vMax);
    }

    private void renderXPosAboveMax() {
        renderXPos(aboveX, aboveZ, maxZ, xPosData.uAbove, xPosData.uMax, xPosData.vMin, xPosData.vMax);
    }

    private void renderXPos(double x, double zMin, double zMax, double uMin, double uMax, double vMin, double vMax) {
        tess.addVertexWithUV(x, yMin, zMax, uMax, vMax);
        tess.addVertexWithUV(x, yMin, zMin, uMin, vMax);
        tess.addVertexWithUV(x, yMax, zMin, uMin, vMin);
        tess.addVertexWithUV(x, yMax, zMax, uMax, vMin);
    }

    //endregion

    //region Z quads

    private void renderZNegMinBelow() {
        renderZNeg(minX, belowX, belowZ, zNegData.uMin, zNegData.uBelow, zNegData.vMin, zNegData.vMax);
    }

    private void renderZNegMinAbove() {
        renderZNeg(minX, aboveX, belowZ, zNegData.uMin, zNegData.uAbove, zNegData.vMin, zNegData.vMax);
    }

    private void renderZNegMinMax() {
        renderZNeg(minX, maxX, belowZ, zNegData.uMin, zNegData.uMax, zNegData.vMin, zNegData.vMax);
    }

    private void renderZNegBelowAbove() {
        renderZNegBelowAbove(belowZ);
    }

    private void renderZNegBelowAbove(double z) {
        renderZNeg(belowX, aboveX, z, zNegData.uBelow, zNegData.uAbove, zNegData.vMin, zNegData.vMax);
    }

    private void renderZNegBelowMax() {
        renderZNeg(belowX, maxX, belowZ, zNegData.uBelow, zNegData.uMax, zNegData.vMin, zNegData.vMax);
    }

    private void renderZNegAboveMax() {
        renderZNeg(aboveX, maxX, belowZ, zNegData.uAbove, zNegData.uMax, zNegData.vMin, zNegData.vMax);
    }

    private void renderZNeg(double xMin, double xMax, double z, double uMin, double uMax, double vMin, double vMax) {
        tess.addVertexWithUV(xMin, yMax, z, uMin, vMin);
        tess.addVertexWithUV(xMax, yMax, z, uMax, vMin);
        tess.addVertexWithUV(xMax, yMin, z, uMax, vMax);
        tess.addVertexWithUV(xMin, yMin, z, uMin, vMax);
    }

    private void renderZPosMinBelow() {
        renderZPos(minX, belowX, aboveZ, zPosData.uMin, zPosData.uBelow, zPosData.vMin, zPosData.vMax);
    }

    private void renderZPosMinAbove() {
        renderZPos(minX, aboveX, aboveZ, zPosData.uMin, zPosData.uAbove, zPosData.vMin, zPosData.vMax);
    }

    private void renderZPosMinMax() {
        renderZPos(minX, maxX, aboveZ, zPosData.uMin, zPosData.uMax, zPosData.vMin, zPosData.vMax);
    }

    private void renderZPosBelowAbove() {
        renderZPosBelowAbove(aboveZ);
    }

    private void renderZPosBelowAbove(double z) {
        renderZPos(belowX, aboveX, z, zPosData.uBelow, zPosData.uAbove, zPosData.vMin, zPosData.vMax);
    }

    private void renderZPosBelowMax() {
        renderZPos(belowX, maxX, aboveZ, zPosData.uBelow, zPosData.uMax, zPosData.vMin, zPosData.vMax);
    }

    private void renderZPosAboveMax() {
        renderZPos(aboveX, maxX, aboveZ, zPosData.uAbove, zPosData.uMax, zPosData.vMin, zPosData.vMax);
    }

    private void renderZPos(double xMin, double xMax, double z, double uMin, double uMax, double vMin, double vMax) {
        tess.addVertexWithUV(xMin, yMax, z, uMin, vMin);
        tess.addVertexWithUV(xMin, yMin, z, uMin, vMax);
        tess.addVertexWithUV(xMax, yMin, z, uMax, vMax);
        tess.addVertexWithUV(xMax, yMax, z, uMax, vMin);
    }

    //endregion

    //region Y quads

    private void renderYZMinBelow() {
        if (edgeYNeg || (yNegMask & MASK_Z_NEG) == 0) {
            renderYNegZMinBelow();
        }
        if (edgeYPos || (yPosMask & MASK_Z_NEG) == 0) {
            renderYPosZMinBelow();
        }
    }

    private void renderYZMinAbove() {
        if (edgeYNeg) {
            renderYNegZMinAbove();
        } else if ((yNegMask & MASK_Z_NEG) == 0) {
            renderYNegZMinBelow();
        }
        if (edgeYPos) {
            renderYPosZMinAbove();
        } else if ((yPosMask & MASK_Z_NEG) == 0) {
            renderYPosZMinBelow();
        }
    }

    private void renderYZMinMax() {
        if (edgeYNeg) {
            renderYNegZMinMax();
        } else {
            if ((yNegMask & MASK_Z_NEG) == 0) {
                renderYNegZMinBelow();
            }
            if ((yNegMask & MASK_Z_POS) == 0) {
                renderYNegZAboveMax();
            }
        }
        if (edgeYPos) {
            renderYPosZMinMax();
        } else {
            if ((yPosMask & MASK_Z_NEG) == 0) {
                renderYPosZMinBelow();
            }
            if ((yPosMask & MASK_Z_POS) == 0) {
                renderYPosZAboveMax();
            }
        }
    }

    private void renderYZBelowMax() {
        if (edgeYNeg) {
            renderYNegZBelowMax();
        } else if ((yNegMask & MASK_Z_POS) == 0) {
            renderYNegZAboveMax();
        }
        if (edgeYPos) {
            renderYPosZBelowMax();
        } else if ((yPosMask & MASK_Z_POS) == 0) {
            renderYPosZAboveMax();
        }
    }

    private void renderYZAboveMax() {
        if (edgeYNeg || (yNegMask & MASK_Z_POS) == 0) {
            renderYNegZAboveMax();
        }
        if (edgeYPos || (yPosMask & MASK_Z_POS) == 0) {
            renderYPosZAboveMax();
        }
    }

    private void renderYNegZMinBelow() {
        renderYNegZV(minZ, belowZ, topData.vMin, topData.vBelow);
    }

    private void renderYNegZMinAbove() {
        renderYNegZV(minZ, aboveZ, topData.vMin, topData.vAbove);
    }

    private void renderYNegZMinMax() {
        renderYNegZV(minZ, maxZ, topData.vMin, topData.vMax);
    }

    private void renderYNegZBelowMax() {
        renderYNegZV(belowZ, maxZ, topData.vBelow, topData.vMax);
    }

    private void renderYNegZAboveMax() {
        renderYNegZV(aboveZ, maxZ, topData.vAbove, topData.vMax);
    }

    private void renderYNegZV(double zMin, double zMax, double vMin, double vMax) {
        tess.addVertexWithUV(belowX, yMin, zMax, topData.uBelow, vMin);
        tess.addVertexWithUV(belowX, yMin, zMin, topData.uBelow, vMax);
        tess.addVertexWithUV(aboveX, yMin, zMin, topData.uAbove, vMax);
        tess.addVertexWithUV(aboveX, yMin, zMax, topData.uAbove, vMin);
    }

    private void renderYPosZMinBelow() {
        renderYPosZV(minZ, belowZ, topData.vMin, topData.vBelow);
    }

    private void renderYPosZMinAbove() {
        renderYPosZV(minZ, aboveZ, topData.vMin, topData.vAbove);
    }

    private void renderYPosZMinMax() {
        renderYPosZV(minZ, maxZ, topData.vMin, topData.vMax);
    }

    private void renderYPosZBelowMax() {
        renderYPosZV(belowZ, maxZ, topData.vBelow, topData.vMax);
    }

    private void renderYPosZAboveMax() {
        renderYPosZV(aboveZ, maxZ, topData.vAbove, topData.vMax);
    }

    private void renderYPosZV(double zMin, double zMax, double vMin, double vMax) {
        tess.addVertexWithUV(aboveX, yMax, zMax, topData.uAbove, vMin);
        tess.addVertexWithUV(aboveX, yMax, zMin, topData.uAbove, vMax);
        tess.addVertexWithUV(belowX, yMax, zMin, topData.uBelow, vMax);
        tess.addVertexWithUV(belowX, yMax, zMax, topData.uBelow, vMin);
    }

    private void renderYXMinBelow() {
        if (edgeYNeg || (yNegMask & MASK_X_NEG) == 0) {
            renderYNegXMinBelow();
        }
        if (edgeYPos || (yPosMask & MASK_X_NEG) == 0) {
            renderYPosXMinBelow();
        }
    }

    private void renderYXMinAbove() {
        if (edgeYNeg) {
            renderYNegXMinAbove();
        } else if ((yNegMask & MASK_X_NEG) == 0) {
            renderYNegXMinBelow();
        }
        if (edgeYPos) {
            renderYPosXMinAbove();
        } else if ((yPosMask & MASK_X_NEG) == 0) {
            renderYPosXMinBelow();
        }
    }

    private void renderYXMinMax() {
        if (edgeYNeg) {
            renderYNegXMinMax();
        } else {
            if ((yNegMask & MASK_X_NEG) == 0) {
                renderYNegXMinBelow();
            }
            if ((yNegMask & MASK_X_POS) == 0) {
                renderYNegXAboveMax();
            }
        }
        if (edgeYPos) {
            renderYPosXMinMax();
        } else {
            if ((yPosMask & MASK_X_NEG) == 0) {
                renderYPosXMinBelow();
            }
            if ((yPosMask & MASK_X_POS) == 0) {
                renderYPosXAboveMax();
            }
        }
    }

    private void renderYXBelowMax() {
        if (edgeYNeg) {
            renderYNegXBelowMax();
        } else if ((yNegMask & MASK_X_POS) == 0) {
            renderYNegXAboveMax();
        }
        if (edgeYPos) {
            renderYPosXBelowMax();
        } else if ((yPosMask & MASK_X_POS) == 0) {
            renderYPosXAboveMax();
        }
    }

    private void renderYXAboveMax() {
        if (edgeYNeg || (yNegMask & MASK_X_POS) == 0) {
            renderYNegXAboveMax();
        }
        if (edgeYPos || (yPosMask & MASK_X_POS) == 0) {
            renderYPosXAboveMax();
        }
    }

    private void renderYNegXMinBelow() {
        renderYNegXV(minX, belowX, topData.vMin, topData.vBelow);
    }

    private void renderYNegXMinAbove() {
        renderYNegXV(minX, aboveX, topData.vMin, topData.vAbove);
    }

    private void renderYNegXMinMax() {
        renderYNegXV(minX, maxX, topData.vMin, topData.vMax);
    }

    private void renderYNegXBelowMax() {
        renderYNegXV(belowX, maxX, topData.vBelow, topData.vMax);
    }

    private void renderYNegXAboveMax() {
        renderYNegXV(aboveX, maxX, topData.vAbove, topData.vMax);
    }

    private void renderYNegXV(double xMin, double xMax, double vMin, double vMax) {
        tess.addVertexWithUV(xMin, yMin, aboveZ, topData.uBelow, vMin);
        tess.addVertexWithUV(xMin, yMin, belowZ, topData.uAbove, vMin);
        tess.addVertexWithUV(xMax, yMin, belowZ, topData.uAbove, vMax);
        tess.addVertexWithUV(xMax, yMin, aboveZ, topData.uBelow, vMax);
    }

    private void renderYPosXMinBelow() {
        renderYPosXV(minX, belowX, topData.vMin, topData.vBelow);
    }

    private void renderYPosXMinAbove() {
        renderYPosXV(minX, aboveX, topData.vMin, topData.vAbove);
    }

    private void renderYPosXMinMax() {
        renderYPosXV(minX, maxX, topData.vMin, topData.vMax);
    }

    private void renderYPosXBelowMax() {
        renderYPosXV(belowX, maxX, topData.vBelow, topData.vMax);
    }

    private void renderYPosXAboveMax() {
        renderYPosXV(aboveX, maxX, topData.vAbove, topData.vMax);
    }

    private void renderYPosXV(double xMin, double xMax, double vMin, double vMax) {
        tess.addVertexWithUV(xMax, yMax, aboveZ, topData.uBelow, vMax);
        tess.addVertexWithUV(xMax, yMax, belowZ, topData.uAbove, vMax);
        tess.addVertexWithUV(xMin, yMax, belowZ, topData.uAbove, vMin);
        tess.addVertexWithUV(xMin, yMax, aboveZ, topData.uBelow, vMin);
    }

    private void renderYCentered() {
        if (edgeYNeg) {
            renderYNegCentered();
        }
        if (edgeYPos) {
            renderYPosCentered();
        }
    }

    private void renderYPosCentered() {
        tess.addVertexWithUV(aboveX, yMax, aboveZ, topData.uAbove, topData.vBelow);
        tess.addVertexWithUV(aboveX, yMax, belowZ, topData.uAbove, topData.vAbove);
        tess.addVertexWithUV(belowX, yMax, belowZ, topData.uBelow, topData.vAbove);
        tess.addVertexWithUV(belowX, yMax, aboveZ, topData.uBelow, topData.vBelow);
    }

    private void renderYNegCentered() {
        tess.addVertexWithUV(belowX, yMin, aboveZ, topData.uBelow, topData.vBelow);
        tess.addVertexWithUV(belowX, yMin, belowZ, topData.uBelow, topData.vAbove);
        tess.addVertexWithUV(aboveX, yMin, belowZ, topData.uAbove, topData.vAbove);
        tess.addVertexWithUV(aboveX, yMin, aboveZ, topData.uAbove, topData.vBelow);
    }

    //endregion

    private static class SideIconData {
        public double uMin;
        public double uMax;
        public double uBelow;
        public double uAbove;
        public double vMin;
        public double vMax;
        public IIcon icon;

        public void update(IIcon icon, boolean negU) {
            this.uMin = negU ? icon.getMaxU() : icon.getMinU();
            this.uMax = negU ? icon.getMinU() : icon.getMaxU();
            this.uBelow = negU ? icon.getInterpolatedU(PANE_ABOVE_UV) : icon.getInterpolatedU(PANE_BELOW_UV);
            this.uAbove = negU ? icon.getInterpolatedU(PANE_BELOW_UV) : icon.getInterpolatedU(PANE_ABOVE_UV);
            this.vMin = icon.getMinV();
            this.vMax = icon.getMaxV();
            this.icon = icon;
        }
    }

    private static class TopIconData {
        public double uBelow;
        public double uAbove;
        public double vMin;
        public double vMax;
        public double vBelow;
        public double vAbove;
        public IIcon icon;

        public void update(IIcon icon) {
            this.uBelow = icon.getInterpolatedU(PANE_BELOW_UV);
            this.uAbove = icon.getInterpolatedU(PANE_ABOVE_UV);
            this.vMin = icon.getMinV();
            this.vMax = icon.getMaxV();
            this.vBelow = icon.getInterpolatedV(PANE_BELOW_UV);
            this.vAbove = icon.getInterpolatedV(PANE_ABOVE_UV);
            this.icon = icon;
        }
    }

    private static class EtFuturumCompat {
        public static boolean thinPanes() {
            return ConfigMixins.thinPanes;
        }
    }
}
