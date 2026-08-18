package io.github.riiimc.nostrum.client.shaders

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import java.time.LocalTime



object NostrumRenderTypes {

    // =========================================================
    // Alchemical Font
    // =========================================================

    @JvmField
    val ALCHEMICAL_SHADER =
        RenderStateShard.ShaderStateShard {
            NostrumShaders.ALCHEMICAL
        }

    @JvmField
    val PHILOSOPHER_STONE_SHADER =
        RenderStateShard.ShaderStateShard {
            NostrumShaders.PHILOSOPHER_STONE
        }

    private val PHILOSOPHER_STONE_CACHE =
        Util.memoize<ResourceLocation, RenderType> { texture ->

            RenderType.create(
                "epilogue",

                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,

                VertexFormat.Mode.QUADS,

                786432,

                false,
                true,

                RenderType.CompositeState.builder()

                    .setShaderState(
                        PHILOSOPHER_STONE_SHADER
                    )

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
    fun philosopherStone(
        texture: ResourceLocation
    ): RenderType {
        return PHILOSOPHER_STONE_CACHE.apply(texture)
    }

    @JvmField
    val EPILOGUE_MOVE =
        RenderStateShard.ShaderStateShard {
            NostrumShaders.EPILOGUE_MOVE
        }

    private val EPILOGUE_MOVE_CACHE =
        Util.memoize<ResourceLocation, RenderType> { texture ->

            RenderType.create(
                "epilogue_move",

                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,

                VertexFormat.Mode.QUADS,

                786432,

                false,
                true,

                RenderType.CompositeState.builder()

                    .setShaderState(
                        EPILOGUE_MOVE
                    )

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
    fun epilogueMove(
        texture: ResourceLocation
    ): RenderType {
        return EPILOGUE_MOVE_CACHE.apply(texture)
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
    fun alchemical(
        texture: ResourceLocation
    ): RenderType {
        return ALCHEMICAL_CACHE.apply(texture)
    }




    // =========================================================
    // Tooltip Background
    // =========================================================

    @JvmField
    val TOOLTIP_SHADER =
        RenderStateShard.ShaderStateShard {
            NostrumShaders.TOOLTIP_BACKGROUND
        }


    private val TOOLTIP_BACKGROUND: RenderType =
        RenderType.create(
            "nostrum_tooltip_background",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            786432,
            false,
            true,

            RenderType.CompositeState.builder()
                .setShaderState(TOOLTIP_SHADER)

                .setTransparencyState(
                    RenderStateShard.TRANSLUCENT_TRANSPARENCY
                )

                .setDepthTestState(
                    RenderStateShard.NO_DEPTH_TEST
                )

                .setCullState(
                    RenderStateShard.NO_CULL
                )

                .setLightmapState(
                    RenderStateShard.LIGHTMAP
                )

                .setOverlayState(
                    RenderStateShard.NO_OVERLAY
                )

                .createCompositeState(false)
        )
    @JvmStatic
    fun tooltipBackground(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        z: Int,
        backgroundStart: Int,
        backgroundEnd: Int,
        borderStart: Int,
        borderEnd: Int,
    ) {
        val pose = graphics.pose().last().pose()

        val left = x - 3
        val top = y - 3
        val actualWidth = width + 6
        val actualHeight = height + 6

        val right = left + actualWidth
        val bottom = top + actualHeight

        val now = LocalTime.now()
        val localSeconds = now.toSecondOfDay().toFloat()

        val minecraft = Minecraft.getInstance()
        val level = minecraft.level

        val gameTime =
            (level?.gameTime?.toFloat() ?: 0.0f) +
                    minecraft.timer.getGameTimeDeltaPartialTick(false)

        NostrumShaders.updateTooltipBackground(
            gameTime / 20.0f,
            localSeconds,
            left.toFloat(),
            top.toFloat(),
            actualWidth.toFloat(),
            actualHeight.toFloat()
        )

        val buffer = graphics.bufferSource()
            .getBuffer(TOOLTIP_BACKGROUND)

        buffer.addVertex(
            pose,
            left.toFloat(),
            top.toFloat(),
            z.toFloat()
        ).setColor(255, 255, 255, 255)

        buffer.addVertex(
            pose,
            left.toFloat(),
            bottom.toFloat(),
            z.toFloat()
        ).setColor(255, 255, 255, 255)

        buffer.addVertex(
            pose,
            right.toFloat(),
            bottom.toFloat(),
            z.toFloat()
        ).setColor(255, 255, 255, 255)

        buffer.addVertex(
            pose,
            right.toFloat(),
            top.toFloat(),
            z.toFloat()
        ).setColor(255, 255, 255, 255)
    }

    @JvmField
    val EPILOGUE_TOOLTIP_SHADER =
        RenderStateShard.ShaderStateShard {
            NostrumShaders.EPILOGUE_TOOLTIP
        }


    private val EPILOGUE_TOOLTIP_BACKGROUND: RenderType =
        RenderType.create(
            "nostrum_epilogue_tooltip_background",

            DefaultVertexFormat.POSITION_COLOR,

            VertexFormat.Mode.QUADS,

            786432,

            false,
            true,

            RenderType.CompositeState.builder()
                .setShaderState(
                    EPILOGUE_TOOLTIP_SHADER
                )

                .setTransparencyState(
                    RenderStateShard.TRANSLUCENT_TRANSPARENCY
                )

                .setDepthTestState(
                    RenderStateShard.NO_DEPTH_TEST
                )

                .setCullState(
                    RenderStateShard.NO_CULL
                )

                .setLightmapState(
                    RenderStateShard.LIGHTMAP
                )

                .setOverlayState(
                    RenderStateShard.NO_OVERLAY
                )

                .createCompositeState(false)
        )

    @JvmStatic
    fun epilogueTooltipBackground(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        z: Int,
        backgroundStart: Int,
        backgroundEnd: Int,
        borderStart: Int,
        borderEnd: Int,
    ) {
        val pose = graphics.pose().last().pose()

        val left = x - 3
        val top = y - 3
        val actualWidth = width + 6
        val actualHeight = height + 6

        val right = left + actualWidth
        val bottom = top + actualHeight

        val now = LocalTime.now()
        val localSeconds = now.toSecondOfDay().toFloat()

        val minecraft = Minecraft.getInstance()
        val level = minecraft.level

        val gameTime =
            (level?.gameTime?.toFloat() ?: 0.0f) +
                    minecraft.timer.getGameTimeDeltaPartialTick(false)

        NostrumShaders.updateEpilogueTooltip(
            gameTime / 20.0f,
            localSeconds,
            left.toFloat(),
            top.toFloat(),
            actualWidth.toFloat(),
            actualHeight.toFloat()
        )

        val buffer = graphics.bufferSource()
            .getBuffer(EPILOGUE_TOOLTIP_BACKGROUND)

        fun vertex(
            x: Float,
            y: Float,
            u: Float,
            v: Float
        ) {
            buffer.addVertex(
                pose,
                x,
                y,
                z.toFloat()
            )
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setUv2(0, 0)
        }

        vertex(
            left.toFloat(),
            top.toFloat(),
            0.0f,
            0.0f
        )

        vertex(
            left.toFloat(),
            bottom.toFloat(),
            0.0f,
            1.0f
        )

        vertex(
            right.toFloat(),
            bottom.toFloat(),
            1.0f,
            1.0f
        )

        vertex(
            right.toFloat(),
            top.toFloat(),
            1.0f,
            0.0f
        )
    }
}