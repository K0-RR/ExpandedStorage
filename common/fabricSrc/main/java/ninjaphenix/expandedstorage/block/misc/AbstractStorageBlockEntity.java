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
package ninjaphenix.expandedstorage.block.misc;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Experimental
public abstract class AbstractStorageBlockEntity extends BlockEntity {
    private ContainerLock lockKey;
    private Text title;

    public AbstractStorageBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        lockKey = ContainerLock.EMPTY;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        lockKey = ContainerLock.fromNbt(tag);
        if (tag.contains("CustomName", NbtElement.STRING_TYPE)) {
            title = Text.Serializer.fromJson(tag.getString("CustomName"));
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        lockKey.writeNbt(tag);
        if (title != null) {
            tag.putString("CustomName", Text.Serializer.toJson(title));
        }
        return tag;
    }

    public boolean usableBy(ServerPlayerEntity player) {
        return lockKey == ContainerLock.EMPTY || !player.isSpectator() && lockKey.canOpen(player.getMainHandStack());
    }

    public final Text getTitle() {
        return this.hasCustomTitle() ? title : this.getDefaultTitle();
    }

    protected abstract Text getDefaultTitle();

    public final boolean hasCustomTitle() {
        return title != null;
    }

    public final void setTitle(Text title) {
        this.title = title;
    }
}
