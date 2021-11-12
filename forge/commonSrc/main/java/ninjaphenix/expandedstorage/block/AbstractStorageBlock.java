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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Experimental
public abstract class AbstractStorageBlock extends Block {
    private final ResourceLocation blockId;
    private final ResourceLocation blockTier;

    public AbstractStorageBlock(Properties settings, ResourceLocation blockId, ResourceLocation blockTier) {
        super(settings);
        this.blockId = blockId;
        this.blockTier = blockTier;
    }

    public abstract ResourceLocation getBlockType();

    public final ResourceLocation getBlockId() {
        return blockId;
    }

    public final ResourceLocation getBlockTier() {
        return blockTier;
    }
}
