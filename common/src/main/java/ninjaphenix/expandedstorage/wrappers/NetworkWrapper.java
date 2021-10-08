package ninjaphenix.expandedstorage.wrappers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import ninjaphenix.expandedstorage.internal_api.inventory.SyncedMenuFactory;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface NetworkWrapper {
    static NetworkWrapper getInstance() {
        return NetworkWrapperImpl.getInstance();
    }

    void initialise();

    void c2s_removeTypeSelectCallback();

    void c2s_openTypeSelectScreen();

    void c2s_setSendTypePreference(ResourceLocation selection);

    void s2c_openMenu(ServerPlayer player, SyncedMenuFactory menuFactory);

    void s2c_openSelectScreen(ServerPlayer player, @Nullable Consumer<ResourceLocation> playerPreferenceCallback);

    AbstractContainerMenu createMenu(int windowId, BlockPos blockPos, Container container, Inventory playerInventory, Component title);

    boolean isValidScreenType(ResourceLocation screenType);

    void c2s_sendTypePreference(ResourceLocation selection);

    void s_setPlayerScreenType(ServerPlayer player, ResourceLocation selection);

    void removeTypeSelectCallback(ServerPlayer player);
}
