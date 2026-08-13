package io.github.riiimc.nostrum.client

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.Util
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation

object NostrumFontRenderTypes {

    private val ALCHEMICAL_SHADER =
        RenderStateShard.ShaderStateShard {
            NostrumShaders.ALCHEMICAL
        }

    private val ALCHEMICAL_CACHE =
        Util.memoize<ResourceLocation, RenderType> { texture ->

            RenderType.create(
                "nostrum_alchemical",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                786432,
                false,
                true,

                RenderType.CompositeState.builder()

                    .setShaderState(
                        ALCHEMICAL_SHADER
                    )

                    // ★ Fontが実際に使用しているatlas
                    .setTextureState(
                        RenderStateShard.TextureStateShard(
                            texture,
                            false,
                            false
                        )
                    )

                    .setTransparencyState(
                        RenderStateShard.TRANSLUCENT_TRANSPARENCY
                    )

                    .setLightmapState(
                        RenderStateShard.LIGHTMAP
                    )

                    .setOverlayState(
                        RenderStateShard.NO_OVERLAY
                    )

                    .createCompositeState(false)
            )
        }

    @JvmStatic
    fun alchemical(texture: ResourceLocation): RenderType {
        return ALCHEMICAL_CACHE.apply(texture)
    }
}