package io.github.riiimc.nostrum.compat.emi

import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import io.github.riiimc.nostrum.Nostrum
import io.github.riiimc.nostrum.content.recipes.alchemy.AlchemyRecipe
import net.minecraft.resources.ResourceLocation
import kotlin.math.roundToInt
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class AlchemyEmiRecipe(
    private val id: ResourceLocation,
    recipe: AlchemyRecipe
) : EmiRecipe {

    private val input: List<EmiIngredient> =
        recipe.inputItems.map { EmiIngredient.of(it) }

    private val fluid: EmiStack =
        EmiStack.of(recipe.inputFluid.fluid)
    private val output: List<EmiStack> =
        listOf(EmiStack.of(recipe.result))

    override fun getCategory(): EmiRecipeCategory {
        return NostrumEmiPlugin.MY_CATEGORY
    }

    override fun getId(): ResourceLocation {
        return id
    }

    override fun getInputs(): List<EmiIngredient> {
        return input
    }

    override fun getOutputs(): List<EmiStack> {
        return output
    }

    override fun getDisplayWidth(): Int {
        return when {
            //input.size <= 8 -> 76
            //input.size <= 12 -> 88
            else -> 100
        }
    }

    override fun getDisplayHeight(): Int {
        return when {
            //input.size <= 8 -> 40
            //input.size <= 12 -> 48
            else -> 120
        }
    }

    override fun addWidgets(widgets: WidgetHolder) {
        val width = displayWidth
        val height = displayHeight

        // Item circle center
        val centerX = width / 2.0
        val centerY = (height - 20) / 2.0

        // Output
        widgets.addSlot(
            output[0],
            centerX.roundToInt() - 8,
            centerY.roundToInt() - 8
        ).recipeContext(this)

        // Item inputs
        if (input.isNotEmpty()) {
            val radiusX = (width - 16) / 2.0 - 2.0
            val radiusY = (height - 40) / 2.0 - 2.0
            val radius = min(radiusX, radiusY)

            for (i in input.indices) {
                val angle =
                    -Math.PI / 2.0 +
                            Math.PI * 2.0 * i / input.size

                val x =
                    (centerX + cos(angle) * radius)
                        .roundToInt() - 8

                val y =
                    (centerY + sin(angle) * radius)
                        .roundToInt() - 8

                widgets.addSlot(
                    input[i],
                    x,
                    y
                )
            }
        }

        // Fluid input
        widgets.addSlot(
            fluid,
            centerX.roundToInt() - 8,
            height - 18
        )
    }
}