package io.github.riiimc.nostrum.utils

import io.github.riiimc.nostrum.Nostrum
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey

object NostrumTags {
    val BREWING_MATERIAL = TagKey.create(Registries.ITEM, Nostrum.rl("brewing_material"))
    val STRONG_1 = TagKey.create(Registries.ITEM, Nostrum.rl("strong_1"))
}