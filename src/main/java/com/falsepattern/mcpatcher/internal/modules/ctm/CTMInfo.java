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

import com.falsepattern.mcpatcher.internal.modules.common.CommonParser;
import com.falsepattern.mcpatcher.internal.modules.common.IntRange;
import com.falsepattern.mcpatcher.internal.modules.common.ResourceScanner;
import com.falsepattern.mcpatcher.internal.modules.common.WeightedRandom;
import com.falsepattern.mcpatcher.internal.modules.overlay.ResourceGenerator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.Map;
import java.util.Properties;

import static com.falsepattern.mcpatcher.internal.modules.ctm.CTMEngine.LOG;

@Accessors(fluent = true)
@Getter
public class CTMInfo {
    @NotNull
    private final String name;

    @NotNull
    private final String basePath;
    @NotNull
    private final Method method;
    private final int facesMask;
    @Nullable
    @Unmodifiable
    private final IntList metadatas;
    @Nullable
    @Unmodifiable
    private final ObjectList<BiomeGenBase> biomes;
    private final IntRange.@Nullable @Unmodifiable List heights;
    private final int renderPass;
    private final boolean innerSeams;
    private final int width;
    private final int height;
    @NotNull
    private final Symmetry symmetry;
    public boolean linked;
    public IIcon[] matchTileIcons;
    public IIcon[] tileIcons;
    @Nullable
    @Unmodifiable
    private ObjectList<@NotNull Block> matchBlocks;
    @Nullable
    @Unmodifiable
    private ObjectList<String> matchTiles;
    @Nullable
    @Unmodifiable
    private ObjectList<String> tiles;
    @NotNull
    private Connect connect;
    @Nullable
    private WeightedRandom weights;

    public CTMInfo(@NotNull Properties props, @NotNull String path) {
        this.name = CTMParser.parseName(path);
        this.basePath = CTMParser.parseBasePath(path);
        this.matchBlocks = CTMParser.parseBlockIds(props.getProperty("matchBlocks"));
        this.matchTiles = CTMParser.parseMatchTiles(props.getProperty("matchTiles"), basePath);
        this.method = CTMParser.parseMethod(props.getProperty("method"));
        this.tiles = CTMParser.parseTileNames(props.getProperty("tiles"), basePath);
        this.connect = CTMParser.parseConnect(props.getProperty("connect"));
        this.facesMask = CTMParser.parseFacesMask(props.getProperty("faces"));
        this.metadatas = CommonParser.parseInts(props.getProperty("metadata"));
        this.biomes = CommonParser.parseBiomes(props.getProperty("biomes"));

        var heights = CommonParser.parseIntRanges(props.getProperty("heights"));
        if (heights == null) {
            int minHeight = CommonParser.parseInt(props.getProperty("minHeight"), Integer.MIN_VALUE);
            int maxHeight = CommonParser.parseInt(props.getProperty("maxHeight"), Integer.MAX_VALUE);
            if (minHeight != Integer.MIN_VALUE || maxHeight != Integer.MAX_VALUE) {
                heights = new IntRange.List();
                heights.add(new IntRange(minHeight, maxHeight));
            }
        }
        this.heights = heights;

        this.renderPass = CommonParser.parseInt(props.getProperty("renderPass"));
        this.innerSeams = CommonParser.parseBoolean(props.getProperty("innerSeams"));
        this.width = CommonParser.parseInt(props.getProperty("width"));
        this.height = CommonParser.parseInt(props.getProperty("height"));
        val weights = CommonParser.parseInts(props.getProperty("weights"));
        if (weights != null) {
            this.weights = new WeightedRandom(weights);
        }
        this.symmetry = CTMParser.parseSymmetry(props.getProperty("symmetry"));
    }

    public static String toFileName(String iconName) {
        final String fullName;
        if (!iconName.contains("/")) {
            fullName = "textures/blocks/" + iconName;
        } else {
            fullName = iconName;
        }
        return fullName + ".png";
    }

    private static @Nullable IIcon getIcon(String iconName) {
        val map = TextureMapExtension.textureMapBlocks;
        if (map == null) {
            return null;
        }
        return map.getTextureExtry(iconName);
    }

