package io.github.riiimc.nostrum.mixinitf;


import net.minecraft.world.damagesource.DamageSource;

public interface LivingEntityMixinItf {
    void riiimc$directAttack(DamageSource source, float hp);

    void riiimc$directDie(DamageSource source);

    int riiimc$getDirectDeathTick();
    boolean riiimc$isDirectDead();


}
