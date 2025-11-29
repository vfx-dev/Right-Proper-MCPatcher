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

package com.falsepattern.mcpatcher.internal.mixin;

import com.falsepattern.lib.mixin.v2.MixinHelper;
import com.falsepattern.lib.mixin.v2.SidedMixins;
import com.falsepattern.lib.mixin.v2.TaggedMod;
import com.falsepattern.mcpatcher.Tags;
import com.falsepattern.mcpatcher.internal.config.MixinConfig;
import com.falsepattern.mcpatcher.internal.config.MixinConfig.CITMixinStrength;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.Language;

import java.util.function.BooleanSupplier;

import static com.falsepattern.lib.mixin.v2.MixinHelper.builder;
import static com.falsepattern.lib.mixin.v2.MixinHelper.require;

@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public enum Mixin implements IMixins {
    //@formatter:off
    ConnectedTextures(Phase.EARLY,
                      () -> MixinConfig.connectedTexturesMixins,
                      client("ctm.RenderBlocksMixin",
                             "ctm.TextureAtlasSpriteMixin",
                             "ctm.TextureMapMixin")),
    ConnectedTextures_NoOverlay(Phase.EARLY,
                                () -> MixinConfig.connectedTexturesMixins && !MixinConfig.resourcePackOverlayMixins,
                                client("ctm.TextureMapMixin_NoOverlay")),
    ConnectedTextures_Overlay(Phase.EARLY,
                              () -> MixinConfig.connectedTexturesMixins && MixinConfig.resourcePackOverlayMixins,
                              client("ctm.TextureMapMixin_Overlay")),

    RandomMobs(Phase.EARLY,
               () -> MixinConfig.randomMobsMixins,
               client("mob.EntityMixin",
                      "mob.RenderGlobalMixin",
                      "mob.GuiInventoryMixin",
                      "mob.TextureManagerMixin")),
    RandomMobs_DamageIndicators(Phase.LATE,
                                () -> MixinConfig.randomMobsMixins,
                                require(TargetMod.DamageIndicators),
                                client("mob.compat.damageindicators.DIGuiToolsMixin")),
    RandomMobs_WhatDreamMasterLooksAt(Phase.LATE,
                                      () -> MixinConfig.randomMobsMixins,
                                      require(TargetMod.WhatDreamMasterLooksAt),
                                      client("mob.compat.wdmla.GuiDrawMixin")),

    BetterGlass(Phase.EARLY,
                () -> MixinConfig.betterGlassMixins,
                client("glass.BlockBeaconMixin",
                       "glass.BlockGlassMixin",
                       "glass.BlockPaneMixin")),

    ResourcePackOverlay(Phase.EARLY,
                        () -> MixinConfig.resourcePackOverlayMixins,
                        client("overlay.TextureMapMixin")),

    CustomItemTextures(Phase.EARLY,
                       () -> MixinConfig.customItemTexturesMixins != CITMixinStrength.Disabled,
                       client("cit.item.RenderSnowballMixin"),
                       client("cit.item.EntityLivingBaseMixin"),
                       client("cit.item.EntityBreakingFXMixin"),
                    // TODO: Implement CIT Enchantments
                    // client("cit.enchant.RendererLivingEntityMixin"),
                    // client("cit.enchant.RenderBipedMixin"),
                    // client("cit.enchant.RenderPlayerMixin"),
                       client("cit.armor.RenderBipedMixin")),
    CustomItemTextures_Weak(Phase.EARLY,
                       () -> MixinConfig.customItemTexturesMixins == CITMixinStrength.Weak,
                       client("cit.item.ItemMixin_Weak")),
    CustomItemTextures_Regular(Phase.EARLY,
                            () -> MixinConfig.customItemTexturesMixins == CITMixinStrength.Regular,
                            client("cit.item.ItemMixin_Regular")),
    CustomItemTextures_Epic(Phase.EARLY,
                            () -> MixinConfig.customItemTexturesMixins == CITMixinStrength.Epic,
                            client("cit.item.ItemMixin_Epic")),
    CustomItemTextures_NoOverlay(Phase.EARLY,
                                 () -> MixinConfig.customItemTexturesMixins != CITMixinStrength.Disabled &&
                                       !MixinConfig.resourcePackOverlayMixins,
                                client("cit.TextureMapMixin_NoOverlay")),
    CustomItemTextures_Overlay(Phase.EARLY,
                               () -> MixinConfig.customItemTexturesMixins != CITMixinStrength.Disabled &&
                                     MixinConfig.resourcePackOverlayMixins,
                              client("cit.TextureMapMixin_Overlay")),

    //@formatter:on

    //region boilerplate
    ;
    @Getter
    private final MixinBuilder builder;

    Mixin(Phase phase, SidedMixins... mixins) {
        this(builder(mixins).setPhase(phase));
    }

    Mixin(Phase phase, BooleanSupplier cond, SidedMixins... mixins) {
        this(builder(cond, mixins).setPhase(phase));
    }

    Mixin(Phase phase, TaggedMod mod, SidedMixins... mixins) {
        this(builder(mod, mixins).setPhase(phase));
    }

    Mixin(Phase phase, TaggedMod[] mods, SidedMixins... mixins) {
        this(builder(mods, mixins).setPhase(phase));
    }

    Mixin(Phase phase, BooleanSupplier cond, TaggedMod mod, SidedMixins... mixins) {
        this(builder(cond, mod, mixins).setPhase(phase));
    }

    Mixin(Phase phase, BooleanSupplier cond, TaggedMod[] mods, SidedMixins... mixins) {
        this(builder(cond, mods, mixins).setPhase(phase));
    }

    private static SidedMixins common(@Language(value = "JAVA",
                                                prefix = "import " + Tags.ROOT_PKG + ".internal.mixin.mixins.common.",
                                                suffix = ";") String... mixins) {
        return MixinHelper.common(mixins);
    }

    private static SidedMixins client(@Language(value = "JAVA",
                                                prefix = "import " + Tags.ROOT_PKG + ".internal.mixin.mixins.client.",
                                                suffix = ";") String... mixins) {
        return MixinHelper.client(mixins);
    }

    private static SidedMixins server(@Language(value = "JAVA",
                                                prefix = "import " + Tags.ROOT_PKG + ".internal.mixin.mixins.server.",
                                                suffix = ";") String... mixins) {
        return MixinHelper.server(mixins);
    }
    //endregion
}
