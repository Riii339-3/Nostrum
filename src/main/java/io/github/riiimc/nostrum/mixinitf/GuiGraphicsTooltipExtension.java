package io.github.riiimc.nostrum.mixinitf;


import net.minecraft.client.gui.GuiGraphics;

public interface GuiGraphicsTooltipExtension {

    void nostrum$renderTooltipBackground(
            int x,
            int y,
            int width,
            int height,
            int z,
            int backgroundStart,
            int backgroundEnd,
            int borderStart,
            int borderEnd
    );
}