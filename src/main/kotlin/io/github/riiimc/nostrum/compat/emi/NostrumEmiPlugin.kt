package io.github.riiimc.nostrum.compat.emi

import dev.emi.emi.api.EmiEntrypoint
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiStack
import io.github.riiimc.nostrum.Nostrum
import io.github.riiimc.nostrum.NostrumRegistries
import io.github.riiimc.nostrum.content.recipes.alchemy.AlchemyRecipe


@EmiEntrypoint
class NostrumEmiPlugin : EmiPlugin {

    companion object {
        @JvmField
        val MY_SPRITE_SHEET = Nostrum.rl("textures/gui/emi_simplified_textures.png")

        @JvmField
        val MY_WORKSTATION = EmiStack.of(NostrumRegistries.ALCHEMIST_CAULDRON_ITEM)

        @JvmField
        val MY_CATEGORY = EmiRecipeCategory(
            Nostrum.rl("alchemy"),
            MY_WORKSTATION,
            EmiTexture(MY_SPRITE_SHEET, 0, 0, 16, 16)
        )
    }

    override fun register(registry: EmiRegistry) {
        registry.addCategory(MY_CATEGORY)
        registry.addWorkstation(MY_CATEGORY, MY_WORKSTATION)

        val manager = registry.recipeManager

        for (holder in manager.recipes) {
            val recipe = holder.value()

            if (recipe !is AlchemyRecipe) continue

            registry.addRecipe(
                AlchemyEmiRecipe(holder.id(), recipe)
            )
        }
    }
}