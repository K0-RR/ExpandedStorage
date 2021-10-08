package ninjaphenix.expandedstorage.base.inventory;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import ninjaphenix.expandedstorage.BaseCommon;
import ninjaphenix.expandedstorage.internal_api.Utils;
import ninjaphenix.expandedstorage.internal_api.inventory.AbstractMenu;
import ninjaphenix.expandedstorage.internal_api.inventory.ClientMenuFactory;
import ninjaphenix.expandedstorage.base.inventory.screen.ScrollableScreenMeta;

import java.util.function.IntUnaryOperator;

public final class ScrollableMenu extends AbstractMenu<ScrollableScreenMeta> {
    // @formatter:off
    private static final ImmutableMap<Integer, ScrollableScreenMeta> SIZES = ImmutableMap.<Integer, ScrollableScreenMeta>builder()
            .put(Utils.WOOD_STACK_COUNT, new ScrollableScreenMeta(9, 3, Utils.WOOD_STACK_COUNT, AbstractMenu.getTexture("shared", 9, 3), 208, 192))
            .put(Utils.IRON_STACK_COUNT, new ScrollableScreenMeta(9, 6, Utils.IRON_STACK_COUNT, AbstractMenu.getTexture("shared", 9, 6), 208, 240))
            .put(Utils.GOLD_STACK_COUNT, new ScrollableScreenMeta(9, 6, Utils.GOLD_STACK_COUNT, AbstractMenu.getTexture("shared", 9, 6), 208, 240))
            .put(Utils.DIAMOND_STACK_COUNT, new ScrollableScreenMeta(9, 6, Utils.DIAMOND_STACK_COUNT, AbstractMenu.getTexture("shared", 9, 6), 208, 240))
            .put(Utils.NETHERITE_STACK_COUNT, new ScrollableScreenMeta(9, 6, Utils.NETHERITE_STACK_COUNT, AbstractMenu.getTexture("shared", 9, 6), 208, 240))
            .put(2 * Utils.GOLD_STACK_COUNT, new ScrollableScreenMeta(9, 6, 2 * Utils.GOLD_STACK_COUNT, AbstractMenu.getTexture("shared", 9, 6), 208, 240))
            .put(2 * Utils.DIAMOND_STACK_COUNT, new ScrollableScreenMeta(9, 6, 2 * Utils.DIAMOND_STACK_COUNT, AbstractMenu.getTexture("shared", 9, 6), 208, 240))
            .put(2 * Utils.NETHERITE_STACK_COUNT, new ScrollableScreenMeta(9, 6, 2 * Utils.NETHERITE_STACK_COUNT, AbstractMenu.getTexture("shared", 9, 6), 208, 240))
            .build();
    // @formatter:on

    public ScrollableMenu(int windowId, BlockPos pos, Container container, Inventory inventory, Component title) {
        super(BaseCommon.SCROLL_MENU_TYPE.get(), windowId, pos, container, inventory, title,
                AbstractMenu.getNearestScreenMeta(container.getContainerSize(), ScrollableMenu.SIZES));
        for (int i = 0; i < container.getContainerSize(); i++) {
            int slotXPos = i % screenMeta.width;
            int slotYPos = Mth.ceil((((double) (i - slotXPos)) / screenMeta.width));
            int realYPos = slotYPos >= screenMeta.height ? -2000 : slotYPos * Utils.SLOT_SIZE + Utils.SLOT_SIZE;
            this.addSlot(new Slot(container, i, slotXPos * Utils.SLOT_SIZE + 8, realYPos));
        }
        int left = (screenMeta.width * Utils.SLOT_SIZE + 14) / 2 - 80;
        int top = Utils.SLOT_SIZE + 14 + (screenMeta.height * Utils.SLOT_SIZE);
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                this.addSlot(new Slot(inventory, y * 9 + x + 9, left + Utils.SLOT_SIZE * x, top + y * Utils.SLOT_SIZE));
            }
        }
        for (int x = 0; x < 9; x++) {
            this.addSlot(new Slot(inventory, x, left + Utils.SLOT_SIZE * x, top + 58));
        }
    }

    public void moveSlotRange(int minSlotIndex, int maxSlotIndex, int yDifference) {
        for (int i = minSlotIndex; i < maxSlotIndex; i++) {
            slots.get(i).y += yDifference;
        }
    }

    public void setSlotRange(int minSlotIndex, int maxSlotIndex, IntUnaryOperator yMutator) {
        for (int i = minSlotIndex; i < maxSlotIndex; i++) {
            slots.get(i).y = yMutator.applyAsInt(i);
        }
    }

    public static final class Factory implements ClientMenuFactory<ScrollableMenu> {
        @Override
        public ScrollableMenu create(int windowId, Inventory inventory, FriendlyByteBuf buffer) {
            if (buffer == null) {
                return null;
            }
            return new ScrollableMenu(windowId, buffer.readBlockPos(), new SimpleContainer(buffer.readInt()), inventory, null);
        }
    }
}
