package io.github.riiimc.nostrum.content.upgrade.custom.inversion

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes

object AttributeSwapHandler {

    private val ATTACK_SWAP_ID =
        ResourceLocation.fromNamespaceAndPath(
            "nostrum",
            "attack_armor_swap_attack"
        )

    private val ARMOR_SWAP_ID =
        ResourceLocation.fromNamespaceAndPath(
            "nostrum",
            "attack_armor_swap_armor"
        )

    private var lastAttack = Double.NaN
    private var lastArmor = Double.NaN

    fun swap(entity: LivingEntity) {
        val attack = entity.getAttribute(Attributes.ATTACK_DAMAGE)
            ?: return

        val armor = entity.getAttribute(Attributes.ARMOR)
            ?: return

        val attackModifier = attack.getModifier(ATTACK_SWAP_ID)
        val armorModifier = armor.getModifier(ARMOR_SWAP_ID)

        val attackValue =
            attack.value - (attackModifier?.amount ?: 0.0)

        val armorValue =
            armor.value - (armorModifier?.amount ?: 0.0)

        val attackAmount = armorValue - attackValue
        val armorAmount = attackValue - armorValue

        // 既に正しいModifierが付いているなら何もしない
        if (
            attackModifier?.amount == attackAmount &&
            armorModifier?.amount == armorAmount
        ) {
            return
        }

        attack.removeModifier(ATTACK_SWAP_ID)
        armor.removeModifier(ARMOR_SWAP_ID)

        attack.addTransientModifier(
            AttributeModifier(
                ATTACK_SWAP_ID,
                attackAmount,
                AttributeModifier.Operation.ADD_VALUE
            )
        )

        armor.addTransientModifier(
            AttributeModifier(
                ARMOR_SWAP_ID,
                armorAmount,
                AttributeModifier.Operation.ADD_VALUE
            )
        )
    }
    fun reset(entity: LivingEntity) {
        entity.getAttribute(Attributes.ATTACK_DAMAGE)
            ?.removeModifier(ATTACK_SWAP_ID)

        entity.getAttribute(Attributes.ARMOR)
            ?.removeModifier(ARMOR_SWAP_ID)

        lastAttack = Double.NaN
        lastArmor = Double.NaN
    }
}