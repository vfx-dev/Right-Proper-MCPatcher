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

import com.falsepattern.mcpatcher.internal.modules.ctm.ICTMSprite;
import com.falsepattern.mcpatcher.internal.modules.ctm.TextureMapExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import lombok.val;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.IIcon;

/**
 * Priority 900 so that applyOverlay lands before Voxelizer TextureMapMixin loadExtraTexturesForLayers
 */
@Mixin(value = TextureMap.class,
       priority = 900)
public abstract class TextureMapMixin extends AbstractTexture {
    @Shadow
    @Final
    private int textureType;
    @Shadow
    @Final
    private TextureAtlasSprite missingImage;

    @Shadow
    @Final
    private String basePath;
    @Unique
    private int mcp$indexCounter = 0;

    @Unique
    private static boolean mcp$isCTMPath(String path) {
        return path.startsWith("mcpatcher/");
    }

    @Inject(method = "<init>(ILjava/lang/String;Z)V",
            at = @At("RETURN"))
    private void grabTextures(int p_i1281_1_, String p_i1281_2_, boolean skipFirst, CallbackInfo ci) {
        if (this.textureType == 0) {
            TextureMapExtension.textureMapBlocks = (TextureMap) (Object) this;
        } else if (this.textureType == 1) {
            TextureMapExtension.textureMapItems = (TextureMap) (Object) this;
        }
    }

    @Inject(method = "loadTexture",
            at = @At("HEAD"),
            require = 1)
    private void resetCounter(IResourceManager resourceManager, CallbackInfo ci) {
        mcp$indexCounter = 0;
    }

    @Inject(method = "initMissingImage",
            at = @At("RETURN"),
            require = 1)
    private void setMissingImageIndex(CallbackInfo ci) {
        ((ICTMSprite) this.missingImage).mcp$indexInMap(mcp$indexCounter++);
    }

    @Inject(method = "loadTextureAtlas",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;hasCustomLoader(Lnet/minecraft/client/resources/IResourceManager;Lnet/minecraft/util/ResourceLocation;)Z"),
            require = 1)
    private void setIndex_LoadTextureAtlas(IResourceManager resourceManager,
                                           CallbackInfo ci,
                                           @Local TextureAtlasSprite sprite) {
        val ctmSprite = (ICTMSprite) sprite;
        if (ctmSprite.mcp$indexInMap() < 0) {
            ctmSprite.mcp$indexInMap(mcp$indexCounter++);
        }
    }

    @Inject(method = "registerIcon",
            at = @At(value = "INVOKE",
                     target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                     shift = At.Shift.AFTER),
            require = 1)
    private void setIndex_registerIcon(String getTextureName,
                                       CallbackInfoReturnable<IIcon> cir,
                                       @Local Object texture) {
        if (!(texture instanceof ICTMSprite)) {
            return;
        }
        val ctmSprite = (ICTMSprite) texture;
        if (ctmSprite.mcp$indexInMap() < 0) {
            ctmSprite.mcp$indexInMap(mcp$indexCounter++);
        }
    }

    @Inject(method = "setTextureEntry",
            at = @At(value = "INVOKE",
                     target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                     shift = At.Shift.AFTER),
            remap = false,
            require = 1)
    private void setIndex_setTextureEntry(String name, TextureAtlasSprite entry, CallbackInfoReturnable<Boolean> cir) {
        val ctmSprite = (ICTMSprite) entry;
        if (ctmSprite.mcp$indexInMap() < 0) {
            ctmSprite.mcp$indexInMap(mcp$indexCounter++);
        }
    }

    @WrapOperation(method = "completeResourceLocation",
                   at = @At(value = "INVOKE",
                            target = "Ljava/lang/String;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
                            ordinal = 0),
                   slice = @Slice(from = @At(value = "CONSTANT",
                                             args = "stringValue=%s/%s%s")),
                   require = 1)
    private String completeResourceLocationStandard(String format, Object[] args, Operation<String> original) {
        if (mcp$isCTMPath((String) args[1])) {
            return original.call("%s%s", new Object[]{args[1], args[2]});
        } else {
            return original.call(format, args);
        }
    }

    @WrapOperation(method = "completeResourceLocation",
                   at = @At(value = "INVOKE",
                            target = "Ljava/lang/String;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
                            ordinal = 0),
                   slice = @Slice(from = @At(value = "CONSTANT",
                                             args = "stringValue=%s/mipmaps/%s.%d%s")),
                   require = 1)
    private String completeResourceLocationMipmap(String format, Object[] args, Operation<String> original) {
        if (mcp$isCTMPath((String) args[1])) {
            return original.call("%smipmap%d%s", new Object[]{args[1], args[2], args[3]});
        } else {
            return original.call(format, args);
        }
    }
}
