package io.github.riiimc.nostrum.compat.ageratum

import com.google.common.base.Supplier
import io.github.riiimc.nostrum.compat.NostrumCompat.isModLoaded
import io.github.riiimc.nostrum.compat.ageratum.items.GuideBookItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister

object NostrumAgeratumRegistries {
    val ITEMS = DeferredRegister.createItems("nostrum")
    val GUIDE_BOOK = ITEMS.register("guide_book", Supplier {
        GuideBookItem(Item.Properties().stacksTo(1).rarity(Rarity.RARE))
    })
    fun registry(bus: IEventBus) {
        if (isModLoaded("ageratum")) { // isModLoaded("ageratum")
            ITEMS.register(bus)
        }
    }
}