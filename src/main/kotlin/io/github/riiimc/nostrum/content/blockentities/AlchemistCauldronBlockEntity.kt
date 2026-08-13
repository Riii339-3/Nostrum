package io.github.riiimc.nostrum.content.blockentities

import io.github.riiimc.nostrum.NostrumRegistries
import io.github.riiimc.nostrum.content.components.AlchemicalPotionForm
import io.github.riiimc.nostrum.content.recipes.AlchemistCauldronMode
import io.github.riiimc.nostrum.content.recipes.alchemy.AlchemyRecipe
import io.github.riiimc.nostrum.content.recipes.alchemy.AlchemyRecipeInput
import io.github.riiimc.nostrum.utils.ResizeStackHandler
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.fluids.FluidStack

class AlchemistCauldronBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(
    NostrumRegistries.ALCHEMIST_CAULDRON_BE_TYPE.get(),
    pos,
    state
) {

    companion object {
        private const val TAG_MODE = "Mode"
        private const val TAG_INVENTORY = "Inventory"
        private const val TAG_FLUID = "Fluid"
        private const val TAG_OUTPUT_FLUID = "OutputFluid"
        private const val TAG_POTION_DATA = "PotionData"
        private const val TAG_POTION_DATA_2 = "PotionData2"

        private const val TAG_EFFECTS = "Effects"
        private const val TAG_REMAINING = "Remaining"
        private const val TAG_FORM = "Form"
    }

    var mode: AlchemistCauldronMode = AlchemistCauldronMode.ALCHEMY

    val inventory = ResizeStackHandler(0)

    var fluid: FluidStack = FluidStack.EMPTY

    var outputFluid: FluidStack = FluidStack.EMPTY

    var potionData: PotionData? = null

    var potionData2: PotionData? = null

    override fun saveAdditional(
        tag: CompoundTag,
        provider: HolderLookup.Provider
    ) {
        super.saveAdditional(tag, provider)

        // ============================================================
        // Mode
        // ============================================================

        tag.putString(
            TAG_MODE,
            mode.name
        )

        // ============================================================
        // Inventory
        // ============================================================

        tag.put(
            TAG_INVENTORY,
            inventory.serializeNBT(provider)
        )

        // ============================================================
        // Fluid
        // ============================================================

        if (!fluid.isEmpty) {
            tag.put(
                TAG_FLUID,
                fluid.save(provider)
            )
        }

        // ============================================================
        // Output Fluid
        // ============================================================

        if (!outputFluid.isEmpty) {
            tag.put(
                TAG_OUTPUT_FLUID,
                outputFluid.save(provider)
            )
        }

        // ============================================================
        // Potion Data
        // ============================================================

        potionData?.let { data ->
            tag.put(
                TAG_POTION_DATA,
                savePotionData(data, provider)
            )
        }

        potionData2?.let { data ->
            tag.put(
                TAG_POTION_DATA_2,
                savePotionData(data, provider)
            )
        }
    }

    override fun loadAdditional(
        tag: CompoundTag,
        provider: HolderLookup.Provider
    ) {
        super.loadAdditional(tag, provider)

        // ============================================================
        // Mode
        // ============================================================

        mode = readMode(
            tag.getString(TAG_MODE)
        )

        // ============================================================
        // Inventory
        // ============================================================

        if (tag.contains(TAG_INVENTORY, Tag.TAG_COMPOUND.toInt())) {
            inventory.deserializeNBT(
                provider,
                tag.getCompound(TAG_INVENTORY)
            )
        }

        // ============================================================
        // Fluid
        // ============================================================

        fluid = if (
            tag.contains(TAG_FLUID, Tag.TAG_COMPOUND.toInt())
        ) {
            FluidStack.parse(
                provider,
                tag.getCompound(TAG_FLUID)
            ).orElse(FluidStack.EMPTY)
        } else {
            FluidStack.EMPTY
        }

        // ============================================================
        // Output Fluid
        // ============================================================

        outputFluid = if (
            tag.contains(TAG_OUTPUT_FLUID, Tag.TAG_COMPOUND.toInt())
        ) {
            FluidStack.parse(
                provider,
                tag.getCompound(TAG_OUTPUT_FLUID)
            ).orElse(FluidStack.EMPTY)
        } else {
            FluidStack.EMPTY
        }

        // ============================================================
        // Potion Data
        // ============================================================

        potionData = if (
            tag.contains(TAG_POTION_DATA, Tag.TAG_COMPOUND.toInt())
        ) {
            loadPotionData(
                tag.getCompound(TAG_POTION_DATA)
            )
        } else {
            null
        }

        // ============================================================
        // Potion Data 2
        // ============================================================

        potionData2 = if (
            tag.contains(TAG_POTION_DATA_2, Tag.TAG_COMPOUND.toInt())
        ) {
            loadPotionData(
                tag.getCompound(TAG_POTION_DATA_2)
            )
        } else {
            null
        }
    }

    /**
     * PotionData を NBT に保存する。
     */
    private fun savePotionData(
        data: PotionData,
        provider: HolderLookup.Provider
    ): CompoundTag {
        val potionTag = CompoundTag()

        // ------------------------------------------------------------
        // Effects
        // ------------------------------------------------------------

        val effectsTag = ListTag()

        for (effect in data.effects) {
            MobEffectInstance.CODEC
                .encodeStart(
                    NbtOps.INSTANCE,
                    effect
                )
                .result()
                .ifPresent { encoded ->
                    effectsTag.add(encoded)
                }
        }

        potionTag.put(
            TAG_EFFECTS,
            effectsTag
        )

        // ------------------------------------------------------------
        // Remaining
        // ------------------------------------------------------------

        potionTag.putInt(
            TAG_REMAINING,
            data.remaining
        )

        // ------------------------------------------------------------
        // Form
        // ------------------------------------------------------------

        potionTag.putString(
            TAG_FORM,
            data.form.name
        )

        return potionTag
    }

    /**
     * PotionData を NBT から読み込む。
     *
     * 壊れた Effect が存在していても、読み込める Effect は
     * 可能な限り復元する。
     */
    private fun loadPotionData(
        potionTag: CompoundTag
    ): PotionData {
        val effects = mutableListOf<MobEffectInstance>()

        // ------------------------------------------------------------
        // Effects
        // ------------------------------------------------------------

        if (
            potionTag.contains(
                TAG_EFFECTS,
                Tag.TAG_LIST.toInt()
            )
        ) {
            val effectsTag = potionTag.getList(
                TAG_EFFECTS,
                Tag.TAG_COMPOUND.toInt()
            )

            for (element in effectsTag) {
                MobEffectInstance.CODEC
                    .parse(
                        NbtOps.INSTANCE,
                        element
                    )
                    .result()
                    .ifPresent { effect ->
                        effects.add(effect)
                    }
            }
        }

        // ------------------------------------------------------------
        // Remaining
        // ------------------------------------------------------------

        val remaining = potionTag.getInt(
            TAG_REMAINING
        )

        // ------------------------------------------------------------
        // Form
        // ------------------------------------------------------------

        val form = readPotionForm(
            potionTag.getString(TAG_FORM)
        )

        return PotionData(
            effects,
            remaining,
            form
        )
    }

    /**
     * AlchemistCauldronMode を安全に読み込む。
     */
    private fun readMode(
        name: String
    ): AlchemistCauldronMode {
        if (name.isBlank()) {
            return AlchemistCauldronMode.ALCHEMY
        }

        return try {
            AlchemistCauldronMode.valueOf(name)
        } catch (_: IllegalArgumentException) {
            AlchemistCauldronMode.ALCHEMY
        }
    }

    /**
     * AlchemicalPotionForm を安全に読み込む。
     */
    private fun readPotionForm(
        name: String
    ): AlchemicalPotionForm {
        if (name.isBlank()) {
            return AlchemicalPotionForm.DRINK
        }

        return try {
            AlchemicalPotionForm.valueOf(name)
        } catch (_: IllegalArgumentException) {
            AlchemicalPotionForm.DRINK
        }
    }

    /**
     * 現在の釜の状態からレシピ入力を作成する。
     */
    fun createRecipeInput(): AlchemyRecipeInput {
        val stacks = NonNullList.withSize(
            inventory.slots,
            ItemStack.EMPTY
        )

        for (slot in 0 until inventory.slots) {
            stacks[slot] = inventory.getStackInSlot(slot)
        }

        return AlchemyRecipeInput(
            blockState,
            stacks,
            fluid
        )
    }

    /**
     * 現在の状態に一致する錬金レシピを取得する。
     */
    fun checkRecipe(): AlchemyRecipe? {
        val level = level ?: return null

        return level.recipeManager
            .getRecipeFor(
                NostrumRegistries.ALCHEMY_RECIPE.get(),
                createRecipeInput(),
                level
            )
            .map { holder ->
                holder.value
            }
            .orElse(null)
    }
}