    private static void warnTilesNotDefined(String path) {
        LOG.warn("Tiles not defined: {}", path);
    }

    private static void warnInvalidTilesExact(int expected, String path) {
        LOG.warn("Invalid tiles, must be exactly {}: {}", expected, path);
    }

    @Contract(value = "!null, _, _-> !null; null, _, _ -> null")
    private @Nullable IIcon[] registerIcons(@Nullable ObjectList<String> tileNames,
                                            @NotNull TextureMap textureMap,
                                            @Nullable Map<ResourceLocation, ResourceGenerator> overlay) {
        if (tileNames == null) {
            return null;
        }
        if (this.method == Method.Compact) {
            if (overlay == null) {
                throw new RuntimeException("Resource overlay is disabled, cannot load compact CTMs!");
            }
            return CompactCTMDecoder.decode(tileNames, textureMap, overlay);
        }
        val icons = new ObjectArrayList<IIcon>();
        for (val iconName : tileNames) {
            val fileName = toFileName(iconName);
            val loc = new ResourceLocation(fileName);
            val exists = ResourceScanner.hasResource(loc);
            if (!exists) {
                LOG.warn("File not found: {}", fileName);
            }

            val icon = textureMap.registerIcon(iconName);
            icons.add(icon);
        }
        return icons.toArray(new IIcon[0]);
    }

    public boolean isValid(String path) {
        if (name.isEmpty()) {
            LOG.warn("No name found: {}", path);
            return false;
        }
        if (matchBlocks == null) {
            matchBlocks = detectMatchBlocks();
        }
        if (matchTiles == null && matchBlocks == null) {
            matchTiles = detectMatchTiles();
        }
        if (matchBlocks == null && matchTiles == null) {
            LOG.warn("No matchBlocks or matchTiles specified: {}", path);
            return false;
        }
        if (method == Method.None) {
            LOG.warn("No method: {}", path);
            return false;
        }
        if (tiles == null || tiles.isEmpty()) {
            LOG.warn("No tiles specified: {}", path);
            return false;
        }
        if (connect == Connect.None) {
            connect = detectConnect();
        }
        if (connect == Connect.Unknown) {
            LOG.warn("Invalid connect in: {}", path);
            return false;
        }
        if (renderPass > 0) {
            LOG.warn("Render pass not supported: {}", renderPass);
            return false;
        }
        if (Side.matches(Side.MASK_UNKNOWN, facesMask)) {
            LOG.warn("Invalid faces in: {}", path);
            return false;
        }
        if (symmetry == Symmetry.Unknown) {
            LOG.warn("Invalid symmetry in: {}", path);
            return false;
        }
        switch (method) {
            case Ctm:
                return isValidCtm(path);
            case Compact:
                return isValidCompact(path);
            case Horizontal:
                return isValidHorizontal(path);
            case Top:
                return isValidTop(path);
            case Random:
                return isValidRandom(path);
            case Repeat:
                return isValidRepeat(path);
            case Vertical:
                return isValidVertical(path);
            case Fixed:
                return isValidFixed(path);
            case HorizontalVertical:
                return isValidHorizontalVertical(path);
            case VerticalHorizontal:
                return isValidVerticalHorizontal(path);
            default:
                LOG.warn("Unknown method: {}", path);
                return false;
        }
    }

    public void updateIcons(TextureMap textureMap, @Nullable Map<ResourceLocation, ResourceGenerator> overlay) {
        matchTileIcons = registerIcons(matchTiles, textureMap, overlay);
        tileIcons = registerIcons(tiles, textureMap, overlay);
    }

    private Connect detectConnect() {
        if (matchBlocks != null) {
            return Connect.Block;
        }
        if (matchTiles != null) {
            return Connect.Tile;
        }
        return Connect.Unknown;
    }

