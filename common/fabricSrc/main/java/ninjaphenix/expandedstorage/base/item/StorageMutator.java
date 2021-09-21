package ninjaphenix.expandedstorage.base.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties.Type;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.barrel.block.BarrelBlock;
import ninjaphenix.expandedstorage.base.internal_api.BaseApi;
import ninjaphenix.expandedstorage.base.internal_api.Utils;
import ninjaphenix.expandedstorage.base.internal_api.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.base.internal_api.item.MutationMode;
import ninjaphenix.expandedstorage.chest.ChestCommon;
import ninjaphenix.expandedstorage.chest.block.ChestBlock;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.state.property.Properties.*;
import static net.minecraft.util.BlockRotation.CLOCKWISE_180;
import static net.minecraft.util.BlockRotation.CLOCKWISE_90;

public class StorageMutator extends Item {
    public StorageMutator(Item.Settings properties) {
        super(properties);
    }

    private static MutationMode getMode(ItemStack stack) {
        NbtCompound tag = stack.getOrCreateNbt();
        if (!tag.contains("mode", NbtElement.BYTE_TYPE)) {
            tag.putByte("mode", (byte) 0);
        }
        return MutationMode.from(tag.getByte("mode"));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof BarrelBlock) {
            return this.useModifierOnBlock(context, state, pos, Type.SINGLE);
        } else if (block instanceof AbstractChestBlock) {
            return this.useModifierOnBlock(context, state, pos, AbstractChestBlock.getBlockType(state));
        } else {
            return this.useOnBlock(context, state, context.getBlockPos());
        }
    }

    protected ActionResult useOnBlock(ItemUsageContext context, BlockState state, BlockPos pos) {
        ItemStack stack = context.getStack();
        World level = context.getWorld();
        PlayerEntity player = context.getPlayer();
        Block block = state.getBlock();
        if (block instanceof net.minecraft.block.AbstractChestBlock) {
            if (StorageMutator.getMode(stack) == MutationMode.ROTATE) {
                if (state.contains(CHEST_TYPE)) {
                    ChestType chestType = state.get(CHEST_TYPE);
                    if (chestType != ChestType.SINGLE) {
                        if (!level.isClient()) {
                            BlockPos otherPos = pos.offset(net.minecraft.block.ChestBlock.getFacing(state));
                            BlockState otherState = level.getBlockState(otherPos);
                            level.setBlockState(pos, state.rotate(CLOCKWISE_180).with(CHEST_TYPE, state.get(CHEST_TYPE).getOpposite()));
                            level.setBlockState(otherPos, otherState.rotate(CLOCKWISE_180).with(CHEST_TYPE, otherState.get(CHEST_TYPE).getOpposite()));
                        }
                        //noinspection ConstantConditions
                        player.getItemCooldownManager().set(this, Utils.QUARTER_SECOND);
                        return ActionResult.SUCCESS;
                    }
                }
                level.setBlockState(pos, state.rotate(CLOCKWISE_90));
                //noinspection ConstantConditions
                player.getItemCooldownManager().set(this, Utils.QUARTER_SECOND);
                return ActionResult.SUCCESS;
            }
        }
        if (block instanceof net.minecraft.block.ChestBlock) {
            MutationMode mode = StorageMutator.getMode(stack);
            if (mode == MutationMode.MERGE) {
                NbtCompound tag = stack.getOrCreateNbt();
                if (tag.contains("pos")) {
                    BlockPos otherPos = NbtHelper.toBlockPos(tag.getCompound("pos"));
                    BlockState otherState = level.getBlockState(otherPos);
                    if (otherState.getBlock() == state.getBlock() &&
                            otherState.get(HORIZONTAL_FACING) == state.get(HORIZONTAL_FACING) &&
                            otherState.get(CHEST_TYPE) == ChestType.SINGLE) {
                        if (!level.isClient()) {
                            BlockPos offset = otherPos.subtract(pos);
                            Direction direction = Direction.fromVector(offset.getX(), offset.getY(), offset.getZ());
                            if (direction != null) {
                                CursedChestType type = ChestBlock.getChestType(state.get(AbstractChestBlock.Y_ROTATION), state.get(AbstractChestBlock.FACE_ROTATION), state.get(AbstractChestBlock.PERP_ROTATION), direction);
                                Predicate<BlockEntity> isRandomizable = b -> b instanceof LootableContainerBlockEntity;
                                this.convertBlock(level, state, pos, BaseApi.getInstance().getTieredBlock(ChestCommon.BLOCK_TYPE, Utils.WOOD_TIER.getId()), Utils.WOOD_STACK_COUNT, type, isRandomizable);
                                this.convertBlock(level, otherState, otherPos, BaseApi.getInstance().getTieredBlock(ChestCommon.BLOCK_TYPE, Utils.WOOD_TIER.getId()), Utils.WOOD_STACK_COUNT, type.getOpposite(), isRandomizable);
                                tag.remove("pos");
                                //noinspection ConstantConditions
                                player.sendMessage(new TranslatableText("tooltip.expandedstorage.storage_mutator.merge_end"), true);
                            }
                        }
                        //noinspection ConstantConditions
                        player.getItemCooldownManager().set(this, Utils.QUARTER_SECOND);
                        return ActionResult.SUCCESS;
                    }
                } else {
                    if (!level.isClient()) {
                        tag.put("pos", NbtHelper.fromBlockPos(pos));
                        //noinspection ConstantConditions
                        player.sendMessage(new TranslatableText("tooltip.expandedstorage.storage_mutator.merge_start"), true);
                    }
                    //noinspection ConstantConditions
                    player.getItemCooldownManager().set(this, Utils.QUARTER_SECOND);
                    return ActionResult.SUCCESS;
                }
            } else if (mode == MutationMode.SPLIT) {
                ChestType chestType = state.get(CHEST_TYPE);
                if (chestType != ChestType.SINGLE) {
                    if (!level.isClient()) {
                        BlockPos otherPos = pos.offset(net.minecraft.block.ChestBlock.getFacing(state));
                        BlockState otherState = level.getBlockState(otherPos);
                        level.setBlockState(pos, state.with(CHEST_TYPE, ChestType.SINGLE));
                        level.setBlockState(otherPos, otherState.with(CHEST_TYPE, ChestType.SINGLE));
                    }
                    return ActionResult.SUCCESS;
                }
            }
        } else if (block instanceof net.minecraft.block.BarrelBlock) {
            if (StorageMutator.getMode(stack) == MutationMode.ROTATE) {
                if (!level.isClient()) {
                    Direction direction = state.get(FACING);
                    level.setBlockState(pos, state.with(FACING, Direction.byId(direction.getId() + 1)));
                }
                //noinspection ConstantConditions
                player.getItemCooldownManager().set(this, Utils.QUARTER_SECOND);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.FAIL;
    }

    private void convertBlock(World level, BlockState state, BlockPos pos, Block block, int slotCount, @Nullable CursedChestType type, Predicate<BlockEntity> check) {
        BlockEntity targetBlockEntity = level.getBlockEntity(pos);
        if (check.test(targetBlockEntity)) {
            DefaultedList<ItemStack> invData = DefaultedList.ofSize(slotCount, ItemStack.EMPTY);
            //noinspection ConstantConditions
            Inventories.readNbt(targetBlockEntity.writeNbt(new NbtCompound()), invData);
            level.removeBlockEntity(pos);
            BlockState newState = block.getDefaultState();
            if (state.contains(WATERLOGGED)) {
                newState = newState.with(WATERLOGGED, state.get(WATERLOGGED));
            }
            if (state.contains(FACING)) {
                newState = newState.with(FACING, state.get(FACING));
            } else if (state.contains(HORIZONTAL_FACING)) {
                newState = newState.with(HORIZONTAL_FACING, state.get(HORIZONTAL_FACING));
            }
            if (type != null) {
                newState = newState.with(ChestBlock.CURSED_CHEST_TYPE, type);
            }
            level.setBlockState(pos, newState);
            BlockEntity newEntity = level.getBlockEntity(pos);
            //noinspection ConstantConditions
            newEntity.readNbt(Inventories.writeNbt(newEntity.writeNbt(new NbtCompound()), invData));
        }
    }

    protected ActionResult useModifierOnBlock(ItemUsageContext context, BlockState state, BlockPos pos, @SuppressWarnings("unused") Type type) {
        World level = context.getWorld();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        Block block = state.getBlock();
        MutationMode mode = StorageMutator.getMode(context.getStack());
        if (mode == MutationMode.MERGE) {
            if (block instanceof AbstractChestBlock<?> chestBlock && state.get(ChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                NbtCompound tag = stack.getOrCreateNbt();
                if (tag.contains("pos")) {
                    BlockPos otherPos = NbtHelper.toBlockPos(tag.getCompound("pos"));
                    BlockState otherState = level.getBlockState(otherPos);
                    Direction facing = state.get(HORIZONTAL_FACING);
                    if (block == otherState.getBlock()
                            && facing == otherState.get(HORIZONTAL_FACING)
                            && otherState.get(AbstractChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) {
                        if (!level.isClient()) {
                            BlockPos offset = otherPos.subtract(pos);
                            Direction direction = Direction.fromVector(offset.getX(), offset.getY(), offset.getZ());
                            if (direction != null) {
                                CursedChestType chestType = AbstractChestBlock.getChestType(state.get(AbstractChestBlock.Y_ROTATION), state.get(AbstractChestBlock.FACE_ROTATION), state.get(AbstractChestBlock.PERP_ROTATION), direction);
                                Predicate<BlockEntity> isStorage = b -> b instanceof AbstractOpenableStorageBlockEntity;
                                this.convertBlock(level, state, pos, block, chestBlock.getSlotCount(), chestType, isStorage);
                                this.convertBlock(level, otherState, otherPos, block, chestBlock.getSlotCount(), chestType.getOpposite(), isStorage);
                                tag.remove("pos");
                                //noinspection ConstantConditions
                                player.sendMessage(new TranslatableText("tooltip.expandedstorage.storage_mutator.merge_end"), true);
                            }
                        }
                        //noinspection ConstantConditions
                        player.getItemCooldownManager().set(this, Utils.QUARTER_SECOND);
                        return ActionResult.SUCCESS;
                    }
                } else {
                    if (!level.isClient()) {
                        tag.put("pos", NbtHelper.fromBlockPos(pos));
                        //noinspection ConstantConditions
                        player.sendMessage(new TranslatableText("tooltip.expandedstorage.storage_mutator.merge_start"), true);
                    }
                    //noinspection ConstantConditions
                    player.getItemCooldownManager().set(this, Utils.QUARTER_SECOND);
                    return ActionResult.SUCCESS;
                }
            }
        } else if (mode == MutationMode.SPLIT) {
            if (block instanceof AbstractChestBlock && state.get(AbstractChestBlock.CURSED_CHEST_TYPE) != CursedChestType.SINGLE) {
                if (!level.isClient()) {
                    BlockPos otherPos = pos.offset(AbstractChestBlock.getDirectionToAttached(state));
                    BlockState otherState = level.getBlockState(otherPos);
                    level.setBlockState(pos, state.with(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE));
                    level.setBlockState(otherPos, otherState.with(AbstractChestBlock.CURSED_CHEST_TYPE, CursedChestType.SINGLE));
                }
                //noinspection ConstantConditions
                player.getItemCooldownManager().set(this, Utils.QUARTER_SECOND);
                return ActionResult.SUCCESS;
            }
        } else if (mode == MutationMode.ROTATE) {
            if (state.contains(FACING)) {
                if (!level.isClient()) {
                    Direction direction = state.get(FACING);
                    level.setBlockState(pos, state.with(FACING, Direction.byId(direction.getId() + 1)));
                }
                //noinspection ConstantConditions
                player.getItemCooldownManager().set(this, Utils.QUARTER_SECOND);
                return ActionResult.SUCCESS;
            } else if (state.contains(HORIZONTAL_FACING)) {
                if (block instanceof AbstractChestBlock) {
                    if (!level.isClient()) {
                        CursedChestType value = state.get(AbstractChestBlock.CURSED_CHEST_TYPE);
                        if (value == CursedChestType.SINGLE) {
                            level.setBlockState(pos, state.rotate(CLOCKWISE_90));
                        } else if (value == CursedChestType.TOP || value == CursedChestType.BOTTOM) {
                            level.setBlockState(pos, state.rotate(CLOCKWISE_90));
                            BlockPos otherPos = pos.offset(AbstractChestBlock.getDirectionToAttached(state));
                            BlockState otherState = level.getBlockState(otherPos);
                            level.setBlockState(otherPos, otherState.rotate(CLOCKWISE_90));
                        } else if (value == CursedChestType.FRONT || value == CursedChestType.BACK || value == CursedChestType.LEFT || value == CursedChestType.RIGHT) {
                            level.setBlockState(pos, state.rotate(CLOCKWISE_180).with(AbstractChestBlock.CURSED_CHEST_TYPE, state.get(AbstractChestBlock.CURSED_CHEST_TYPE).getOpposite()));
                            BlockPos otherPos = pos.offset(AbstractChestBlock.getDirectionToAttached(state));
                            BlockState otherState = level.getBlockState(otherPos);
                            level.setBlockState(otherPos, otherState.rotate(CLOCKWISE_180).with(AbstractChestBlock.CURSED_CHEST_TYPE, otherState.get(AbstractChestBlock.CURSED_CHEST_TYPE).getOpposite()));
                        }
                    }
                    //noinspection ConstantConditions
                    player.getItemCooldownManager().set(this, Utils.QUARTER_SECOND);
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.FAIL;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        TypedActionResult<ItemStack> result = this.useModifierInAir(level, player, hand);
        if (result.getResult() == ActionResult.SUCCESS) {
            player.getItemCooldownManager().set(this, Utils.QUARTER_SECOND);
        }
        return result;
    }

    private TypedActionResult<ItemStack> useModifierInAir(World level, PlayerEntity player, Hand hand) {
        if (player.isSneaking()) {
            ItemStack stack = player.getStackInHand(hand);
            NbtCompound tag = stack.getOrCreateNbt();
            MutationMode nextMode = StorageMutator.getMode(stack).next();
            tag.putByte("mode", nextMode.toByte());
            if (tag.contains("pos")) {
                tag.remove("pos");
            }
            if (!level.isClient()) {
                player.sendMessage(new TranslatableText("tooltip.expandedstorage.storage_mutator.description_" + nextMode, Utils.ALT_USE), true);
            }
            return TypedActionResult.success(stack);
        }
        return TypedActionResult.pass(player.getStackInHand(hand));
    }

    @Override
    public void onCraft(ItemStack stack, World level, PlayerEntity player) {
        super.onCraft(stack, level, player);
        StorageMutator.getMode(stack);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        StorageMutator.getMode(stack);
        return stack;
    }

    @Override
    public void appendStacks(ItemGroup tab, DefaultedList<ItemStack> stacks) {
        if (this.isIn(tab)) {
            stacks.add(this.getDefaultStack());
        }
    }

    private MutableText getToolModeText(MutationMode mode) {
        return new TranslatableText("tooltip.expandedstorage.storage_mutator.tool_mode",
                new TranslatableText("tooltip.expandedstorage.storage_mutator." + mode));
    }

    @Override
    protected String getOrCreateTranslationKey() {
        return "item.expandedstorage.storage_mutator";
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> list, TooltipContext flag) {
        MutationMode mode = StorageMutator.getMode(stack);
        list.add(this.getToolModeText(mode).formatted(Formatting.GRAY));
        list.add(Utils.translation("tooltip.expandedstorage.storage_mutator.description_" + mode, Utils.ALT_USE).formatted(Formatting.GRAY));
    }
}
