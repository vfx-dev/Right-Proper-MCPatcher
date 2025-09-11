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

import com.falsepattern.mcpatcher.internal.modules.overlay.ResourceGenerator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * <img src="CompactCTM.png" width=500></img>
 */
public class CompactCTMDecoder {
    // Bitmask:
    // each entry
    //
    // 000iiivh 000iiivh 00iiivh 000iiivh
    // TL       TR       BL      BR
    private static final int[] UNCOMPACTION_TABLE;


    static {
        //The LUT of all time
        // 5 textures:
        //
        // ab ef ij mn qr
        // cd gh kl op st
        //
        // order: top left, top right, bottom left, bottom right
        val lutString = "abcd ancp mnop mbod ankt mbsl irkt mnst qfst qrsh ergt efst " +
                        "abkl ankh mngh mbgl ircp qjod qrop qjsl erst qrgt qrgh qfsh " +
                        "ijkl ifkh efgh ejgl irkh mngt ifkt mnsh efgt efsh qfgt ersh " +
                        "ijcd ifcp efop ejod qfop ejsl erop qjgl ergh qfgh qrst";
        val output = new int[47];
        for (int i = 0; i < 47; i++) {
            val offset = i * 5;
            val tl = lutString.charAt(offset) - 'a';
            val tr = lutString.charAt(offset + 1) - 'a';
            val bl = lutString.charAt(offset + 2) - 'a';
            val br = lutString.charAt(offset + 3) - 'a';
            int packed = (tl << 24) | (tr << 16) | (bl << 8) | br;
            output[i] = packed;
        }
        UNCOMPACTION_TABLE = output;
    }

    public static @Nullable IIcon[] decode(@NotNull ObjectList<String> tileNames,
                                           @NotNull TextureMap textureMap,
                                           @NotNull Map<ResourceLocation, ResourceGenerator> overlay) {
        assert tileNames.size() == 5;
        ResourceLocation[] base = new ResourceLocation[5];
        for (int i = 0; i < 5; i++) {
            val iconName = tileNames.get(i);
            val fileName = CTMInfo.toFileName(iconName);
            base[i] = new ResourceLocation(fileName);
        }
        BufferedImage[] imgs = new BufferedImage[5];
        val icons = new IIcon[47];
        for (int i = 0; i < 47; i++) {
            val iconName = tileNames.get(0) + "$$GEN" + i;
            val fileName = CTMInfo.toFileName(iconName);
            val iconGen = new CompositeTextureIcon(base, imgs, i);
            overlay.put(new ResourceLocation(fileName), iconGen);
            icons[i] = textureMap.registerIcon(iconName);
        }
        return icons;
    }

    @RequiredArgsConstructor
    private static class CompositeTextureIcon implements ResourceGenerator {
        private final ResourceLocation[] base;
        private final BufferedImage[] imgs;
        private final int index;

        @Override
        public IResource gen(IResourceManager manager) throws IOException {
            int w = 0;
            int h = 0;
            for (int i = 0; i < 5; i++) {
                if (imgs[i] == null) {
                    val backing = manager.getResource(base[i]);
                    imgs[i] = ImageIO.read(backing.getInputStream());
                }
                w = Math.max(w, imgs[i].getWidth());
                h = Math.max(h, imgs[i].getHeight());
            }
            int dqW = w / 2;
            int dqH = h / 2;
            val img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            val gfx = img.createGraphics();
            val lut = UNCOMPACTION_TABLE[index];
            for (int i = 0; i < 4; i++) {
                int lutBits = (lut >>> (24 - (8 * i))) & 0b0011111;
                val srcImg = imgs[Math.min((lutBits >>> 2), 4)];
                int sImgW = srcImg.getWidth();
                int sImgH = srcImg.getHeight();
                int srcQuad = lutBits & 0b11;
                int sqW = sImgW / 2;
                int sqH = sImgH / 2;
                int sX = sqW * (srcQuad & 0b1);
                int sY = sqH * ((srcQuad >> 1) & 0b1);
                int dX = dqW * (i & 0b1);
                int dY = dqH * ((i >> 1) & 0b1);
                gfx.drawImage(srcImg, dX, dY, dX + dqW, dY + dqH, sX, sY, sX + sqW, sY + sqH, null);
            }
            val out = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", out);
            val bytes = out.toByteArray();
            return new GeneratedResource(bytes);
        }
    }

    @RequiredArgsConstructor
    private static class GeneratedResource implements IResource {
        private final byte[] bytes;

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public boolean hasMetadata() {
            return false;
        }

        @Override
        public IMetadataSection getMetadata(String sectionName) {
            return null;
        }
    }
}
