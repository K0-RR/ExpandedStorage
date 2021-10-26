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
package ninjaphenix.expandedstorage.block.misc.strategies.temp_be.extendable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ninjaphenix.expandedstorage.block.misc.strategies.ItemAccess;
import ninjaphenix.expandedstorage.block.misc.strategies.Lockable;
import ninjaphenix.expandedstorage.block.misc.strategies.Nameable;
import ninjaphenix.expandedstorage.block.misc.strategies.Observable;

public abstract class StrategyBlockEntity extends BlockEntity {
    private final Identifier blockId;
    private ItemAccess itemAccess;
    private Lockable lockable;
    private Nameable nameable;
    private Observable observable;

    public StrategyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Identifier blockId) {
        super(type, pos, state);
        this.blockId = blockId;
        this.initialise(blockId, state.getBlock());
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        lockable.readLock(tag);
        nameable.readName(tag);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        lockable.writeLock(tag);
        nameable.writeName(tag);
        return tag;
    }

    protected void initialise(Identifier blockId, Block block) {

    }

    public final Identifier getBlockId() {
        return blockId;
    }

    protected void setItemAccess(ItemAccess itemAccess) {
        if (this.itemAccess == null) this.itemAccess = itemAccess;
    }

    public ItemAccess getItemAccess() {
        return itemAccess;
    }

    protected void setLock(Lockable lockable) {
        if (this.lockable == null) this.lockable = lockable;
    }

    public Lockable getLock() {
        return lockable;
    }

    protected void setName(Nameable nameable) {
        if (this.nameable == null) this.nameable = nameable;
    }

    public Nameable getName() {
        return nameable;
    }

    protected void setObservable(Observable observable) {
        if (this.observable == null) this.observable = observable;
    }

    public Observable getObservable() {
        return observable;
    }
}
