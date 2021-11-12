/**
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
package ninjaphenix.expandedstorage.item;

import ninjaphenix.expandedstorage.Common;
import ninjaphenix.expandedstorage.Utils;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class StorageConversionKit extends Item {
    private final ResourceLocation from;
    private final ResourceLocation to;
    private final Component instructionsFirst;
    private final Component instructionsSecond;

    public StorageConversionKit(Properties properties, ResourceLocation from, ResourceLocation to) {
        super(properties);
        this.from = from;
        this.to = to;
        if (PlatformUtils.getInstance().isForge()) {
            this.instructionsFirst = Utils.translation("tooltip.expandedstorage.conversion_kit_" + from.getPath() + "_" + to.getPath() + "_1", Utils.ALT_USE).withStyle(ChatFormatting.GRAY).append(Utils.translation("tooltip.expandedstorage.conversion_kit_" + from.getPath() + "_" + to.getPath() + "_2", Utils.ALT_USE).withStyle(ChatFormatting.GRAY));
            this.instructionsSecond = new TextComponent("");
        } else {
            this.instructionsFirst = Utils.translation("tooltip.expandedstorage.conversion_kit_" + from.getPath() + "_" + to.getPath() + "_1", Utils.ALT_USE).withStyle(ChatFormatting.GRAY);
            this.instructionsSecond = Utils.translation("tooltip.expandedstorage.conversion_kit_" + from.getPath() + "_" + to.getPath() + "_2", Utils.ALT_USE).withStyle(ChatFormatting.GRAY);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        if (player != null) {
            player.getCooldowns().addCooldown(this, Utils.QUARTER_SECOND);
            if (player.isShiftKeyDown()) {
                Block block = world.getBlockState(context.getClickedPos()).getBlock();
                BlockUpgradeBehaviour behaviour = Common.getBlockUpgradeBehaviour(block);
                if (behaviour != null) {
                    if (world.isClientSide()) {
                        return InteractionResult.CONSUME;
                    } else if (behaviour.tryUpgradeBlock(context, from, to)) {
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        list.add(instructionsFirst);
        if (!instructionsSecond.getString().equals("")) {
            list.add(instructionsSecond);
        }
    }
}
