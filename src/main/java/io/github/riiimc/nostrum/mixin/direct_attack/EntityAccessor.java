package io.github.riiimc.nostrum.mixin.direct_attack;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(Entity.class)
public interface EntityAccessor {


    @Accessor("levelCallback")
    EntityInLevelCallback riiimc$getLevelCallback();

    @Accessor("levelCallback")
    void riiimc$setLevelCallback(EntityInLevelCallback callback);
}
