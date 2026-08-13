package io.github.riiimc.nostrum.mixin.client;

import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(FontSet.class)
public interface FontSetAccessor {

    @Accessor("textures")
    List<ResourceLocation> nostrum$getTextures();
}