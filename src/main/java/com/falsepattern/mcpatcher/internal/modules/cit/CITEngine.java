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

package com.falsepattern.mcpatcher.internal.modules.cit;

import com.falsepattern.mcpatcher.Tags;
import com.falsepattern.mcpatcher.internal.modules.common.CollectionUtil;
import com.falsepattern.mcpatcher.internal.modules.common.Identity2ObjectHashMap;
import com.falsepattern.mcpatcher.internal.modules.common.ResourceScanner;
import com.falsepattern.mcpatcher.internal.modules.overlay.ResourceGenerator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;

public final class CITEngine {
    static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + " CIT");

    private static CITPropsGlobal globalProps = new CITPropsGlobal();

    private static @Unmodifiable Object2ObjectMap<Item, ObjectList<CITPropsItem>> itemProps = Object2ObjectMaps.emptyMap();
    private static @Unmodifiable Object2ObjectMap<Item, ObjectList<CITPropsArmor>> armorProps = Object2ObjectMaps.emptyMap();

    private CITEngine() {
        throw new UnsupportedOperationException();
    }

    /**
     * @implNote Called after resources have been reloaded.
     */
    public static void reloadResources() {
        LOG.debug("Reloading Resources");

        CITParticleHandler.reset();

        try {
            val res = ResourceScanner.getResource(new ResourceLocation("minecraft:mcpatcher/cit/cit.properties"));
            val props = new Properties();
            props.load(res.getInputStream());
            globalProps = new CITPropsGlobal(props);
            LOG.debug("Loaded custom cit.properties");
        } catch (IOException e) {
            globalProps = new CITPropsGlobal();
            LOG.debug("Loaded default cit.properties");
        }
    }

    public static void updateIcons(@NotNull TextureMap textureMap,
                                   @Nullable Map<ResourceLocation, ResourceGenerator> overlay) {
        LOG.debug("Updating Icons");

        itemProps = new Identity2ObjectHashMap<>();
        armorProps = new Identity2ObjectHashMap<>();

        val packs = ResourceScanner.resourcePacks();
        for (val pack : packs) {
            if (pack == null) {
                continue;
            }
            updateIcons(textureMap, overlay, pack);
        }

        itemProps = lockMapRecursive(itemProps);
        armorProps = lockMapRecursive(armorProps);
    }

    private static void updateIcons(@NotNull TextureMap textureMap,
                                    @Nullable Map<ResourceLocation, ResourceGenerator> overlay,
                                    @NotNull IResourcePack pack) {
        val names = ResourceScanner.collectFiles(pack, "mcpatcher/cit/", ".properties", false);
        names.sort(Comparator.naturalOrder());

        for (val name : names) {
            // Skip global properties
            if (name.endsWith("cit.properties")) {
                continue;
            }

            LOG.debug("CustomItemTextures: {}", name);
            try {
                val loc = new ResourceLocation(name);
                val is = pack.getInputStream(loc);
                if (is == null) {
                    throw new FileNotFoundException(name);
                }
                val props = new Properties();
                props.load(is);
                val type = props.getProperty("type", "item");
                switch (type) {
                    case "item":
                        addItemInfo(textureMap, overlay, name, props);
                        break;
                    case "enchantment":
                    case "overlay":
                        LOG.warn("type={} not implemented!", type);
                        addEnchantmentInfo(textureMap, overlay, name, props);
                        break;
                    case "armor":
                        addArmorInfo(textureMap, overlay, name, props);
                        break;
                    default:
                        LOG.warn("Invalid [type={}] for: {}", type, name);
                }
            } catch (FileNotFoundException e) {
                LOG.warn("ConnectedTextures file not found: {}", name);
            } catch (IOException | RuntimeException e) {
                LOG.warn(new FormattedMessage("Error while loading custom item texture: {}", name), e);
            }
        }
    }

    private static void addItemInfo(@NotNull TextureMap textureMap,
                                    @Nullable Map<ResourceLocation, ResourceGenerator> overlay,
                                    @NotNull String name,
                                    @NotNull Properties props) {
        val citProps = new CITPropsItem(name, props);
        if (!citProps.isValid()) {
            return;
        }
        citProps.load(textureMap, overlay);
        if (!citProps.shouldKeep()) {
            return;
        }

        val items = citProps.items();
        for (val item : items) {
            itemProps.computeIfAbsent(item, key -> new ObjectArrayList<>())
                     .add(citProps);
        }
    }

    private static void addEnchantmentInfo(@NotNull TextureMap textureMap,
                                           @Nullable Map<ResourceLocation, ResourceGenerator> overlay,
                                           @NotNull String name,
                                           @NotNull Properties props) {
        // TODO: Implement CIT Enchantments
    }

    private static void addArmorInfo(@NotNull TextureMap textureMap,
                                     @Nullable Map<ResourceLocation, ResourceGenerator> overlay,
                                     @NotNull String name,
                                     @NotNull Properties props) {
        val citProps = new CITPropsArmor(name, props);
        if (!citProps.isValid()) {
            return;
        }
        citProps.load(textureMap, overlay);
        if (!citProps.shouldKeep()) {
            return;
        }

        val items = citProps.items();
        for (val item : items) {
            armorProps.computeIfAbsent(item, key -> new ObjectArrayList<>())
                      .add(citProps);
        }
    }

    private static <T extends CITPropsSingle> @Unmodifiable Object2ObjectMap<Item, ObjectList<T>> lockMapRecursive(
            Object2ObjectMap<Item, ObjectList<T>> map) {
        val temp = new Identity2ObjectHashMap<Item, ObjectList<T>>();
        for (val entry : map.entrySet()) {
            val item = entry.getKey();
            val infos = entry.getValue();
            if (infos.isEmpty()) {
                continue;
            }
            infos.sort(CITPropsSingle::compareTo);
            temp.put(item, CollectionUtil.lockList(infos));
        }
        return CollectionUtil.lockMap(temp);
    }

    public static IIcon replaceIcon(@Nullable ItemStack itemStack, @Nullable IIcon original) {
        if (itemProps.isEmpty()) {
            return original;
        }

        if (itemStack == null || original == null) {
            return original;
        }
        val item = itemStack.getItem();
        if (item == null) {
            return original;
        }
        val list = itemProps.get(item);
        if (list == null) {
            return original;
        }

        for (val info : list) {
            if (info.matches(itemStack)) {
                return info.getIcon(original);
            }
        }
        return original;
    }

    /**
     * Wraps the fetching of an armor texture.
     *
     * @param itemStack Item Stack containing the armor
     * @param original  Original texture
     *
     * @return Either the {@code original} texture or a replacement
     *
     * @implNote Called from the big forge hook: {@link RenderBiped#getArmorResource}
     */
    public static ResourceLocation replaceArmorTexture(ItemStack itemStack, ResourceLocation original) {
        if (armorProps.isEmpty()) {
            return original;
        }

        if (itemStack == null || original == null) {
            return original;
        }
        val item = itemStack.getItem();
        if (item == null) {
            return original;
        }
        val list = armorProps.get(item);
        if (list == null) {
            return original;
        }

        for (val info : list) {
            if (info.matches(itemStack)) {
                return info.getTexture(original);
            }
        }
        return original;
    }

    /**
     * Wraps the rendering of entity armor glint.
     *
     * @param entity      Target entity wearing the armor
     * @param itemStack   Item Stack containing the armor
     * @param partialTick Partial tick (0-1)
     * @param renderFn    The wrapped render function
     *
     * @return {@code true} if rendering was done, {@code false} if vanilla code should run
     *
     * @implNote The {@code renderFn} is captured to the first call to {@link ModelBase#render} in {@link RendererLivingEntity#doRender}
     */
    // TODO: Implement CIT Enchantments
    public static boolean renderArmorGlint(EntityLivingBase entity,
                                           ItemStack itemStack,
                                           float partialTick,
                                           Runnable renderFn) {

        // TODO: This is the example vanilla code extracted for convenience
        val f8 = (float) entity.ticksExisted + partialTick;
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));
        GL11.glEnable(GL11.GL_BLEND);
        val f9 = 0.5F;
        GL11.glColor4f(f9, f9, f9, 1.0F);
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDepthMask(false);

        for (int k = 0; k < 2; ++k) {
            GL11.glDisable(GL11.GL_LIGHTING);
            val f10 = 0.76F;
            GL11.glColor4f(0.5F * f10, 0.25F * f10, 0.8F * f10, 1.0F);
            GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glLoadIdentity();
            float f11 = f8 * (0.001F + (float) k * 0.003F) * 20.0F;
            float f12 = 0.33333334F;
            GL11.glScalef(f12, f12, f12);

            // Note how 'k' is being used here for the shift
            GL11.glRotatef(30.0F - (float) k * 60.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(0.0F, f11, 0.0F);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            renderFn.run();
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glDepthMask(true);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        // TODO: In this example, we are always redirecting all render calls
        return true;
    }
}
