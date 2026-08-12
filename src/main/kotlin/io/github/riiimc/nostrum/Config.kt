package io.github.riiimc.nostrum

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.neoforge.common.ModConfigSpec
import net.neoforged.neoforge.common.ModConfigSpec.IntValue
import java.util.function.Predicate

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = Nostrum.MODID, bus = EventBusSubscriber.Bus.MOD)
object Config {
    private val BUILDER = ModConfigSpec.Builder()

    private val LOG_DIRT_BLOCK: ModConfigSpec.BooleanValue =
        BUILDER.comment("Whether to log the dirt block on common setup").define("logDirtBlock", true)

    private val MAGIC_NUMBER: IntValue =
        BUILDER.comment("A magic number").defineInRange("magicNumber", 42, 0, Int.MAX_VALUE)

    val MAGIC_NUMBER_INTRODUCTION: ModConfigSpec.ConfigValue<String?> =
        BUILDER.comment("What you want the introduction message to be for the magic number")
            .define<String?>("magicNumberIntroduction", "The magic number is... ")

    // a list of strings that are treated as resource locations for items
    private val ITEM_STRINGS: ModConfigSpec.ConfigValue<MutableList<out String?>?> =
        BUILDER.comment("A list of items to log on common setup.").defineListAllowEmpty<String?>(
            "items",
            mutableListOf<String?>("minecraft:iron_ingot"),
            Predicate { obj: Any? -> Config.validateItemName(obj) })

    val SPEC: ModConfigSpec = BUILDER.build()

    var logDirtBlock: Boolean = false
    var magicNumber: Int = 0
    var magicNumberIntroduction: String? = null
    var items: MutableSet<Item?>? = null

    private fun validateItemName(obj: Any?): Boolean {
        return obj is String && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(obj))
    }

    @SubscribeEvent
    @JvmStatic
    fun onLoad(event: ModConfigEvent?) {
        logDirtBlock = LOG_DIRT_BLOCK.get()
        magicNumber = MAGIC_NUMBER.get()
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get()
    }
}
