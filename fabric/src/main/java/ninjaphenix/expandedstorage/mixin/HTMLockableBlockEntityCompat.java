/*
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
package ninjaphenix.expandedstorage.mixin;

import com.github.fabricservertools.htm.HTMContainerLock;
import com.github.fabricservertools.htm.api.LockableObject;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ninjaphenix.expandedstorage.block.entity.extendable.OpenableBlockEntity;
import ninjaphenix.expandedstorage.block.entity.extendable.StrategyBlockEntity;
import ninjaphenix.expandedstorage.compat.htm.HTMLockable;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("unused")
@Mixin(OpenableBlockEntity.class)
public abstract class HTMLockableBlockEntityCompat extends StrategyBlockEntity implements LockableObject {
    public HTMLockableBlockEntityCompat(BlockEntityType<?> type, BlockPos pos, BlockState state, Identifier blockId) {
        super(type, pos, state, blockId);
    }

    @Override
    public HTMContainerLock getLock() {
        return ((HTMLockable) this.getLockable()).getLock();
    }

    @Override
    public void setLock(HTMContainerLock lock) {
        ((HTMLockable) this.getLockable()).setLock(lock);
    }
}
