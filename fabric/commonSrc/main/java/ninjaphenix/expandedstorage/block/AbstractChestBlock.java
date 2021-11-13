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
package ninjaphenix.expandedstorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import ninjaphenix.container_library.api.helpers.VariableSidedInventory;
import ninjaphenix.container_library.api.v2.OpenableBlockEntityV2;
import ninjaphenix.container_library.api.v2.helpers.OpenableBlockEntitiesV2;
import ninjaphenix.expandedstorage.Common;
import ninjaphenix.expandedstorage.Utils;
import ninjaphenix.expandedstorage.block.entity.OldChestBlockEntity;
import ninjaphenix.expandedstorage.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.block.misc.Property;
import ninjaphenix.expandedstorage.block.misc.PropertyRetriever;
import ninjaphenix.expandedstorage.block.strategies.Nameable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;

/**
 * Note to self, do not rename, used by chest tracker.
 */
public class AbstractChestBlock extends OpenableBlock implements InventoryProvider {
    /**
     * Note to self, do not rename, used by chest tracker.
     */
    public static final EnumProperty<CursedChestType> CURSED_CHEST_TYPE = EnumProperty.of("type", CursedChestType.class);

    public AbstractChestBlock(Settings settings, Identifier blockId, Identifier blockTier, Identifier openingStat, int slotCount) {
        super(settings, blockId, blockTier, openingStat, slotCount);
        this.setDefaultState(this.getDefaultState()
                                 .with(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE)
                                 .with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    public static <T extends OldChestBlockEntity> PropertyRetriever<T> createPropertyRetriever(AbstractChestBlock block, BlockState state, WorldAccess world, BlockPos pos, boolean retrieveBlockedChests) {
        BiPredicate<WorldAccess, BlockPos> isChestBlocked = retrieveBlockedChests ? (_level, _pos) -> false : block::isAccessBlocked;
        return PropertyRetriever.create(block.getBlockEntityType(), AbstractChestBlock::getBlockType, AbstractChestBlock::getDirectionToAttached,
                (s) -> s.get(Properties.HORIZONTAL_FACING), state, world, pos, isChestBlocked);
    }

    protected boolean isAccessBlocked(WorldAccess world, BlockPos pos) {
        return false;
    }

    protected <T extends OldChestBlockEntity> BlockEntityType<T> getBlockEntityType() {
        //noinspection unchecked
        return (BlockEntityType<T>) Common.getOldChestBlockEntityType();
    }

    @Override
    public Identifier getBlockType() {
        return Common.OLD_CHEST_BLOCK_TYPE;
    }

    /**
     * Note to self, do not rename, used by chest tracker.
     */
    public static Direction getDirectionToAttached(BlockState state) {
        CursedChestType value = state.get(AbstractChestBlock.CURSED_CHEST_TYPE);
        if (value == CursedChestType.TOP) {
            return Direction.DOWN;
        } else if (value == CursedChestType.BACK) {
            return state.get(Properties.HORIZONTAL_FACING);
        } else if (value == CursedChestType.RIGHT) {
            return state.get(Properties.HORIZONTAL_FACING).rotateYClockwise();
        } else if (value == CursedChestType.BOTTOM) {
            return Direction.UP;
        } else if (value == CursedChestType.FRONT) {
            return state.get(Properties.HORIZONTAL_FACING).getOpposite();
        } else if (value == CursedChestType.LEFT) {
            return state.get(Properties.HORIZONTAL_FACING).rotateYCounterclockwise();
        } else if (value == CursedChestType.SINGLE) {
            throw new IllegalArgumentException("BaseChestBlock#getDirectionToAttached received an unexpected state.");
        }
        throw new IllegalArgumentException("Invalid CursedChestType passed.");
    }

    public static DoubleBlockProperties.Type getBlockType(BlockState state) {
        CursedChestType value = state.get(AbstractChestBlock.CURSED_CHEST_TYPE);
        if (value == CursedChestType.TOP || value == CursedChestType.LEFT || value == CursedChestType.FRONT) {
            return DoubleBlockProperties.Type.FIRST;
        } else if (value == CursedChestType.BACK || value == CursedChestType.RIGHT || value == CursedChestType.BOTTOM) {
            return DoubleBlockProperties.Type.SECOND;
        } else if (value == CursedChestType.SINGLE) {
            return DoubleBlockProperties.Type.SINGLE;
        }
        throw new IllegalArgumentException("Invalid CursedChestType passed.");
    }

    public static CursedChestType getChestType(Direction facing, Direction offset) {
        if (facing.rotateYClockwise() == offset) {
            return CursedChestType.RIGHT;
        } else if (facing.rotateYCounterclockwise() == offset) {
            return CursedChestType.LEFT;
        } else if (facing == offset) {
            return CursedChestType.BACK;
        } else if (facing == offset.getOpposite()) {
            return CursedChestType.FRONT;
        } else if (offset == Direction.DOWN) {
            return CursedChestType.TOP;
        } else if (offset == Direction.UP) {
            return CursedChestType.BOTTOM;
        }
        return CursedChestType.SINGLE;
    }

    @Override
    protected final void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AbstractChestBlock.CURSED_CHEST_TYPE);
        builder.add(Properties.HORIZONTAL_FACING);
        this.appendAdditionalStateDefinitions(builder);
    }

