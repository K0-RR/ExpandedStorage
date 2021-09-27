package ninjaphenix.expandedstorage.base.client;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.resources.ResourceLocation;
import ninjaphenix.expandedstorage.base.client.menu.PickScreen;
import ninjaphenix.expandedstorage.base.internal_api.Utils;

import java.util.HashSet;
import java.util.Set;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return returnToScreen -> {
            Set<ResourceLocation> values = new HashSet<>();
            values.add(Utils.SINGLE_SCREEN_TYPE);
            values.add(Utils.PAGED_SCREEN_TYPE);
            values.add(Utils.SCROLLABLE_SCREEN_TYPE);
            return new PickScreen(values, returnToScreen);
        };
    }
}
