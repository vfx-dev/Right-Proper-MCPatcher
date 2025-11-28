package com.falsepattern.mcpatcher.internal.mixin.client.mob.compat.wdmla;

import com.falsepattern.mcpatcher.internal.modules.mob.MobEngine;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderManager.class)
public abstract class RenderManagerMobEngineMixin {

    @Unique
    private boolean mcpatcher$startedMobEngineHere;

    @Inject(method = "renderEntityWithPosYaw",
            at = @At("HEAD"),
            remap = true,
            require = 1)
    private void mcpatcher$preRenderEntityWithPosYaw(Entity entity,
                                                     double x, double y, double z,
                                                     float yaw, float partialTicks,
                                                     CallbackInfoReturnable<?> cir) {
        if (!MobEngine.isActive()) {
            mcpatcher$startedMobEngineHere = true;
            MobEngine.beginEntities();
        } else {
            mcpatcher$startedMobEngineHere = false;
        }

        if (MobEngine.isActive()) {
            MobEngine.nextEntity(entity);
        }
    }

    @Inject(method = "renderEntityWithPosYaw",
            at = @At("RETURN"),
            remap = true,
            require = 1)
    private void mcpatcher$postRenderEntityWithPosYaw(Entity entity,
                                                      double x, double y, double z,
                                                      float yaw, float partialTicks,
                                                      CallbackInfoReturnable<?> cir) {
        if (mcpatcher$startedMobEngineHere) {
            MobEngine.endEntities();
            mcpatcher$startedMobEngineHere = false;
        }
    }
}