    protected void appendAdditionalStateDefinitions(StateManager.Builder<Block, BlockState> builder) {

    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return this.getBlockEntityType().instantiate(pos, state);
    }

    @NotNull
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        CursedChestType chestType = CursedChestType.SINGLE;
        Direction chestForwardDir = context.getPlayerFacing().getOpposite();
        Direction clickedFace = context.getSide();
        if (context.shouldCancelInteraction()) {
            Direction offsetDir = clickedFace.getOpposite();
            BlockState offsetState = world.getBlockState(pos.offset(offsetDir));
            if (offsetState.isOf(this) && offsetState.get(Properties.HORIZONTAL_FACING) == chestForwardDir && offsetState.get(AbstractChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                chestType = AbstractChestBlock.getChestType(chestForwardDir, offsetDir);
            }
        } else {
            for (Direction dir : Direction.values()) {
                BlockState offsetState = world.getBlockState(pos.offset(dir));
                if (offsetState.isOf(this) && offsetState.get(Properties.HORIZONTAL_FACING) == chestForwardDir && offsetState.get(AbstractChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                    CursedChestType type = AbstractChestBlock.getChestType(chestForwardDir, dir);
                    if (type != CursedChestType.SINGLE) {
                        chestType = type;
                        break;
                    }
                }
            }
        }
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, chestForwardDir).with(AbstractChestBlock.CURSED_CHEST_TYPE, chestType);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction offset, BlockState offsetState, WorldAccess world,
                                                BlockPos pos, BlockPos offsetPos) {
        DoubleBlockProperties.Type mergeType = AbstractChestBlock.getBlockType(state);
        if (mergeType == DoubleBlockProperties.Type.SINGLE) {
            Direction facing = state.get(Properties.HORIZONTAL_FACING);
            if (!offsetState.isOf(this)) {
                return state.with(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE);
            }
            CursedChestType newType = AbstractChestBlock.getChestType(facing, offset);
            if (offsetState.get(AbstractChestBlock.CURSED_CHEST_TYPE) == newType.getOpposite() && facing == offsetState.get(Properties.HORIZONTAL_FACING)) {
                return state.with(AbstractChestBlock.CURSED_CHEST_TYPE, newType);
            }
        } else {
            BlockState otherState = world.getBlockState(pos.offset(AbstractChestBlock.getDirectionToAttached(state)));
            if (!otherState.isOf(this) ||
                    otherState.get(CURSED_CHEST_TYPE) != state.get(CURSED_CHEST_TYPE).getOpposite() ||
                    state.get(Properties.HORIZONTAL_FACING) != state.get(Properties.HORIZONTAL_FACING)) {
                return state.with(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE);
            }
        }
        return super.getStateForNeighborUpdate(state, offset, offsetState, world, pos, offsetPos);
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        // todo: move to properties class / field
        return AbstractChestBlock.createPropertyRetriever(this, state, world, pos, true).get(new Property<OldChestBlockEntity, SidedInventory>() {
            @Override
            public SidedInventory get(OldChestBlockEntity first, OldChestBlockEntity second) {
                return VariableSidedInventory.of(first.getInventory(), second.getInventory());
            }

            @Override
            public SidedInventory get(OldChestBlockEntity single) {
                return single.getInventory();
            }
        }).orElse(null);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        SidedInventory inventory = this.getInventory(state, world, pos);
        if (inventory != null) return ScreenHandler.calculateComparatorOutput(inventory);
        return super.getComparatorOutput(state, world, pos);
    }

    @Override
    public OpenableBlockEntityV2 getOpenableBlockEntity(World world, BlockState state, BlockPos pos) {
        return AbstractChestBlock.createPropertyRetriever(this, state, world, pos, false).get(new Property<OldChestBlockEntity, OpenableBlockEntityV2>() {
            @Override
            public OpenableBlockEntityV2 get(OldChestBlockEntity first, OldChestBlockEntity second) {
                Nameable firstName = first.getNameable();
                Nameable secondName = second.getNameable();
                Text name = firstName.isCustom() ? firstName.get() : secondName.isCustom() ? secondName.get() : Utils.translation("container.expandedstorage.generic_double", firstName.get());
                return new OpenableBlockEntitiesV2(name, first, second);
            }

            @Override
            public OpenableBlockEntityV2 get(OldChestBlockEntity single) {
                return single;
            }
        }).orElse(null);
    }
}
