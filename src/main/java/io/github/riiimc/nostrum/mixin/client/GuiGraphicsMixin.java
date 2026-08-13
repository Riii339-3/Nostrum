package io.github.riiimc.nostrum.mixin.client;

import io.github.riiimc.nostrum.NostrumConfig;
import io.github.riiimc.nostrum.NostrumRegistries;
import io.github.riiimc.nostrum.client.shaders.NostrumRenderTypes;
import io.github.riiimc.nostrum.content.tiers.NostrumRarities;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Shadow
    private ItemStack tooltipStack;

    @Redirect(
            method = "lambda$renderTooltipInternal$3",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/TooltipRenderUtil;renderTooltipBackground(Lnet/minecraft/client/gui/GuiGraphics;IIIIIIIII)V"
            )
    )
    private void nostrum$renderTooltipBackground(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int z,
            int backgroundStart,
            int backgroundEnd,
            int borderStart,
            int borderEnd
    ) {
        ItemStack stack = this.tooltipStack;

        if (stack.get(NostrumRegistries.getALCHEMICAL_UPGRADE_COMPONENT()) != null) {
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
            return;
        }

        if (stack.getRarity().equals(NostrumRarities.EPILOGUE_ENUM_PROXY.getValue())) {
            NostrumRenderTypes.epilogueTooltipBackground(
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
            return;
        }

        TooltipRenderUtil.renderTooltipBackground(
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
    }
}