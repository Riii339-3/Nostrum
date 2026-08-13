package io.github.riiimc.nostrum.mixin.client;

import io.github.riiimc.nostrum.NostrumConfig;
import io.github.riiimc.nostrum.client.shaders.NostrumRenderTypes;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public class FontStringRenderOutputMixin {

    @Unique
    private static final ResourceLocation NOSTRUM$ALCHEMICAL_FONT =
            ResourceLocation.fromNamespaceAndPath(
                    "nostrum",
                    "alchemical"
            );
    @Unique
    private static final ResourceLocation NOSTRUM$PHILOSOPHER_STONE_FONT =
            ResourceLocation.fromNamespaceAndPath(
                    "nostrum",
                    "epilogue"
            );

    @Unique
    private static final ResourceLocation NOSTRUM$EPILOGUE_MOVE =
            ResourceLocation.fromNamespaceAndPath(
                    "nostrum",
                    "epilogue_move"
            );

    @Unique
    private Style nostrum$currentStyle;

    @Inject(
            method = "accept",
            at = @At("HEAD")
    )
    private void nostrum$acceptHead(
            int codePoint,
            Style style,
            int glyph,
            CallbackInfoReturnable<Boolean> cir
    ) {

        ResourceLocation font = style.getFont();

        if (NOSTRUM$ALCHEMICAL_FONT.equals(font)
                || NOSTRUM$PHILOSOPHER_STONE_FONT.equals(font)
                || NOSTRUM$EPILOGUE_MOVE.equals(font)) {
            this.nostrum$currentStyle = style;
        } else {
            this.nostrum$currentStyle = null;
        }
    }



    @Redirect(
            method = "accept",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/gui/Font;" +
                                    "getFontSet(" +
                                    "Lnet/minecraft/resources/ResourceLocation;" +
                                    ")" +
                                    "Lnet/minecraft/client/gui/font/FontSet;"
            )
    )
    private FontSet nostrum$redirectFontSet(
            Font font,
            ResourceLocation location
    ) {
        if (this.nostrum$currentStyle == null) {
            return ((FontAccessor) (Object) font)
                    .nostrum$getFontSet(location);
        }

        return ((FontAccessor) (Object) font)
                .nostrum$getFontSet(Style.DEFAULT_FONT);
    }


    @Redirect(
            method = "accept",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;" +
                                    "renderType(" +
                                    "Lnet/minecraft/client/gui/Font$DisplayMode;" +
                                    ")" +
                                    "Lnet/minecraft/client/renderer/RenderType;"
            )
    )
    private RenderType nostrum$redirectRenderType(
            BakedGlyph glyph,
            Font.DisplayMode displayMode
    ) {
        RenderType original = glyph.renderType(displayMode);

        Style style = this.nostrum$currentStyle;
        if (style == null) {
            return original;
        }

        ResourceLocation font = style.getFont();

        RenderType.CompositeState state =
                ((RenderTypeCompositeAccessor) (Object) original)
                        .nostrum$getState();

        RenderStateShard.EmptyTextureStateShard textureState =
                ((RenderTypeCompositeStateAccessor) (Object) state)
                        .nostrum$getTextureState();

        ResourceLocation texture =
                ((EmptyTextureStateShardInvoker) (Object) textureState)
                        .nostrum$cutoutTexture()
                        .orElse(null);

        if (texture == null) {
            return original;
        }

        if (NOSTRUM$ALCHEMICAL_FONT.equals(font)) {
            return NostrumRenderTypes.alchemical(texture);
        }

        if (NOSTRUM$PHILOSOPHER_STONE_FONT.equals(font)) {
            return NostrumRenderTypes.philosopherStone(texture);
        }

        if (NOSTRUM$EPILOGUE_MOVE.equals(font)) {
            return NostrumRenderTypes.epilogueMove(texture);
        }

        return original;
    }

    @Inject(
            method = "accept",
            at = @At("RETURN")
    )
    private void nostrum$acceptReturn(
            int codePoint,
            Style style,
            int glyph,
            CallbackInfoReturnable<Boolean> cir
    ) {
        this.nostrum$currentStyle = null;
    }
}