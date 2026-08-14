package io.github.riiimc.nostrum.events

import io.github.riiimc.nostrum.Nostrum
import io.github.riiimc.nostrum.NostrumRegistries
import io.github.riiimc.nostrum.content.components.AlchemicalUpgradeComponent
import io.github.riiimc.nostrum.content.upgrade.AttributeData
import io.github.riiimc.nostrum.content.upgrade.UpgradeManage
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.ItemAttributeModifierEvent
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent

@EventBusSubscriber(modid = Nostrum.MODID, bus = EventBusSubscriber.Bus.GAME)
object NostrumEvents {
    @JvmStatic
    @SubscribeEvent
    fun onAttributeModifierEvent(event: ItemAttributeModifierEvent) {
        val item = event.itemStack
        val component: AlchemicalUpgradeComponent = item.get(NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT.get()) ?: return
        val upgradeId = component.id
        val upgrade =
            UpgradeManage.instance.get(upgradeId)
                ?: run {
                    Nostrum.LOGGER.warn(
                        "Unknown alchemical upgrade: {}",
                        upgradeId
                    )

                    return
                }
        if (upgrade.attributes == emptyList<AttributeData>()) return
        var i = 0
        upgrade.attributes.forEach { (attribute, operation, equipmentSlot, amount) ->
            event.addModifier(attribute, AttributeModifier(Nostrum.rl("upgrades/${upgradeId.namespace}.${upgradeId.path}.${i}"), amount, operation), equipmentSlot)
            i++
        }
    }
}