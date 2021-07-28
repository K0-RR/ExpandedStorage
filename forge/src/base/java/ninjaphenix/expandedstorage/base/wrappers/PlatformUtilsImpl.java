package ninjaphenix.expandedstorage.base.wrappers;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.types.Type;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import net.minecraftforge.fmllegacy.network.IContainerFactory;
import ninjaphenix.expandedstorage.base.internal_api.Utils;
import ninjaphenix.expandedstorage.base.internal_api.inventory.ClientContainerMenuFactory;
import org.lwjgl.glfw.GLFW;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

final class PlatformUtilsImpl implements PlatformUtils {
    private static PlatformUtilsImpl INSTANCE;
    private final Supplier<Object> configKeyMapping = Suppliers.memoize(() -> {
        KeyMapping binding = new KeyMapping("key.expandedstorage.config", KeyConflictContext.GUI, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_W, "key.categories.inventory");
        ClientRegistry.registerKeyBinding(binding);
        return binding;
    });
    private final boolean isClient;

    private PlatformUtilsImpl() {
        isClient = FMLLoader.getDist() == Dist.CLIENT;
    }

    public static PlatformUtilsImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlatformUtilsImpl();
        }
        return INSTANCE;
    }

    @Override
    public CreativeModeTab createTab(Supplier<ItemStack> icon) {
        return new CreativeModeTab(Utils.MOD_ID) {
            @Override
            public ItemStack makeIcon() {
                return icon.get();
            }
        };
    }

    @Override
    public boolean isClient() {
        return isClient;
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(ResourceLocation menuType, ClientContainerMenuFactory<T> factory) {
        MenuType<T> menu = new MenuType<>((IContainerFactory<T>) factory::create);
        menu.setRegistryName(menuType);
        return menu;
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> blockEntitySupplier, Set<Block> blocks, Type<?> type) {
        return new BlockEntityType<>(blockEntitySupplier::apply, blocks, type);
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public KeyMapping getConfigScreenKeyMapping() {
        return (KeyMapping) configKeyMapping.get();
    }

    @Override
    public boolean isKeyMappingPressed(int keyCode, int scanCode, int modifiers) {
        return getConfigScreenKeyMapping().isActiveAndMatches(InputConstants.getKey(keyCode, scanCode));
    }
}
