package io.github.riiimc.nostrum.mixin.client;

import io.github.riiimc.nostrum.NostrumConfig;
import io.github.riiimc.nostrum.NostrumRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(
            method = "getHoverName",
            at = @At("RETURN"),
            cancellable = true
    )
    private void nostrum$alchemicalName(
            CallbackInfoReturnable<Component> cir
    ) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.has(
                NostrumRegistries.getALCHEMICAL_UPGRADE_COMPONENT().get()
        )) {
            return;
        }

        Component name = cir.getReturnValue();

        cir.setReturnValue(
                name.copy().withStyle(ChatFormatting.valueOf("ALCHEMICAL"))
        );
    }
}