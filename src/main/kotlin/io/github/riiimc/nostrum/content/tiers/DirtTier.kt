package io.github.riiimc.nostrum.content.tiers

import net.minecraft.tags.TagKey
import net.minecraft.world.item.Items
import net.minecraft.world.item.Tier
import net.minecraft.world.item.Tiers
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Block

object DirtTier : Tier {
    override fun getUses() = Tiers.IRON.uses
    override fun getSpeed() = Tiers.IRON.speed
    override fun getAttackDamageBonus() = Tiers.IRON.attackDamageBonus
    override fun getIncorrectBlocksForDrops(): TagKey<Block?> {
        return Tiers.IRON.incorrectBlocksForDrops
    }
    override fun getEnchantmentValue() = Tiers.IRON.enchantmentValue
    override fun getRepairIngredient() = Ingredient.of(Items.DIRT)
}