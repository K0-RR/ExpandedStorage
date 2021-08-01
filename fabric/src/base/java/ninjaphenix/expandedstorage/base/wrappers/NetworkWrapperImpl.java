package ninjaphenix.expandedstorage.base.wrappers;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import ninjaphenix.expandedstorage.base.client.menu.PickScreen;
import ninjaphenix.expandedstorage.base.internal_api.Utils;
import ninjaphenix.expandedstorage.base.internal_api.inventory.AbstractMenu;
import ninjaphenix.expandedstorage.base.internal_api.inventory.ServerMenuFactory;
import ninjaphenix.expandedstorage.base.internal_api.inventory.SyncedMenuFactory;
import ninjaphenix.expandedstorage.base.inventory.PagedMenu;
import ninjaphenix.expandedstorage.base.inventory.ScrollableMenu;
import ninjaphenix.expandedstorage.base.inventory.SingleMenu;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

final class NetworkWrapperImpl implements NetworkWrapper {
    private static final ResourceLocation OPEN_SELECT_SCREEN = Utils.resloc("open_select_screen");
    private static final ResourceLocation UPDATE_PLAYER_PREFERENCE = Utils.resloc("update_player_preference");
    private static final ResourceLocation REMOVE_TYPE_SELECT_CALLBACK = Utils.resloc("remove_type_select_callback");
    private static NetworkWrapperImpl INSTANCE;
    private final Map<UUID, Consumer<ResourceLocation>> preferenceCallbacks = new HashMap<>();
    private final Map<UUID, ResourceLocation> playerPreferences = new HashMap<>();
    private final Map<ResourceLocation, ServerMenuFactory> menuFactories = Utils.unmodifiableMap(map -> {
        map.put(Utils.SINGLE_SCREEN_TYPE, SingleMenu::new);
        map.put(Utils.SCROLLABLE_SCREEN_TYPE, ScrollableMenu::new);
        map.put(Utils.PAGED_SCREEN_TYPE, PagedMenu::new);
    });

