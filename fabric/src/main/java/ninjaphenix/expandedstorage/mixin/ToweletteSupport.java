package ninjaphenix.expandedstorage.mixin;

import ninjaphenix.expandedstorage.block.ChestBlock;
import org.spongepowered.asm.mixin.Mixin;
import virtuoel.towelette.api.Fluidloggable;

@Mixin(ChestBlock.class)
public abstract class ToweletteSupport implements Fluidloggable {
}
