package ninjaphenix.expandedstorage.block.misc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ninjaphenix.expandedstorage.internal_api.block.misc.AbstractOpenableStorageBlockEntity;

public final class OldChestBlockEntity extends AbstractOpenableStorageBlockEntity {
    public OldChestBlockEntity(BlockEntityType<OldChestBlockEntity> blockEntityType, ResourceLocation blockId) {
        super(blockEntityType, blockId);
    }
}