    public static NetworkWrapper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NetworkWrapperImpl();
        }
        return INSTANCE;
    }

    public void initialise() {
        if (PlatformUtils.getInstance().isClient()) {
            new Client().initialise();
        }
        // Register Server Receivers
        ServerPlayConnectionEvents.INIT.register((listener_init, server_unused) -> {
            ServerPlayNetworking.registerReceiver(listener_init, NetworkWrapperImpl.OPEN_SELECT_SCREEN, this::s_handleOpenSelectScreen);
            ServerPlayNetworking.registerReceiver(listener_init, NetworkWrapperImpl.UPDATE_PLAYER_PREFERENCE, this::s_handleUpdatePlayerPreference);
            ServerPlayNetworking.registerReceiver(listener_init, NetworkWrapperImpl.REMOVE_TYPE_SELECT_CALLBACK, this::s_handleRemoveTypeSelectCallback);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((listener, server) -> this.s_setPlayerScreenType(listener.player, Utils.UNSET_SCREEN_TYPE));
    }

    public void c2s_removeTypeSelectCallback() {
        if (ClientPlayNetworking.canSend(NetworkWrapperImpl.REMOVE_TYPE_SELECT_CALLBACK)) {
            ClientPlayNetworking.send(NetworkWrapperImpl.REMOVE_TYPE_SELECT_CALLBACK, new FriendlyByteBuf(Unpooled.buffer()));
        }
    }

    public void c2s_openTypeSelectScreen() {
        if (ClientPlayNetworking.canSend(NetworkWrapperImpl.OPEN_SELECT_SCREEN)) {
            ClientPlayNetworking.send(NetworkWrapperImpl.OPEN_SELECT_SCREEN, new FriendlyByteBuf(Unpooled.buffer()));
        }
    }

    public void c2s_setSendTypePreference(ResourceLocation selection) {
        if (ConfigWrapper.getInstance().setPreferredScreenType(selection)) {
            this.c2s_sendTypePreference(selection);
        }
    }

    public void s2c_openMenu(ServerPlayer player, SyncedMenuFactory menuFactory) {
        UUID uuid = player.getUUID();
        if (playerPreferences.containsKey(uuid) && this.isValidScreenType(playerPreferences.get(uuid))) {
            player.openMenu(new ExtendedScreenHandlerFactory() {
                @Override
                public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buffer) {
                    menuFactory.writeClientData(player, buffer);
                }

                @Override
                public Component getDisplayName() {
                    return menuFactory.getMenuTitle();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
                    return menuFactory.createMenu(windowId, inventory, (ServerPlayer) player);
                }
            });
        } else {
            this.s2c_openSelectScreen(player, (type) -> this.s2c_openMenu(player, menuFactory));
        }
    }

    public void s2c_openSelectScreen(ServerPlayer player, Consumer<ResourceLocation> playerPreferenceCallback) {
        if (ServerPlayNetworking.canSend(player, NetworkWrapperImpl.OPEN_SELECT_SCREEN)) {
            if (playerPreferenceCallback != null) {
                preferenceCallbacks.put(player.getUUID(), playerPreferenceCallback);
            }
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeInt(menuFactories.size());
            menuFactories.keySet().forEach(buffer::writeResourceLocation);
            ServerPlayNetworking.send(player, NetworkWrapperImpl.OPEN_SELECT_SCREEN, buffer);
        }
        // else illegal state
    }

    public AbstractContainerMenu createMenu(int windowId, BlockPos pos, Container container, Inventory inventory, Component title) {
        UUID uuid = inventory.player.getUUID();
        ResourceLocation playerPreference;
        if (playerPreferences.containsKey(uuid) && menuFactories.containsKey(playerPreference = playerPreferences.get(uuid))) {
            return menuFactories.get(playerPreference).create(windowId, pos, container, inventory, title);
        }
        return null;
    }

    @Override
    public void s_setPlayerScreenType(ServerPlayer player, ResourceLocation screenType) {
        UUID uuid = player.getUUID();
        if (menuFactories.containsKey(screenType)) {
            playerPreferences.put(uuid, screenType);
            if (preferenceCallbacks.containsKey(uuid)) {
                preferenceCallbacks.remove(uuid).accept(screenType);
            }
        } else {
            playerPreferences.remove(uuid);
            preferenceCallbacks.remove(uuid);
        }
    }

    private void s_handleUpdatePlayerPreference(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener,
                                                FriendlyByteBuf buffer, PacketSender sender) {
        ResourceLocation screenType = buffer.readResourceLocation();
        server.submit(() -> this.s_setPlayerScreenType(player, screenType));
    }

    private void s_handleRemoveTypeSelectCallback(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener,
                                                  FriendlyByteBuf buffer, PacketSender sender) {
        server.submit(() -> this.removeTypeSelectCallback(player));
    }

    private void s_handleOpenSelectScreen(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener,
                                          FriendlyByteBuf buffer, PacketSender sender) {
        if (player.containerMenu instanceof AbstractMenu<?> menu) {
            server.submit(() -> this.s2c_openSelectScreen(player, (type) -> player.openMenu(new ExtendedScreenHandlerFactory() {
                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
                    return NetworkWrapperImpl.this.createMenu(windowId, menu.pos, menu.getContainer(), inventory, menu.getTitle());
                }

                @Override
                public Component getDisplayName() {
                    return menu.getTitle();
                }

                @Override
                public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buffer) {
                    buffer.writeBlockPos(menu.pos).writeInt(menu.getContainer().getContainerSize());
                }
            })));
        } else {
            server.submit(() -> this.s2c_openSelectScreen(player, null));
        }
    }

    public boolean isValidScreenType(ResourceLocation screenType) {
        return screenType != null && menuFactories.containsKey(screenType);
    }

    @Override
    public void c2s_sendTypePreference(ResourceLocation selection) {
        if (ClientPlayNetworking.canSend(NetworkWrapperImpl.UPDATE_PLAYER_PREFERENCE)) {
            ClientPlayNetworking.send(NetworkWrapperImpl.UPDATE_PLAYER_PREFERENCE, new FriendlyByteBuf(Unpooled.buffer()).writeResourceLocation(selection));
        }
    }

    @Override
    public void removeTypeSelectCallback(ServerPlayer player) {
        preferenceCallbacks.remove(player.getUUID());
    }

    private class Client {
        public void initialise() {
            ClientPlayConnectionEvents.INIT.register((listener_init, client) -> ClientPlayNetworking.registerReceiver(NetworkWrapperImpl.OPEN_SELECT_SCREEN, this::c_handleOpenSelectScreen));
            ClientPlayConnectionEvents.JOIN.register((listener_play, sender, client) -> sender.sendPacket(NetworkWrapperImpl.UPDATE_PLAYER_PREFERENCE, new FriendlyByteBuf(Unpooled.buffer()).writeResourceLocation(ConfigWrapper.getInstance().getPreferredScreenType())));
        }

        private void c_handleOpenSelectScreen(Minecraft minecraft, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
            int count = buffer.readInt();
            HashSet<ResourceLocation> allowed = new HashSet<>();
            for (int i = 0; i < count; i++) {
                ResourceLocation screenType = buffer.readResourceLocation();
                if (menuFactories.containsKey(screenType)) {
                    allowed.add(screenType);
                }
            }
            minecraft.submit(() -> minecraft.setScreen(new PickScreen(allowed, null)));
        }
    }
}
