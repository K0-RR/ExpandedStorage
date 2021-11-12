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
package ninjaphenix.expandedstorage.tier;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.function.UnaryOperator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

@Internal
@Experimental
@SuppressWarnings("ClassCanBeRecord")
public class Tier {
    private final ResourceLocation id;
    private final UnaryOperator<Item.Properties> itemSettings;
    private final UnaryOperator<BlockBehaviour.Properties> blockSettings;
    private final int slots;

    public Tier(ResourceLocation id, int slots, UnaryOperator<BlockBehaviour.Properties> blockSettings, UnaryOperator<Item.Properties> itemSettings) {
        this.id = id;
        this.slots = slots;
        this.blockSettings = blockSettings;
        this.itemSettings = itemSettings;
    }

    public final ResourceLocation getId() {
        return id;
    }

    public final UnaryOperator<Item.Properties> getItemSettings() {
        return itemSettings;
    }

    public UnaryOperator<BlockBehaviour.Properties> getBlockSettings() {
        return blockSettings;
    }

    public final int getSlotCount() {
        return slots;
    }
}


