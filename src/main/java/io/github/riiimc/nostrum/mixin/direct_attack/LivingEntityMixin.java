package io.github.riiimc.nostrum.mixin.direct_attack;

import io.github.riiimc.nostrum.mixinitf.LivingEntityMixinItf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityMixinItf {
    @Unique
    private static final EntityDataAccessor<Boolean> RIIIMC_DIRECT_DEAD =
            SynchedEntityData.defineId(
                    LivingEntity.class,
                    EntityDataSerializers.BOOLEAN
            );
    @Invoker("dropAllDeathLoot")
    abstract void invokeDropAllDeathLoot(
            ServerLevel level,
            DamageSource source
    );


    @Invoker("getHurtSound")
    abstract SoundEvent invokeGetHurtSound(DamageSource source);

    @Invoker("shouldDropLoot")
    abstract boolean invokeShouldDropLoot();

    @Unique
    LivingEntityAccessor riiiMCScrollUpgrade$healthDataAccessor = (LivingEntityAccessor) this;
    @Unique
    private final LivingEntity riiiMCScrollUpgrade$living = (LivingEntity) (Object) this;

    @Unique
    public int riiimc$directDeathTick = 0;

    @Unique
    private void riiimc$playDirectHurtSound(DamageSource source) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide()) {
            return;
        }

        SoundEvent sound = this.invokeGetHurtSound(source);

        if (sound == null) {
            return;
        }

        entity.level().playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                sound,
                entity.getSoundSource(),
                1F,
                entity.getVoicePitch()
        );
    }

    @Override
    public int riiimc$getDirectDeathTick() {
        return this.riiimc$directDeathTick;
    }

    @Override
    public boolean riiimc$isDirectDead() {
        LivingEntity entity = (LivingEntity) (Object) this;

        return entity.getEntityData().get(RIIIMC_DIRECT_DEAD);
    }
    @Override
    public void riiimc$directAttack(DamageSource source, float hp) {
        riiiMCScrollUpgrade$living.hurtDuration = 10;
        riiiMCScrollUpgrade$living.hurtTime = riiiMCScrollUpgrade$living.hurtDuration;
        riiiMCScrollUpgrade$living.getEntityData().set(riiiMCScrollUpgrade$healthDataAccessor.getDataHealthId(), Mth.clamp(hp, 0.0F, riiiMCScrollUpgrade$living.getMaxHealth()));
        riiiMCScrollUpgrade$living.level().broadcastDamageEvent(riiiMCScrollUpgrade$living, source);
        riiiMCScrollUpgrade$living.hurtMarked = true;
        if (source.getEntity() instanceof Player player) {
            riiiMCScrollUpgrade$living.setLastHurtByPlayer(player);
        }

        if (riiiMCScrollUpgrade$living.getHealth() <= 0.0F) {
            if (riiiMCScrollUpgrade$living.level().isClientSide) {

            }
            else {
                ServerLevel level = riiiMCScrollUpgrade$living.level() instanceof ServerLevel serverLevel ? serverLevel : null;
                LivingEntity attacker = source.getEntity() instanceof LivingEntity livingEntity ? livingEntity : null;
                if (level != null && attacker != null) {
                    attacker.killedEntity(level, riiiMCScrollUpgrade$living);
                }
                riiimc$directDie(source);
                if (!riiiMCScrollUpgrade$living.getEntityData().get(RIIIMC_DIRECT_DEAD)) {
                    invokeDropAllDeathLoot(
                            level,
                            source
                    );
                }
                riiiMCScrollUpgrade$living.getEntityData()
                        .set(RIIIMC_DIRECT_DEAD, true);
            }
        }
        riiimc$playDirectHurtSound(source);

    }

    @Inject(
            method = "defineSynchedData",
            at = @At("TAIL")
    )
    private void riiimc$defineSynchedData(
            SynchedEntityData.Builder builder,
            CallbackInfo ci
    ) {
        builder.define(RIIIMC_DIRECT_DEAD, false);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntityMixinItf entityMixinItf = (LivingEntityMixinItf) (Object) this;
        if (entityMixinItf.riiimc$isDirectDead()) {
            riiimc$directDie(null);
        }
    }

    @Inject(method = "isDeadOrDying", at = @At("HEAD"), cancellable = true)
    private void onIsDeadOrDying(CallbackInfoReturnable<Boolean> cir) {
        LivingEntityMixinItf entityMixinItf = (LivingEntityMixinItf) (Object) this;
        if (entityMixinItf.riiimc$isDirectDead()) {
            cir.setReturnValue(true);
        }
    }


    @Inject(method = "isAlive", at = @At("HEAD"), cancellable = true)
    private void onIsAlive(CallbackInfoReturnable<Boolean> cir) {
        LivingEntityMixinItf entityMixinItf = (LivingEntityMixinItf) (Object) this;
        if (entityMixinItf.riiimc$isDirectDead()) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public void riiimc$directDie(DamageSource source) {
        EntityAccessor levelCallback = (EntityAccessor) this;
        Entity.RemovalReason reason = Entity.RemovalReason.KILLED;
        LivingEntity entity = (LivingEntity) (Object) this;

        entity.setPose(Pose.DYING);

        ++this.riiimc$directDeathTick;
        entity.deathTime = this.riiimc$directDeathTick;
        if (this.riiimc$directDeathTick == 1
                && !entity.level().isClientSide()) {

            entity.level().broadcastEntityEvent(entity, (byte) 3);
        }
        if (this.riiimc$directDeathTick >= 20
                && !entity.level().isClientSide()
                && !entity.isRemoved()) {

            ServerLevel level = entity.level() instanceof ServerLevel serverLevel ? serverLevel : null;
            if (level != null) {
            }

            entity.level().broadcastEntityEvent(entity, (byte) 60);
            levelCallback.riiimc$getLevelCallback().onRemove(reason);
        }
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void onDie(DamageSource source, CallbackInfo ci) {
        LivingEntityMixinItf entityMixinItf = (LivingEntityMixinItf) (Object) this;
        if (entityMixinItf.riiimc$isDirectDead()) {
            riiimc$directDie(null);
        }
    }

    @Inject(method = "getHealth", at = @At("HEAD"), cancellable = true)
    private void onGetHealth(CallbackInfoReturnable<Float> cir) {
        LivingEntityMixinItf entityMixinItf = (LivingEntityMixinItf) (Object) this;
        if (entityMixinItf.riiimc$isDirectDead()) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "setHealth", at = @At("HEAD"))
    private void onSetHealth(float hp, CallbackInfo ci) {
        LivingEntityMixinItf entityMixinItf = (LivingEntityMixinItf) (Object) this;
        if (entityMixinItf.riiimc$isDirectDead()) {
            hp = 0.0F;
        }
    }

    /*
    @Inject(method = "actuallyHurt", at = @At("HEAD"), cancellable = true)
    private void onActuallyHurt(DamageSource source, float amount, CallbackInfo ci) {
        if (source.getEntity() instanceof Player player && player.getMainHandItem().has(ScrollRegistries.INSTANCE.getSCROLL_DATA())) {
            ScrollDataComponent scrollData = player.getMainHandItem().get(ScrollRegistries.INSTANCE.getSCROLL_DATA());
            if (scrollData == null || !scrollData.getId().equals(ResourceLocation.fromNamespaceAndPath(RiiiMcScrolls.MODID, "direct_attack"))) return;
            LivingEntity entity = (LivingEntity) (Object) this;
            LivingEntityMixinItf livingItf = (LivingEntityMixinItf) entity;
            livingItf.riiimc$directAttack(source, entity.getHealth() - amount);
            ci.cancel();
        }
    }
     */

}
