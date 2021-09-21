package ninjaphenix.expandedstorage.base.internal_api.block;

import ninjaphenix.container_library.api.OpenableBlockEntityProvider;
import ninjaphenix.container_library.api.client.NCL_ClientApi;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
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
public abstract class AbstractOpenableStorageBlock extends AbstractStorageBlock implements EntityBlock, OpenableBlockEntityProvider {
    private final ResourceLocation openingStat;
    private final int slots;

    public AbstractOpenableStorageBlock(BlockBehaviour.Properties properties, ResourceLocation blockId, ResourceLocation blockTier,
                                        ResourceLocation openingStat, int slots) {
        super(properties, blockId, blockTier);
        this.openingStat = openingStat;
        this.slots = slots;
    }

    public final int getSlotCount() {
        return slots;
    }

    public final Component getMenuTitle() {
        return new TranslatableComponent(this.getDescriptionId());
    }

    @Override
    @SuppressWarnings("deprecation")
    public final InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            NCL_ClientApi.openInventoryAt(pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter getter, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, getter, tooltip, flag);
        tooltip.add(new TranslatableComponent("tooltip.expandedstorage.stores_x_stacks", slots).withStyle(ChatFormatting.GRAY));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean bl) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof AbstractOpenableStorageBlockEntity entity) {
                Containers.dropContents(level, pos, entity.getItems());
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, bl);
        }
    }

    public void onInitialOpen(ServerPlayer player) {
        player.awardStat(openingStat);
    }
}
