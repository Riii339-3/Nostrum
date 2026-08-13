package io.github.riiimc.nostrum.utils

import io.github.riiimc.nostrum.Nostrum
import io.github.riiimc.nostrum.Nostrum.Companion.rl
import net.minecraft.core.registries.Registries
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey

object NostrumTags {
    val BREWING_MATERIAL = TagKey.create(Registries.ITEM, rl("brewing_material"))
    val STRONG_1 = TagKey.create(Registries.ITEM, rl("strong_1"))
    val SPLASH = TagKey.create(Registries.ITEM, Nostrum.rl("potion/splash"))
    val LINGERING = TagKey.create(Registries.ITEM, Nostrum.rl("potion/lingering"))
    val AEROSOL = TagKey.create(Registries.ITEM, Nostrum.rl("potion/aerosol"))
    val SPRAY = TagKey.create(Registries.ITEM, Nostrum.rl("potion/spray"))
    val MIXING_MATERIAL = TagKey.create(Registries.ITEM, Nostrum.rl("mixing_material"))
    val PHILOSOPHERS_STONE_BLACKLIST =
        ItemTags.create(
            rl("philosopher_stone_blacklist")
        )
    val EPILOGUE_SHADER_ITEM = TagKey.create(Registries.ITEM, Nostrum.rl("shader_items/epilogue"))
    val EPILOGUE_MOVE_SHADER = TagKey.create(Registries.ITEM, Nostrum.rl("shader_items/epilogue_move"))
    val EPILOGUE_TOOLTIP_SHADER = TagKey.create(Registries.ITEM, Nostrum.rl("shader_items/epilogue_tooltip"))
    val ALCHEMICAL_SHADER_ITEM = TagKey.create(Registries.ITEM, Nostrum.rl("shader_items/alchemical"))
    val ALCHEMICAL_TOOLTIP_SHADER = TagKey.create(Registries.ITEM, Nostrum.rl("shader_items/alchemical_tooltip"))
}