package io.github.riiimc.nostrum.mixin.direct_attack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.riiimc.nostrum.mixinitf.LivingEntityMixinItf;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Inject(
            method = "setupRotations",
            at = @At("HEAD"),
            cancellable = true)
    private void riiimc$directDeathAnimation(
            T entity,
            PoseStack poseStack,
            float bob,
            float bodyRotation,
            float partialTick,
            float scale,
            CallbackInfo ci
    ) {
        LivingEntityMixinItf entityItf =
                (LivingEntityMixinItf) entity;

        if (!entityItf.riiimc$isDirectDead()) {
            return;
        }

        if (entityItf.riiimc$getDirectDeathTick() <= 0) {
            return;
        }

        float deathTick =
                entityItf.riiimc$getDirectDeathTick() + partialTick;

        float f =
                (deathTick - 1.0F)
                        / 20.0F
                        * 1.6F;

        f = Mth.sqrt(f);

        if (f > 1.0F) {
            f = 1.0F;
        }

        poseStack.mulPose(
                Axis.ZP.rotationDegrees(
                        f * 90.0F
                )
        );
        ci.cancel();
    }

}