/**
 * Copyright 2021 NinjaPhenix
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ninjaphenix.expandedstorage.block;

import ninjaphenix.container_library.api.v2.OpenableBlockEntityProviderV2;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Internal
@Experimental
public abstract class AbstractOpenableStorageBlock extends AbstractStorageBlock implements EntityBlock, OpenableBlockEntityProviderV2 {
    private final ResourceLocation openingStat;
    private final int slots;

    public AbstractOpenableStorageBlock(BlockBehaviour.Properties settings, ResourceLocation blockId, ResourceLocation blockTier,
                                        ResourceLocation openingStat, int slots) {
        super(settings, blockId, blockTier);
        this.openingStat = openingStat;
        this.slots = slots;
    }

    public final int getSlotCount() {
        return slots;
    }

    public final Component getInventoryTitle() {
        return new TranslatableComponent(this.getDescriptionId());
    }

    @Override
    @SuppressWarnings("deprecation")
    public final InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return this.ncl_onBlockUse(world, state, pos, player, hand, hit);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter view, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, view, tooltip, flag);
        tooltip.add(new TranslatableComponent("tooltip.expandedstorage.stores_x_stacks", slots).withStyle(ChatFormatting.GRAY));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean bl) {
        if (!state.is(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof AbstractOpenableStorageBlockEntity entity) {
                Containers.dropContents(world, pos, entity.getItems());
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, bl);
        }
    }

    public void onInitialOpen(ServerPlayer player) {
        player.awardStat(openingStat);
        PiglinAi.angerNearbyPiglins(player, true);
    }

    // todo: move, should check for AbstractNameableAccessibleStorageBlockEntity
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof AbstractOpenableStorageBlockEntity entity && stack.hasCustomHoverName()) {
            entity.setTitle(stack.getHoverName());
        }
    }
}
