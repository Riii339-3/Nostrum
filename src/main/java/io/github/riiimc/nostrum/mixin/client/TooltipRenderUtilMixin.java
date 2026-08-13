package io.github.riiimc.nostrum.mixin.client;

import io.github.riiimc.nostrum.client.shaders.NostrumRenderTypes;
import io.netty.util.internal.UnstableApi;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@UnstableApi
@Mixin(TooltipRenderUtil.class)
public abstract class TooltipRenderUtilMixin {

    @Inject(
            method = "renderTooltipBackground(Lnet/minecraft/client/gui/GuiGraphics;IIIIIIIII)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void nostrum$renderTooltipBackground(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int z,
            int backgroundStart,
            int backgroundEnd,
            int borderStart,
            int borderEnd,
            CallbackInfo ci
    ) {
        NostrumRenderTypes.tooltipBackground(
                graphics,
                x,
                y,
                width,
                height,
                z,
                backgroundStart,
                backgroundEnd,
                borderStart,
                borderEnd
        );

        ci.cancel();
    }
}