    private @Nullable ObjectList<Block> detectMatchBlocks() {
        if (!name.startsWith("block")) {
            return null;
        }
        int startPos = "block".length();

        int pos;
        for (pos = startPos; pos < name.length(); ++pos) {
            char ch = name.charAt(pos);
            if (ch < '0' || ch > '9') {
                break;
            }
        }

        if (pos == startPos) {
            return null;
        }
        String idStr = name.substring(startPos, pos);
        int id = CommonParser.parseInt(idStr, -1);
        if (id < 0) {
            return null;
        }
        Block block = Block.getBlockById(id);
        if (block == null) {
            return null;
        }
        return ObjectLists.singleton(block);
    }

    private ObjectList<String> detectMatchTiles() {
        IIcon icon = getIcon(name);
        return icon == null ? null : ObjectLists.singleton(name);
    }

    private boolean isValidCtm(String path) {
        if (this.tiles == null) {
            this.tiles = CTMParser.parseTileNames("0-11 16-27 32-43 48-58", basePath);
        }

        if (this.tiles.size() < 47) {
            LOG.warn("Invalid tiles, must be at least 47: {}", path);
            return false;
        }
        return true;
    }

    private boolean isValidCompact(String path) {
        if (this.tiles == null) {
            this.tiles = CTMParser.parseTileNames("0-4", basePath);
        }

        if (this.tiles.size() < 5) {
            LOG.warn("Invalid tiles, must be at least 5: {}", path);
            return false;
        }
        return true;
    }

    private boolean isValidHorizontal(String path) {
        if (this.tiles == null) {
            this.tiles = CTMParser.parseTileNames("12-15", basePath);
        }

        if (this.tiles.size() != 4) {
            warnInvalidTilesExact(4, path);
            return false;
        }
        return true;
    }

    private boolean isValidVertical(String path) {
        if (this.tiles == null) {
            LOG.warn("No tiles defined for vertical: {}", path);
            return false;
        }
        if (this.tiles.size() != 4) {
            warnInvalidTilesExact(4, path);
            return false;
        }
        return true;
    }

    private boolean isValidHorizontalVertical(String path) {
        if (this.tiles == null) {
            LOG.warn("No tiles defined for horizontal+vertical: {}", path);
            return false;
        }
        if (this.tiles.size() != 7) {
            warnInvalidTilesExact(7, path);
            return false;
        }
        return true;
    }

    private boolean isValidVerticalHorizontal(String path) {
        if (this.tiles == null) {
            LOG.warn("No tiles defined for vertical+horizontal: {}", path);
            return false;
        }
        if (this.tiles.size() != 7) {
            warnInvalidTilesExact(7, path);
            return false;
        }
        return true;
    }

    private boolean isValidRandom(String path) {
        if (this.tiles == null || this.tiles.isEmpty()) {
            warnTilesNotDefined(path);
            return false;
        }
        if (this.weights == null) {
            return true;
        }
        if (this.weights.size() > this.tiles.size()) {
            LOG.warn("More weights defined than tiles, trimming weights: {}", path);
            this.weights = this.weights.resize(this.tiles.size());
        }

        if (this.weights.size() < this.tiles.size()) {
            LOG.warn("Less weights defined than tiles, expanding weights: {}", path);
            this.weights = this.weights.resize(this.tiles.size());
        }

        return true;
    }

    private boolean isValidRepeat(String path) {
        if (this.tiles == null) {
            warnTilesNotDefined(path);
            return false;
        }
        if (this.width <= 0 || this.width > 16) {
            LOG.warn("Invalid width: {}", path);
            return false;
        }
        if (this.height <= 0 || this.height > 16) {
            LOG.warn("Invalid height: {}", path);
            return false;
        }
        if (this.tiles.size() != this.width * this.height) {
            LOG.warn("Number of tiles does not equal width x height: {}", path);
            return false;
        }
        return true;
    }

    private boolean isValidFixed(String path) {
        if (this.tiles == null) {
            warnTilesNotDefined(path);
            return false;
        }
        if (this.tiles.size() != 1) {
            LOG.warn("Number of tiles should be 1 for method: fixed.");
            return false;
        }
        return true;
    }

    private boolean isValidTop(String path) {
        if (this.tiles == null) {
            this.tiles = CTMParser.parseTileNames("66", basePath);
        }

        if (this.tiles.size() != 1) {
            LOG.warn("Invalid tiles, must be exactly 1: {}", path);
            return false;
        }
        return true;
    }
}
