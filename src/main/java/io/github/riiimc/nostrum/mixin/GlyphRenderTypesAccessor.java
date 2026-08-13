package io.github.riiimc.nostrum.mixin;

import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlyphRenderTypes.class)
public interface GlyphRenderTypesAccessor {

    @Accessor("normal")
    RenderType nostrum$getNormal();

    @Accessor("seeThrough")
    RenderType nostrum$getSeeThrough();

    @Accessor("polygonOffset")
    RenderType nostrum$getPolygonOffset();
}