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

import com.falsepattern.mcpatcher.Tags;
import com.falsepattern.mcpatcher.internal.modules.common.MCPMath;
import com.falsepattern.mcpatcher.internal.modules.common.ResourceScanner;
import com.falsepattern.mcpatcher.internal.modules.overlay.ResourceGenerator;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.val;
import lombok.var;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;

public class CTMEngine {
    static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + " CTM");

    private static final Object2ObjectMap<Block, ObjectList<CTMInfo>> blockProperties = new Object2ObjectOpenCustomHashMap<>(
            128,
            0.1f,
            new Hash.Strategy<Block>() {
                @Override
                public int hashCode(Block o) {
                    return System.identityHashCode(o);
                }

                @Override
                public boolean equals(Block a, Block b) {
                    return a == b;
                }
            });
    private static final ObjectList<ObjectList<CTMInfo>> tileProperties = new ObjectArrayList<>();
    private static boolean multipass = false;

    public static IIcon getCTMIconMultiPass(IBlockAccess blockAccess,
                                            Block block,
                                            int x,
                                            int y,
                                            int z,
                                            @Nullable Side side,
                                            IIcon icon) {
        if (blockAccess == null) {
            return icon;
        }
        IIcon newIcon = getCTMIconSinglePass(blockAccess, block, x, y, z, side, icon, true);
        if (!multipass) {
            return newIcon;
        }
        if (newIcon == icon) {
            return newIcon;
        }
        IIcon mpIcon = newIcon;

        for (int i = 0; i < 3; ++i) {
            IIcon newMpIcon = getCTMIconSinglePass(blockAccess, block, x, y, z, side, mpIcon, false);
            if (newMpIcon == mpIcon) {
                break;
            }

            mpIcon = newMpIcon;
        }

        return mpIcon;
    }

    public static IIcon getCTMIconSinglePass(IBlockAccess blockAccess,
                                             Block block,
                                             int x,
                                             int y,
                                             int z,
                                             @Nullable Side side,
                                             IIcon icon,
                                             boolean checkBlocks) {
        if (!(icon instanceof TextureAtlasSprite)) {
            return icon;
        }
        TextureAtlasSprite ts = (TextureAtlasSprite) icon;
        int iconId = ((ICTMSprite) ts).mcp$indexInMap();
        int metadata = -1;
        if (Tessellator.instance.defaultTexture && iconId >= 0 && iconId < tileProperties.size()) {
            val infos = tileProperties.get(iconId);
            if (infos != null) {
                metadata = blockAccess.getBlockMetadata(x, y, z);

                IIcon newIcon = getCTMIcon(infos, blockAccess, block, x, y, z, side, ts, metadata);
                if (newIcon != null) {
                    return newIcon;
                }
            }
        }

        if (checkBlocks) {
            val infos = blockProperties.get(block);
            if (infos != null) {
                if (metadata < 0) {
                    metadata = blockAccess.getBlockMetadata(x, y, z);
                }

                IIcon newIcon = getCTMIcon(infos, blockAccess, block, x, y, z, side, ts, metadata);
                if (newIcon != null) {
                    return newIcon;
                }
            }
        }

        return icon;
    }

    private static @Nullable IIcon getCTMIcon(ObjectList<CTMInfo> infos,
                                              IBlockAccess blockAccess,
                                              Block block,
                                              int x,
                                              int y,
                                              int z,
                                              @Nullable Side side,
                                              IIcon icon,
                                              int metadata) {
        for (val info : infos) {
            if (info == null) {
                continue;
            }
            IIcon newIcon = getCTMIcon(info, blockAccess, block, x, y, z, side, icon, metadata);
            if (newIcon != null) {
                return newIcon;
            }
        }

        return null;
    }

    private static IIcon getCTMIcon(CTMInfo info,
                                    IBlockAccess blockAccess,
                                    Block block,
                                    int x,
                                    int y,
                                    int z,
                                    @Nullable Side side,
                                    IIcon icon,
                                    int metadata) {
        if (info.heights() != null &&
            !info.heights()
                 .isInRange(y)) {
            return null;
        } else {
            if (info.biomes() != null) {
                BiomeGenBase blockBiome = blockAccess.getBiomeGenForCoords(x, z);
                boolean biomeOk = false;

                for (val biome : info.biomes()) {
                    if (blockBiome == biome) {
                        biomeOk = true;
                        break;
                    }
                }

                if (!biomeOk) {
                    return null;
                }
            }

            Axis vertAxis = Axis.Y;
            int metadataCheck = metadata;
            if (block instanceof BlockRotatedPillar) {
                vertAxis = getWoodAxis(side, metadata);
                metadataCheck = metadata & 3;
            }

            if (block instanceof BlockQuartz) {
                vertAxis = getQuartzAxis(side, metadata);
                if (metadataCheck > 2) {
                    metadataCheck = 2;
                }
            }

            if (side != null && !Side.matches(Side.MASK_ALL, info.facesMask())) {
                Side sideCheck = side;
                if (vertAxis != Axis.Y) {
                    sideCheck = fixSideByAxis(side, vertAxis);
                }

                if (!sideCheck.matches(info.facesMask())) {
                    return null;
                }
            }

            if (info.metadatas() != null) {
                IntList mds = info.metadatas();
                boolean metadataFound = false;

                int len = mds.size();
                for (int i = 0; i < len; ++i) {
                    if (mds.getInt(i) == metadataCheck) {
                        metadataFound = true;
                        break;
                    }
                }

                if (!metadataFound) {
                    return null;
                }
            }

            switch (info.method()) {
                case Ctm:
                case Compact:
                    return getConnectedTextureCtm(info, blockAccess, block, x, y, z, side, icon, metadata);
                case Horizontal:
                    return getConnectedTextureHorizontal(info,
                                                         blockAccess,
                                                         block,
                                                         x,
                                                         y,
                                                         z,
                                                         vertAxis,
                                                         side,
                                                         icon,
                                                         metadata);
                case Top:
                    return getConnectedTextureTop(info, blockAccess, block, x, y, z, vertAxis, side, icon, metadata);
                case Random:
                    return getConnectedTextureRandom(info, blockAccess, block, x, y, z, side);
                case Repeat:
                    return getConnectedTextureRepeat(info, x, y, z, side);
                case Vertical:
                    return getConnectedTextureVertical(info,
                                                       blockAccess,
                                                       block,
                                                       x,
                                                       y,
                                                       z,
                                                       vertAxis,
                                                       side,
                                                       icon,
                                                       metadata);
                case Fixed:
                    return getConnectedTextureFixed(info);
                case HorizontalVertical:
                    return getConnectedTextureHorizontalVertical(info,
                                                                 blockAccess,
                                                                 block,
                                                                 x,
                                                                 y,
                                                                 z,
                                                                 vertAxis,
                                                                 side,
                                                                 icon,
                                                                 metadata);
                case VerticalHorizontal:
                    return getConnectedTextureVerticalHorizontal(info,
                                                                 blockAccess,
                                                                 block,
                                                                 x,
                                                                 y,
                                                                 z,
                                                                 vertAxis,
                                                                 side,
                                                                 icon,
                                                                 metadata);
                default:
                    return null;
            }
        }
    }

    private static Side fixSideByAxis(Side side, Axis vertAxis) {
        switch (vertAxis) {
            case Z:
                switch (side) {
                    case YNeg:
                        return Side.ZNeg;
                    case YPos:
                        return Side.ZPos;
                    case ZNeg:
                        return Side.YPos;
                    case ZPos:
                        return Side.YNeg;
                    default:
                        return side;
                }
            case X:
                switch (side) {
                    case YNeg:
                        return Side.XNeg;
                    case YPos:
                        return Side.XPos;
                    case XNeg:
                        return Side.YPos;
                    case XPos:
                        return Side.YNeg;
                    default:
                        return side;
                }
            default:
                return side;
        }
    }

    private static Axis getWoodAxis(Side side, int metadata) {
        int orient = (metadata & 12) >> 2;
        switch (orient) {
            case 1:
                return Axis.X;
            case 2:
                return Axis.Z;
            default:
                return Axis.Y;
        }
    }

    private static Axis getQuartzAxis(Side side, int metadata) {
        switch (metadata) {
            case 3:
                return Axis.X;
            case 4:
                return Axis.Z;
            default:
                return Axis.Y;
        }
    }

    private static IIcon getConnectedTextureRandom(CTMInfo info,
                                                   IBlockAccess blockAccess,
                                                   Block block,
                                                   int x,
                                                   int y,
                                                   int z,
                                                   @Nullable Side side) {
        if (info.tileIcons.length == 1) {
            return info.tileIcons[0];
        }
        val face = info.symmetry()
                       .apply(side);
        if (info.linked) {
            int yDown = y - 1;

            for (Block blockDown = blockAccess.getBlock(x, yDown, z); blockDown == block;
                 blockDown = blockAccess.getBlock(x, yDown, z)) {
                y = yDown--;
                if (yDown < 0) {
                    break;
                }
            }
        }

        int rand = getRandom(x, y, z, face) & Integer.MAX_VALUE;
        int index = 0;
        if (info.weights() == null) {
            index = rand % info.tileIcons.length;
        } else {
            index = info.weights()
                        .getIndex(rand) % info.tileIcons.length;
        }

        return info.tileIcons[index];
    }

    private static int getRandom(int x, int y, int z, @NotNull Side face) {
        int rand = MCPMath.intHash(face.ordinal() + 37);
        rand = MCPMath.intHash(rand + x);
        rand = MCPMath.intHash(rand + z);
        return MCPMath.intHash(rand + y);
    }

    private static IIcon getConnectedTextureFixed(CTMInfo info) {
        return info.tileIcons[0];
    }

    private static IIcon getConnectedTextureRepeat(CTMInfo info, int x, int y, int z, @Nullable Side side) {
        if (info.tileIcons.length == 1) {
            return info.tileIcons[0];
        } else {
            int nx = 0;
            int ny = 0;
            if (side != null) {
                switch (side) {
                    case YNeg:
                    case YPos:
                        nx = x;
                        ny = z;
                        break;
                    case ZNeg:
                        nx = -x - 1;
                        ny = -y;
                        break;
                    case ZPos:
                        nx = x;
                        ny = -y;
                        break;
                    case XNeg:
                        nx = z;
                        ny = -y;
                        break;
                    case XPos:
                        nx = -z - 1;
                        ny = -y;
                }
            }

            nx %= info.width();
            ny %= info.height();
            if (nx < 0) {
                nx += info.width();
            }

            if (ny < 0) {
                ny += info.height();
            }

            int index = ny * info.width() + nx;
            return info.tileIcons[index];
        }
    }

    private static IIcon getConnectedTextureCtm(CTMInfo info,
                                                IBlockAccess blockAccess,
                                                Block block,
                                                int x,
                                                int y,
                                                int z,
                                                @Nullable Side side,
                                                IIcon icon,
                                                int metadata) {
        boolean[] borders = new boolean[6];
        if (side != null) {
            switch (side) {
                case YNeg:
                case YPos:
                    borders[0] = isNeighbour(info, blockAccess, block, x - 1, y, z, side, icon, metadata);
                    borders[1] = isNeighbour(info, blockAccess, block, x + 1, y, z, side, icon, metadata);
                    borders[2] = isNeighbour(info, blockAccess, block, x, y, z + 1, side, icon, metadata);
                    borders[3] = isNeighbour(info, blockAccess, block, x, y, z - 1, side, icon, metadata);
                    break;
                case ZNeg:
                    borders[0] = isNeighbour(info, blockAccess, block, x + 1, y, z, side, icon, metadata);
                    borders[1] = isNeighbour(info, blockAccess, block, x - 1, y, z, side, icon, metadata);
                    borders[2] = isNeighbour(info, blockAccess, block, x, y - 1, z, side, icon, metadata);
                    borders[3] = isNeighbour(info, blockAccess, block, x, y + 1, z, side, icon, metadata);
                    break;
                case ZPos:
                    borders[0] = isNeighbour(info, blockAccess, block, x - 1, y, z, side, icon, metadata);
                    borders[1] = isNeighbour(info, blockAccess, block, x + 1, y, z, side, icon, metadata);
                    borders[2] = isNeighbour(info, blockAccess, block, x, y - 1, z, side, icon, metadata);
                    borders[3] = isNeighbour(info, blockAccess, block, x, y + 1, z, side, icon, metadata);
                    break;
                case XNeg:
                    borders[0] = isNeighbour(info, blockAccess, block, x, y, z - 1, side, icon, metadata);
                    borders[1] = isNeighbour(info, blockAccess, block, x, y, z + 1, side, icon, metadata);
                    borders[2] = isNeighbour(info, blockAccess, block, x, y - 1, z, side, icon, metadata);
                    borders[3] = isNeighbour(info, blockAccess, block, x, y + 1, z, side, icon, metadata);
                    break;
                case XPos:
                    borders[0] = isNeighbour(info, blockAccess, block, x, y, z + 1, side, icon, metadata);
                    borders[1] = isNeighbour(info, blockAccess, block, x, y, z - 1, side, icon, metadata);
                    borders[2] = isNeighbour(info, blockAccess, block, x, y - 1, z, side, icon, metadata);
                    borders[3] = isNeighbour(info, blockAccess, block, x, y + 1, z, side, icon, metadata);
            }
        }

        int index = 0;
        if (borders[0] & !borders[1] & !borders[2] & !borders[3]) {
            index = 3;
        } else if (!borders[0] & borders[1] & !borders[2] & !borders[3]) {
            index = 1;
        } else if (!borders[0] & !borders[1] & borders[2] & !borders[3]) {
            index = 12;
        } else if (!borders[0] & !borders[1] & !borders[2] & borders[3]) {
            index = 36;
        } else if (borders[0] & borders[1] & !borders[2] & !borders[3]) {
            index = 2;
        } else if (!borders[0] & !borders[1] & borders[2] & borders[3]) {
            index = 24;
        } else if (borders[0] & !borders[1] & borders[2] & !borders[3]) {
            index = 15;
        } else if (borders[0] & !borders[1] & !borders[2] & borders[3]) {
            index = 39;
        } else if (!borders[0] & borders[1] & borders[2] & !borders[3]) {
            index = 13;
        } else if (!borders[0] & borders[1] & !borders[2] & borders[3]) {
            index = 37;
        } else if (!borders[0] & borders[1] & borders[2] & borders[3]) {
            index = 25;
        } else if (borders[0] & !borders[1] & borders[2] & borders[3]) {
            index = 27;
        } else if (borders[0] & borders[1] & !borders[2] & borders[3]) {
            index = 38;
        } else if (borders[0] & borders[1] & borders[2] & !borders[3]) {
            index = 14;
        } else if (borders[0] & borders[1] & borders[2] & borders[3]) {
            index = 26;
        }

        if (index == 0) {
            return info.tileIcons[index];
        }
        boolean[] edges = new boolean[6];
        if (side != null) {
            switch (side) {
                case YNeg:
                case YPos:
                    edges[0] = !isNeighbour(info, blockAccess, block, x + 1, y, z + 1, side, icon, metadata);
                    edges[1] = !isNeighbour(info, blockAccess, block, x - 1, y, z + 1, side, icon, metadata);
                    edges[2] = !isNeighbour(info, blockAccess, block, x + 1, y, z - 1, side, icon, metadata);
                    edges[3] = !isNeighbour(info, blockAccess, block, x - 1, y, z - 1, side, icon, metadata);
                    break;
                case ZNeg:
                    edges[0] = !isNeighbour(info, blockAccess, block, x - 1, y - 1, z, side, icon, metadata);
                    edges[1] = !isNeighbour(info, blockAccess, block, x + 1, y - 1, z, side, icon, metadata);
                    edges[2] = !isNeighbour(info, blockAccess, block, x - 1, y + 1, z, side, icon, metadata);
                    edges[3] = !isNeighbour(info, blockAccess, block, x + 1, y + 1, z, side, icon, metadata);
                    break;
                case ZPos:
                    edges[0] = !isNeighbour(info, blockAccess, block, x + 1, y - 1, z, side, icon, metadata);
                    edges[1] = !isNeighbour(info, blockAccess, block, x - 1, y - 1, z, side, icon, metadata);
                    edges[2] = !isNeighbour(info, blockAccess, block, x + 1, y + 1, z, side, icon, metadata);
                    edges[3] = !isNeighbour(info, blockAccess, block, x - 1, y + 1, z, side, icon, metadata);
                    break;
                case XNeg:
                    edges[0] = !isNeighbour(info, blockAccess, block, x, y - 1, z + 1, side, icon, metadata);
                    edges[1] = !isNeighbour(info, blockAccess, block, x, y - 1, z - 1, side, icon, metadata);
                    edges[2] = !isNeighbour(info, blockAccess, block, x, y + 1, z + 1, side, icon, metadata);
                    edges[3] = !isNeighbour(info, blockAccess, block, x, y + 1, z - 1, side, icon, metadata);
                    break;
                case XPos:
                    edges[0] = !isNeighbour(info, blockAccess, block, x, y - 1, z - 1, side, icon, metadata);
                    edges[1] = !isNeighbour(info, blockAccess, block, x, y - 1, z + 1, side, icon, metadata);
                    edges[2] = !isNeighbour(info, blockAccess, block, x, y + 1, z - 1, side, icon, metadata);
                    edges[3] = !isNeighbour(info, blockAccess, block, x, y + 1, z + 1, side, icon, metadata);
            }
        }

        if (index == 13 && edges[0]) {
            index = 4;
        } else if (index == 15 && edges[1]) {
            index = 5;
        } else if (index == 37 && edges[2]) {
            index = 16;
        } else if (index == 39 && edges[3]) {
            index = 17;
        } else if (index == 14 && edges[0] && edges[1]) {
            index = 7;
        } else if (index == 25 && edges[0] && edges[2]) {
            index = 6;
        } else if (index == 27 && edges[3] && edges[1]) {
            index = 19;
        } else if (index == 38 && edges[3] && edges[2]) {
            index = 18;
        } else if (index == 14 && !edges[0] && edges[1]) {
            index = 31;
        } else if (index == 25 && edges[0] && !edges[2]) {
            index = 30;
        } else if (index == 27 && !edges[3] && edges[1]) {
            index = 41;
        } else if (index == 38 && edges[3] && !edges[2]) {
            index = 40;
        } else if (index == 14 && edges[0] && !edges[1]) {
            index = 29;
        } else if (index == 25 && !edges[0] && edges[2]) {
            index = 28;
        } else if (index == 27 && edges[3] && !edges[1]) {
            index = 43;
        } else if (index == 38 && !edges[3] && edges[2]) {
            index = 42;
        } else if (index == 26 && edges[0] && edges[1] && edges[2] && edges[3]) {
            index = 46;
        } else if (index == 26 && !edges[0] && edges[1] && edges[2] && edges[3]) {
            index = 9;
        } else if (index == 26 && edges[0] && !edges[1] && edges[2] && edges[3]) {
            index = 21;
        } else if (index == 26 && edges[0] && edges[1] && !edges[2] && edges[3]) {
            index = 8;
        } else if (index == 26 && edges[0] && edges[1] && edges[2] && !edges[3]) {
            index = 20;
        } else if (index == 26 && edges[0] && edges[1] && !edges[2] && !edges[3]) {
            index = 11;
        } else if (index == 26 && !edges[0] && !edges[1] && edges[2] && edges[3]) {
            index = 22;
        } else if (index == 26 && !edges[0] && edges[1] && !edges[2] && edges[3]) {
            index = 23;
        } else if (index == 26 && edges[0] && !edges[1] && edges[2] && !edges[3]) {
            index = 10;
        } else if (index == 26 && edges[0] && !edges[1] && !edges[2] && edges[3]) {
            index = 34;
        } else if (index == 26 && !edges[0] && edges[1] && edges[2] && !edges[3]) {
            index = 35;
        } else if (index == 26 && edges[0] && !edges[1] && !edges[2] && !edges[3]) {
            index = 32;
        } else if (index == 26 && !edges[0] && edges[1] && !edges[2] && !edges[3]) {
            index = 33;
        } else if (index == 26 && !edges[0] && !edges[1] && edges[2] && !edges[3]) {
            index = 44;
        } else if (index == 26 && !edges[0] && !edges[1] && !edges[2] && edges[3]) {
            index = 45;
        }

        return info.tileIcons[index];
    }

    private static boolean isNeighbour(CTMInfo info,
                                       IBlockAccess iblockaccess,
                                       Block block,
                                       int x,
                                       int y,
                                       int z,
                                       @Nullable Side side,
                                       IIcon icon,
                                       int metadata) {
        Block neighbourBlock = iblockaccess.getBlock(x, y, z);
        if (info.connect() == Connect.Tile) {
            if (neighbourBlock == null) {
                return false;
            } else {
                int neighbourMetadata = iblockaccess.getBlockMetadata(x, y, z);
                IIcon neighbourIcon;
                if (side != null) {
                    neighbourIcon = neighbourBlock.getIcon(side.ordinal(), neighbourMetadata);
                } else {
                    neighbourIcon = neighbourBlock.getIcon(Side.YPos.ordinal(), neighbourMetadata);
                }

                return neighbourIcon == icon;
            }
        } else if (info.connect() == Connect.Material) {
            if (neighbourBlock == null) {
                return false;
            } else {
                return neighbourBlock.getMaterial() == block.getMaterial();
            }
        } else {
            return neighbourBlock == block && iblockaccess.getBlockMetadata(x, y, z) == metadata;
        }
    }

    private static IIcon getConnectedTextureHorizontal(CTMInfo info,
                                                       IBlockAccess blockAccess,
                                                       Block block,
                                                       int x,
                                                       int y,
                                                       int z,
                                                       Axis vertAxis,
                                                       @Nullable Side side,
                                                       IIcon icon,
                                                       int metadata) {
        boolean left;
        boolean right;
        left = false;
        right = false;
        if (side != null) {
            switch (vertAxis) {
                case Y:
                    switch (side) {
                        case YNeg:
                        case YPos:
                            return null;
                        case ZNeg:
                            left = isNeighbour(info, blockAccess, block, x + 1, y, z, side, icon, metadata);
                            right = isNeighbour(info, blockAccess, block, x - 1, y, z, side, icon, metadata);
                            break;
                        case ZPos:
                            left = isNeighbour(info, blockAccess, block, x - 1, y, z, side, icon, metadata);
                            right = isNeighbour(info, blockAccess, block, x + 1, y, z, side, icon, metadata);
                            break;
                        case XNeg:
                            left = isNeighbour(info, blockAccess, block, x, y, z - 1, side, icon, metadata);
                            right = isNeighbour(info, blockAccess, block, x, y, z + 1, side, icon, metadata);
                            break;
                        case XPos:
                            left = isNeighbour(info, blockAccess, block, x, y, z + 1, side, icon, metadata);
                            right = isNeighbour(info, blockAccess, block, x, y, z - 1, side, icon, metadata);
                    }
                    break;
                case Z:
                    switch (side) {
                        case YNeg:
                            left = isNeighbour(info, blockAccess, block, x + 1, y, z, side, icon, metadata);
                            right = isNeighbour(info, blockAccess, block, x - 1, y, z, side, icon, metadata);
                            break;
                        case YPos:
                            left = isNeighbour(info, blockAccess, block, x + 1, y, z, side, icon, metadata);
                            right = isNeighbour(info, blockAccess, block, x - 1, y, z, side, icon, metadata);
                            break;
                        case ZNeg:
                        case ZPos:
                            return null;
                        case XNeg:
                            left = isNeighbour(info, blockAccess, block, x, y + 1, z, side, icon, metadata);
                            right = isNeighbour(info, blockAccess, block, x, y - 1, z, side, icon, metadata);
                            break;
                        case XPos:
                            left = isNeighbour(info, blockAccess, block, x, y + 1, z, side, icon, metadata);
                            right = isNeighbour(info, blockAccess, block, x, y - 1, z, side, icon, metadata);
                    }
                    break;
                case X:
                    switch (side) {
                        case YNeg:
                            left = isNeighbour(info, blockAccess, block, x, y, z - 1, side, icon, metadata);
                            right = isNeighbour(info, blockAccess, block, x, y, z + 1, side, icon, metadata);
                            break;
                        case YPos:
                            left = isNeighbour(info, blockAccess, block, x, y, z - 1, side, icon, metadata);
                            right = isNeighbour(info, blockAccess, block, x, y, z + 1, side, icon, metadata);
                            break;
                        case ZNeg:
                            left = isNeighbour(info, blockAccess, block, x, y + 1, z, side, icon, metadata);
                            right = isNeighbour(info, blockAccess, block, x, y - 1, z, side, icon, metadata);
                            break;
                        case ZPos:
                            left = isNeighbour(info, blockAccess, block, x, y + 1, z, side, icon, metadata);
                            right = isNeighbour(info, blockAccess, block, x, y - 1, z, side, icon, metadata);
                            break;
                        case XNeg:
                        case XPos:
                            return null;
                    }
                    break;
            }
        }

        int index = 3;
        if (left) {
            if (right) {
                index = 1;
            } else {
                index = 2;
            }
        } else if (right) {
            index = 0;
        } else {
            index = 3;
        }

        return info.tileIcons[index];
    }

    private static IIcon getConnectedTextureVertical(CTMInfo info,
                                                     IBlockAccess blockAccess,
                                                     Block block,
                                                     int x,
                                                     int y,
                                                     int z,
                                                     Axis vertAxis,
                                                     @Nullable Side side,
                                                     IIcon icon,
                                                     int metadata) {
        boolean bottom = false;
        boolean top = false;
        if (side != null) {
            if (side.axis == vertAxis) {
                return null;
            }
            switch (vertAxis) {
                case Y:
                    bottom = isNeighbour(info, blockAccess, block, x, y - 1, z, side, icon, metadata);
                    top = isNeighbour(info, blockAccess, block, x, y + 1, z, side, icon, metadata);
                    break;
                case Z:
                    bottom = isNeighbour(info, blockAccess, block, x, y, z - 1, side, icon, metadata);
                    top = isNeighbour(info, blockAccess, block, x, y, z + 1, side, icon, metadata);
                    break;
                case X:
                    bottom = isNeighbour(info, blockAccess, block, x - 1, y, z, side, icon, metadata);
                    top = isNeighbour(info, blockAccess, block, x + 1, y, z, side, icon, metadata);
            }
        }

        int index;
        if (bottom) {
            if (top) {
                index = 1;
            } else {
                index = 2;
            }
        } else if (top) {
            index = 0;
        } else {
            index = 3;
        }

        return info.tileIcons[index];
    }

    private static IIcon getConnectedTextureHorizontalVertical(CTMInfo info,
                                                               IBlockAccess blockAccess,
                                                               Block block,
                                                               int x,
                                                               int y,
                                                               int z,
                                                               Axis vertAxis,
                                                               @Nullable Side side,
                                                               IIcon icon,
                                                               int metadata) {
        IIcon[] tileIcons = info.tileIcons;
        IIcon iconH = getConnectedTextureHorizontal(info, blockAccess, block, x, y, z, vertAxis, side, icon, metadata);
        if (iconH != null && iconH != icon && iconH != tileIcons[3]) {
            return iconH;
        } else {
            IIcon iconV = getConnectedTextureVertical(info,
                                                      blockAccess,
                                                      block,
                                                      x,
                                                      y,
                                                      z,
                                                      vertAxis,
                                                      side,
                                                      icon,
                                                      metadata);
            if (iconV == tileIcons[0]) {
                return tileIcons[4];
            } else if (iconV == tileIcons[1]) {
                return tileIcons[5];
            } else {
                return iconV == tileIcons[2] ? tileIcons[6] : iconV;
            }
        }
    }

    private static IIcon getConnectedTextureVerticalHorizontal(CTMInfo info,
                                                               IBlockAccess blockAccess,
                                                               Block block,
                                                               int x,
                                                               int y,
                                                               int z,
                                                               Axis vertAxis,
                                                               @Nullable Side side,
                                                               IIcon icon,
                                                               int metadata) {
        IIcon[] tileIcons = info.tileIcons;
        IIcon iconV = getConnectedTextureVertical(info, blockAccess, block, x, y, z, vertAxis, side, icon, metadata);
        if (iconV != null && iconV != icon && iconV != tileIcons[3]) {
            return iconV;
        } else {
            IIcon iconH = getConnectedTextureHorizontal(info,
                                                        blockAccess,
                                                        block,
                                                        x,
                                                        y,
                                                        z,
                                                        vertAxis,
                                                        side,
                                                        icon,
                                                        metadata);
            if (iconH == tileIcons[0]) {
                return tileIcons[4];
            } else if (iconH == tileIcons[1]) {
                return tileIcons[5];
            } else {
                return iconH == tileIcons[2] ? tileIcons[6] : iconH;
            }
        }
    }

    private static IIcon getConnectedTextureTop(CTMInfo info,
                                                IBlockAccess blockAccess,
                                                Block block,
                                                int x,
                                                int y,
                                                int z,
                                                Axis vertAxis,
                                                @Nullable Side side,
                                                IIcon icon,
                                                int metadata) {
        boolean top = false;
        if (side != null) {
            if (side.axis == vertAxis) {
                return null;
            }
            switch (vertAxis) {
                case Y:
                    top = isNeighbour(info, blockAccess, block, x, y + 1, z, side, icon, metadata);
                    break;
                case Z:
                    top = isNeighbour(info, blockAccess, block, x, y, z + 1, side, icon, metadata);
                    break;
                case X:
                    top = isNeighbour(info, blockAccess, block, x + 1, y, z, side, icon, metadata);
            }
        }

        return top ? info.tileIcons[0] : null;
    }

    public static void updateIcons(TextureMap textureMap, @Nullable Map<ResourceLocation, ResourceGenerator> overlay) {
        blockProperties.clear();
        tileProperties.clear();
        val packs = ResourceScanner.resourcePacks();

        for (val pack : packs) {
            if (pack == null) {
                continue;
            }
            updateIcons(textureMap, pack, overlay);
        }
    }

    private static void updateIcons(TextureMap textureMap,
                                    @NotNull IResourcePack pack,
                                    @Nullable Map<ResourceLocation, ResourceGenerator> overlay) {
        val names = ResourceScanner.collectFiles(pack, "mcpatcher/ctm/", ".properties", false);
        names.sort(Comparator.naturalOrder());

        for (val name : names) {
            LOG.debug("ConnectedTextures: {}", name);

            try {
                ResourceLocation locFile = new ResourceLocation(name);
                InputStream in = pack.getInputStream(locFile);
                if (in == null) {
                    LOG.warn("ConnectedTextures file not found: {}", name);
                    continue;
                }
                Properties props = new Properties();
                props.load(in);
                val def = props.getProperty("ft_default");
                if (def != null && !ResourceScanner.isFromDefaultResourcePack(new ResourceLocation(def))) {
                    continue;
                }
                CTMInfo info = new CTMInfo(props, name);
                if (!info.isValid(name)) {
                    continue;
                }
                info.updateIcons(textureMap, overlay);
                addToTileMap(info);
                addToBlockMap(info);
            } catch (FileNotFoundException e) {
                LOG.warn("ConnectedTextures file not found: {}", name);
            } catch (IOException | RuntimeException e) {
                LOG.warn(new FormattedMessage("Error while loading connected texture: {}", name), e);
            }
        }

        multipass = detectMultipass();
        LOG.debug("Multipass connected textures: {}", multipass);
    }

    private static boolean detectMultipass() {
        val props = new ObjectArrayList<CTMInfo>();

        for (val infos : tileProperties) {
            if (infos != null) {
                props.addAll(infos);
            }
        }

        for (val infos : blockProperties.values()) {
            if (infos != null) {
                props.addAll(infos);
            }
        }

        val matchIcons = new ObjectOpenHashSet<IIcon>();
        val tileIcons = new ObjectOpenHashSet<IIcon>();

        for (val info : props) {
            if (info.matchTileIcons != null) {
                matchIcons.addAll(Arrays.asList(info.matchTileIcons));
            }

            if (info.tileIcons != null) {
                tileIcons.addAll(Arrays.asList(info.tileIcons));
            }
        }

        matchIcons.retainAll(tileIcons);
        return !matchIcons.isEmpty();
    }

    private static void addToTileMap(CTMInfo info) {
        if (info.matchTileIcons != null) {
            for (val icon : info.matchTileIcons) {
                if (!(icon instanceof TextureAtlasSprite)) {
                    LOG.warn("IIcon is not TextureAtlasSprite: {}, name: {}", icon, icon.getIconName());
                } else {
                    TextureAtlasSprite ts = (TextureAtlasSprite) icon;
                    int tileId = ((ICTMSprite) ts).mcp$indexInMap();
                    if (tileId < 0) {
                        LOG.warn("Invalid tile ID: {}, icon: {}", tileId, ts.getIconName());
                    } else {
                        addToTileMap(info, tileId);
                    }
                }
            }

        }
    }

    private static void addToBlockMap(CTMInfo info) {
        val mb = info.matchBlocks();
        if (mb != null) {
            for (val block : mb) {
                if (block != null) {
                    addToBlockMap(info, block);
                }
            }

        }
    }

    private static void addToBlockMap(CTMInfo info, Block id) {
        var subList = blockProperties.get(id);
        if (subList == null) {
            subList = new ObjectArrayList<>();
            blockProperties.put(id, subList);
        }

        subList.add(info);
    }

    private static void addToTileMap(CTMInfo info, int id) {
        while (id >= tileProperties.size()) {
            tileProperties.add(null);
        }

        var subList = tileProperties.get(id);
        if (subList == null) {
            subList = new ObjectArrayList<>();
            tileProperties.set(id, subList);
        }

        subList.add(info);
    }
}