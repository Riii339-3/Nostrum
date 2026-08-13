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
object NostrumConfig {
    private val BUILDER_COMMON = ModConfigSpec.Builder()
    private val BUILDER_CLIENT = ModConfigSpec.Builder()

    @JvmStatic
    val UTC: IntValue =
        BUILDER_CLIENT.comment("Your UTC").defineInRange("magicNumber", 0, -12, 14)

    @JvmStatic
    val USE_SHADER_TEXT: ModConfigSpec.BooleanValue =
        BUILDER_CLIENT.comment("Whether to use shader text").define("useShaderText", true)

    @JvmStatic
    val USE_SHADER_TOOLTIP: ModConfigSpec.BooleanValue =
        BUILDER_CLIENT.comment("Whether to use shader tooltip").define("useShaderTooltip", true)

    @JvmStatic
    val MAX_POTION_LEVEL =
        BUILDER_COMMON.comment("Max potion level in mixing").defineInRange("maxPotionLevel", 5, 0, 255)

    @JvmStatic
    val MAX_POTION_UPGRADE_AMOUNT =
        BUILDER_COMMON.comment("Max potion upgrade amount").defineInRange("maxPotionUpgradeAmount", 3, 1, Int.MAX_VALUE)

    val MAGIC_NUMBER_INTRODUCTION: ModConfigSpec.ConfigValue<String?> =
        BUILDER_COMMON.comment("What you want the introduction message to be for the magic number")
            .define<String?>("magicNumberIntroduction", "The magic number is... ")

    // a list of strings that are treated as resource locations for items
    private val ITEM_STRINGS: ModConfigSpec.ConfigValue<MutableList<out String?>?> =
        BUILDER_COMMON.comment("A list of items to log on common setup.").defineListAllowEmpty<String?>(
            "items",
            mutableListOf<String?>("minecraft:iron_ingot"),
            Predicate { obj: Any? -> NostrumConfig.validateItemName(obj) })

    val SPEC: ModConfigSpec = BUILDER_COMMON.build()
    val SPEC_CLIENT: ModConfigSpec = BUILDER_CLIENT.build()

    var utc: Int = 0
    var useShaderText: Boolean = true
    var useShaderTooltip: Boolean = true
    var maxPotionLevel: Int = 5
    var maxPotionUpgradeAmount: Int = 3
    var magicNumberIntroduction: String? = null
    var items: MutableSet<Item?>? = null

    private fun validateItemName(obj: Any?): Boolean {
        return obj is String && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(obj))
    }

    @SubscribeEvent
    @JvmStatic
    fun onLoad(event: ModConfigEvent) {
        when (event.config.spec) {
            SPEC -> {
                maxPotionLevel = MAX_POTION_LEVEL.get()
                maxPotionUpgradeAmount = MAX_POTION_UPGRADE_AMOUNT.get()
            }

            SPEC_CLIENT -> {
                utc = UTC.get()
                useShaderText = USE_SHADER_TEXT.get()
                useShaderTooltip = USE_SHADER_TOOLTIP.get()
            }
        }
    }
}
