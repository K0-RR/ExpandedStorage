package ninjaphenix.expandedstorage.internal_api;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import ninjaphenix.expandedstorage.internal_api.tier.Tier;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@Internal
@Experimental
public final class Utils {
    @Internal
    public static final String MOD_ID = "expandedstorage";
    public static final CreativeModeTab TAB = PlatformUtils.getInstance().createTab(() -> new ItemStack(Registry.ITEM.get(Utils.resloc("netherite_chest"))));
    @Internal
    public static final Component ALT_USE = new TranslatableComponent("tooltip.expandedstorage.alt_use",
            new KeybindComponent("key.sneak").withStyle(ChatFormatting.GOLD),
            new KeybindComponent("key.use").withStyle(ChatFormatting.GOLD));

    // Slots for Storage Tiers
    public static final int WOOD_STACK_COUNT = 27;

    // Default tiers which all modules can, but don't need to, specify blocks for.
    public static final Tier WOOD_TIER = new Tier(Utils.resloc("wood"), WOOD_STACK_COUNT, UnaryOperator.identity(), UnaryOperator.identity());
    public static final Tier IRON_TIER = new Tier(Utils.resloc("iron"), 54, BlockBehaviour.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
    public static final Tier GOLD_TIER = new Tier(Utils.resloc("gold"), 81, BlockBehaviour.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
    public static final Tier DIAMOND_TIER = new Tier(Utils.resloc("diamond"), 108, BlockBehaviour.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
    public static final Tier OBSIDIAN_TIER = new Tier(Utils.resloc("obsidian"), 108, BlockBehaviour.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
    public static final Tier NETHERITE_TIER = new Tier(Utils.resloc("netherite"), 135, BlockBehaviour.Properties::requiresCorrectToolForDrops, Item.Properties::fireResistant);

    // NBT Tag Types
    /**
     * @deprecated Removing in 1.17, in 1.17 use {@link net.minecraft.nbt.Tag.TAG_STRING} instead.
     */
    @Deprecated
    @ScheduledForRemoval(inVersion = "8 (MC=1.17)")
    public static final int NBT_STRING_TYPE = 8;

    /**
     * @deprecated Removing in 1.17, in 1.17 use {@link net.minecraft.nbt.Tag.TAG_COMPOUND} instead.
     */
    @Deprecated
    @ScheduledForRemoval(inVersion = "8 (MC=1.17)")
    public static final int NBT_COMPOUND_TYPE = 10;

    /**
     * @deprecated Removing in 1.17, in 1.17 use {@link net.minecraft.nbt.Tag.TAG_BYTE} instead.
     */
    @Deprecated
    @ScheduledForRemoval(inVersion = "8 (MC=1.17)")
    public static final int NBT_BYTE_TYPE = 1;

    /**
     * @deprecated Removing in 1.17, in 1.17 use {@link net.minecraft.nbt.Tag.TAG_LIST} instead.
     */
    @Deprecated
    @ScheduledForRemoval(inVersion = "8 (MC=1.17)")
    public static final int NBT_LIST_TYPE = 9;

    // Item Cooldown
    public static final int QUARTER_SECOND = 5;

    private Utils() {

    }

    @Internal
    public static ResourceLocation resloc(String path) {
        return new ResourceLocation(Utils.MOD_ID, path);
    }

    @Internal
    public static MutableComponent translation(String key, Object... params) {
        return new TranslatableComponent(key, params);
    }

    @Internal
    public static <K, V> Map<K, V> unmodifiableMap(Consumer<Map<K, V>> initialiser) {
        Map<K, V> map = new HashMap<>();
        initialiser.accept(map);
        return Collections.unmodifiableMap(map);
    }
}
