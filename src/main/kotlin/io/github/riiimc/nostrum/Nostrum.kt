package io.github.riiimc.nostrum

import com.mojang.logging.LogUtils
import io.github.riiimc.nostrum.client.AlchemicalPotionItemColor
import io.github.riiimc.nostrum.client.NostrumShaders
import io.github.riiimc.nostrum.content.upgrade.UpgradeManage
import io.github.riiimc.nostrum.content.upgrade.custom.inversion.AttributeSwapHandler.reset
import io.github.riiimc.nostrum.content.upgrade.custom.inversion.AttributeSwapHandler.swap
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.ThrownItemRenderer
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.block.Blocks
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent
import net.neoforged.neoforge.client.event.RegisterShadersEvent
import net.neoforged.neoforge.client.event.RenderFrameEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.AddReloadListenerEvent
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent
import org.joml.Vector3f
import org.slf4j.Logger
import java.util.function.Consumer

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Nostrum.MODID)
class Nostrum(modEventBus: IEventBus, modContainer: ModContainer) {
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    init {
        // Register the commonSetup method for modloading
        modEventBus.addListener<FMLCommonSetupEvent?>(Consumer { event: FMLCommonSetupEvent? -> this.commonSetup(event) })

        NostrumRegistries.registry(modEventBus)
        // Register the Deferred Register to the mod event bus so blocks get registered

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Nostrum) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this)


        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)
    }

    private fun commonSetup(event: FMLCommonSetupEvent?) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP")

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT))

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber)

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent?) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting")
    }
    // あほなのでResourceManagerをModクラスに置く図
    @SubscribeEvent
    fun onAddReloadListener(event: AddReloadListenerEvent) {
        event.addListener(UpgradeManage.instance)
    }


    @SubscribeEvent
    fun onEntityTick(event: EntityTickEvent.Post) {
        val entity = event.entity

        if (entity.level().isClientSide)
            return

        if (entity !is LivingEntity)
            return

        val enabled = entity.armorSlots.any { stack ->
            stack.get(NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT)
                ?.id == ResourceLocation.fromNamespaceAndPath(
                "nostrum",
                "inversion"
            )
        }

        if (enabled) {
            swap(entity)
        } else {
            reset(entity)
        }
    }


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
    object ClientModEvents {
        @SubscribeEvent
        @JvmStatic
        fun onClientSetup(event: FMLClientSetupEvent?) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP")
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)
        }
        @SubscribeEvent
        @JvmStatic
        fun registerEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
            event.registerEntityRenderer(
                NostrumRegistries.THROWN_ALCHEMICAL_POTION.get(),
                ::ThrownItemRenderer
            )
        }
        @JvmStatic
        @SubscribeEvent
        fun registerItemColors(event: RegisterColorHandlersEvent.Item) {
            event.register(
                AlchemicalPotionItemColor(),
                NostrumRegistries.ALCHEMICAL_POTION.get()
            )
        }

        @SubscribeEvent
        @JvmStatic
        fun registerShaders(event: RegisterShadersEvent) {
            NostrumShaders.registerShaders(event)
        }


        @JvmStatic
        @SubscribeEvent
        fun onItemTooltip(event: ItemTooltipEvent) {
            val stack = event.itemStack

            val component = stack.get(
                NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT
            ) ?: return

            /*
            val name = stack.hoverName.copy()

            name.withStyle { style ->
                style
                    .withFont(Nostrum.rl("alchemical"))
            }

            event.toolTip[0] = name

             */


            /*

            event.toolTip.add(1,
                Component.translatable(
                    "nostrum.tooltip.alchemical_upgrade"
                ).withStyle { style ->
                    style
                        .withFont(ResourceLocation.fromNamespaceAndPath("nostrum", "alchemical"))
                        .withBold(true)
                }
            )

             */
            event.toolTip.add(1,
                Component.translatable(
                    "nostrum.tooltip.alchemical_upgrade",
                    Component.translatable("nostrum.tooltip.alchemical_upgrade.upgrade.${component.id.namespace}.${component.id.path}")
                ).withStyle { style ->
                    style
                        .withFont(ResourceLocation.fromNamespaceAndPath("nostrum", "alchemical"))
                        .withBold(true)
                }
            )
            val description = Component.translatable(
                "nostrum.tooltip.alchemical_upgrade.upgrade.${component.id.namespace}.${component.id.path}.description"
            ).withStyle { style ->
                style
                    .withFont(ResourceLocation.fromNamespaceAndPath("nostrum", "alchemical"))
                    .withBold(true)
            }

            description.string.split("\n").forEachIndexed { index, line ->
                event.toolTip.add(
                    2 + index,
                    Component.literal(line).withStyle(description.style)
                )
            }
        }

        @SubscribeEvent
        @JvmStatic
        fun onRenderTick(event: RenderFrameEvent.Pre) {
            val minecraft = Minecraft.getInstance()
            val level = minecraft.level ?: return

            val time =
                level.gameTime.toFloat() +
                        minecraft.timer.getGameTimeDeltaPartialTick(false)

            NostrumShaders.updateAlchemical(
                time / 20.0f,
                0.5f,
                1.5f,
                Vector3f(0.05f, 0.35f, 1.0f),
                Vector3f(0.1f, 1.0f, 0.45f)
            )
            NostrumShaders.updatePhilosopherStone(
                time / 20.0f,
                0.5f,
                1.5f,
                Vector3f(0.025f, 0.08f, 0.38f),
                Vector3f(0.32f, 0.58f, 1.0f)
            )
            NostrumShaders.updateEpilogueMove(
                time / 20.0f,
                0.5f,
                1.5f,
                Vector3f(0.025f, 0.08f, 0.38f),
                Vector3f(0.32f, 0.58f, 1.0f))

            /*
            val now = java.time.LocalTime.now()
            val localSeconds = now.toSecondOfDay()

            println(
                "REAL=${now} localSeconds=$localSeconds"
            )

            NostrumShaders.updateTooltipTime(localSeconds.toFloat())

             */
        }
    }



    companion object {
        @JvmStatic
        fun rl(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MODID, path)

        // Define mod id in a common place for everything to reference
        const val MODID: String = "nostrum"

        // Directly reference a slf4j logger
        val LOGGER: Logger = LogUtils.getLogger()


        // Create a Deferred Register to hold Blocks which will all be registered under the "nostrum" namespace

    }
}
