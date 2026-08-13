package io.github.riiimc.nostrum.client.shaders

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.client.event.RegisterShadersEvent
import org.joml.Vector3f
import java.io.IOException


object NostrumShaders {

    @JvmField
    var ALCHEMICAL: ShaderInstance? = null

    @JvmField
    var PHILOSOPHER_STONE: ShaderInstance? = null



    @JvmField
    var TOOLTIP_BACKGROUND: ShaderInstance? = null


    @Throws(IOException::class)
    fun registerShaders(event: RegisterShadersEvent) {

        // =====================================================
        // Alchemical Font
        // =====================================================

        event.registerShader(
            ShaderInstance(
                event.resourceProvider,
                ResourceLocation.fromNamespaceAndPath(
                    "nostrum",
                    "alchemical"
                ),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP
            )
        ) { shader ->
            ALCHEMICAL = shader
        }


        // =====================================================
        // Alchemical Tooltip
        // =====================================================

        event.registerShader(
            ShaderInstance(
                event.resourceProvider,
                ResourceLocation.fromNamespaceAndPath(
                    "nostrum",
                    "tooltip_background"
                ),
                DefaultVertexFormat.POSITION_COLOR
            )
        ) { shader ->
            TOOLTIP_BACKGROUND = shader
        }

        event.registerShader(
            ShaderInstance(
                event.resourceProvider,
                ResourceLocation.fromNamespaceAndPath(
                    "nostrum",
                    "epilogue"
                ),
                DefaultVertexFormat.POSITION_COLOR
            )
        ) { shader ->
            PHILOSOPHER_STONE = shader
        }
        event.registerShader(
            ShaderInstance(
                event.resourceProvider,
                ResourceLocation.fromNamespaceAndPath(
                    "nostrum",
                    "epilogue_tooltip"
                ),
                DefaultVertexFormat.POSITION_COLOR
            )
        ) { shader ->
            EPILOGUE_TOOLTIP = shader
        }

        event.registerShader(
            ShaderInstance(
                event.resourceProvider,
                ResourceLocation.fromNamespaceAndPath(
                    "nostrum",
                    "epilogue_move"
                ),
                DefaultVertexFormat.POSITION_COLOR
            )
        ) { shader ->
            EPILOGUE_MOVE = shader
        }

    }


    @JvmField
    var EPILOGUE_MOVE: ShaderInstance? = null


    // =========================================================
    // Font
    // =========================================================

    fun updatePhilosopherStone(
        gameTime: Float,
        speed: Float,
        intensity: Float,
        colorA: Vector3f,
        colorB: Vector3f
    ) {
        val shader = PHILOSOPHER_STONE ?: return

        shader.getUniform("NostrumTime")
            ?.set(gameTime)

        shader.getUniform("Speed")
            ?.set(speed)

        shader.getUniform("Intensity")
            ?.set(intensity)

        shader.getUniform("ColorA")
            ?.set(
                colorA.x,
                colorA.y,
                colorA.z
            )

        shader.getUniform("ColorB")
            ?.set(
                colorB.x,
                colorB.y,
                colorB.z
            )
    }

    fun updateEpilogueMove(
        gameTime: Float,
        speed: Float,
        intensity: Float,
        colorA: Vector3f,
        colorB: Vector3f
    ) {
        val shader = EPILOGUE_MOVE ?: return

        shader.getUniform("NostrumTime")
            ?.set(gameTime)

        shader.getUniform("Speed")
            ?.set(speed)

        shader.getUniform("Intensity")
            ?.set(intensity)

        shader.getUniform("ColorA")
            ?.set(
                colorA.x,
                colorA.y,
                colorA.z
            )

        shader.getUniform("ColorB")
            ?.set(
                colorB.x,
                colorB.y,
                colorB.z
            )
    }

    fun updateAlchemical(
        gameTime: Float,
        speed: Float,
        intensity: Float,
        colorA: Vector3f,
        colorB: Vector3f
    ) {
        val shader = ALCHEMICAL ?: return

        shader.getUniform("NostrumTime")
            ?.set(gameTime)

        shader.getUniform("Speed")
            ?.set(speed)

        shader.getUniform("Intensity")
            ?.set(intensity)

        shader.getUniform("ColorA")
            ?.set(
                colorA.x,
                colorA.y,
                colorA.z
            )

        shader.getUniform("ColorB")
            ?.set(
                colorB.x,
                colorB.y,
                colorB.z
            )
    }


    // =========================================================
    // Tooltip
    // =========================================================

    fun updateTooltipTime(time: Float) {
        println("SET NostrumTime = $time")
        TOOLTIP_BACKGROUND
            ?.getUniform("NostrumTime")
            ?.set(time)

    }
    private var tooltipTime: Float = 0.0f

    fun updateTooltipBackground(
        time: Float,
        realTime: Float,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val shader = TOOLTIP_BACKGROUND ?: return

        shader.getUniform("NostrumTime")?.set(time)
        shader.getUniform("RealTime")?.set(realTime)

        shader.getUniform("TooltipX")?.set(x)
        shader.getUniform("TooltipY")?.set(y)
        shader.getUniform("TooltipWidth")?.set(width)
        shader.getUniform("TooltipHeight")?.set(height)
    }

    fun updateTooltipBounds(
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val shader = TOOLTIP_BACKGROUND ?: return

        shader.getUniform("TooltipX")?.set(x)
        shader.getUniform("TooltipY")?.set(y)
        shader.getUniform("TooltipWidth")?.set(width)
        shader.getUniform("TooltipHeight")?.set(height)
    }

    fun updateTooltipBackground(time: Float) {
        val shader = TOOLTIP_BACKGROUND ?: return

        shader.getUniform("NostrumTime")?.set(time)
    }
    @JvmField
    var EPILOGUE_TOOLTIP: ShaderInstance? = null

    fun updateEpilogueTooltip(
        gameTime: Float,
        realTime: Float,
        tooltipX: Float,
        tooltipY: Float,
        tooltipWidth: Float,
        tooltipHeight: Float
    ) {
        val shader = EPILOGUE_TOOLTIP ?: return

        shader.getUniform("NostrumTime")
            ?.set(gameTime)

        shader.getUniform("RealTime")
            ?.set(realTime)

        shader.getUniform("TooltipX")
            ?.set(tooltipX)

        shader.getUniform("TooltipY")
            ?.set(tooltipY)

        shader.getUniform("TooltipWidth")
            ?.set(tooltipWidth)

        shader.getUniform("TooltipHeight")
            ?.set(tooltipHeight)
    }
}