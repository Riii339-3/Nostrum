package io.github.riiimc.nostrum.content.recipes

enum class AlchemistCauldronMode {
    ALCHEMY,
    POTION;

    fun next(): AlchemistCauldronMode {
        val values = AlchemistCauldronMode.entries
        return values[(ordinal + 1) % values.size]
    }

}