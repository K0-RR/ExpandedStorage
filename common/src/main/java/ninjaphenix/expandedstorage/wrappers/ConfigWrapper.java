package ninjaphenix.expandedstorage.wrappers;

import net.minecraft.resources.ResourceLocation;

public interface ConfigWrapper {
    static ConfigWrapper getInstance() {
        return ConfigWrapperImpl.getInstance();
    }

    void initialise();

    boolean isScrollingUnrestricted();

    @SuppressWarnings("unused")
    void setScrollingRestricted(boolean value);

    ResourceLocation getPreferredScreenType();

    boolean setPreferredScreenType(ResourceLocation screenType);
}
