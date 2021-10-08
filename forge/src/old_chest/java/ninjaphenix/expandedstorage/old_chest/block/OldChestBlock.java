package ninjaphenix.expandedstorage.old_chest.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import ninjaphenix.expandedstorage.internal_api.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.old_chest.OldChestCommon;
import ninjaphenix.expandedstorage.old_chest.block.misc.OldChestBlockEntity;
import org.jetbrains.annotations.NotNull;

public final class OldChestBlock extends AbstractChestBlock<OldChestBlockEntity> {
    public OldChestBlock(Properties properties, ResourceLocation blockId, ResourceLocation blockTier,
                         ResourceLocation openingStat, int slots) {
        super(properties, blockId, blockTier, openingStat, slots);
    }

    @Override
    protected BlockEntityType<OldChestBlockEntity> getBlockEntityType() {
        return OldChestCommon.getBlockEntityType();
    }

    @Override
    public ResourceLocation getBlockType() {
        return OldChestCommon.BLOCK_TYPE;
    }

    @NotNull
    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new OldChestBlockEntity(OldChestCommon.getBlockEntityType(), this.getBlockId());
    }
}
