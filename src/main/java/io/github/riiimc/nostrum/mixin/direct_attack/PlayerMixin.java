package io.github.riiimc.nostrum.mixin.direct_attack;

import io.github.riiimc.nostrum.Nostrum;
import io.github.riiimc.nostrum.NostrumRegistries;
import io.github.riiimc.nostrum.content.components.AlchemicalUpgradeComponent;
import io.github.riiimc.nostrum.mixinitf.LivingEntityMixinItf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(
            method = "attack",
            at = @At("HEAD"),
            cancellable = true)
    private void onAttack(Entity entity, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!(entity instanceof LivingEntity living)) return;

        if (player.getMainHandItem().has(NostrumRegistries.getALCHEMICAL_UPGRADE_COMPONENT())) {
            AlchemicalUpgradeComponent scrollData = player.getMainHandItem().get(NostrumRegistries.getALCHEMICAL_UPGRADE_COMPONENT());
            if (scrollData == null) return;
            if (!scrollData.getId().equals(ResourceLocation.fromNamespaceAndPath(Nostrum.MODID, "direct_attack"))) return;
            float damage = (float) Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
            LivingEntityMixinItf livingItf = (LivingEntityMixinItf) living;
            livingItf.riiimc$directAttack(living.level().damageSources().source(DamageTypes.MOB_ATTACK, player),living.getHealth() - (damage * 0.2f));
            if (!living.level().isClientSide()) {
                living.level().playSound(
                        null,
                        living.blockPosition(),
                        SoundEvents.PLAYER_ATTACK_STRONG,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );
            }
            ci.cancel();
        }
    }
}
