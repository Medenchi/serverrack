package com.denchy.serverrack.det;

import com.denchy.serverrack.client.ServerRackClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * Director's detonator.
 * LMB = corner 1 (via AttackBlockCallback), RMB on block = corner 2,
 * RMB tap in air = boom menu, RMB HOLD in air = nuke settings,
 * sneak+RMB in air = clear selection.
 */
public class DetonatorItem extends Item {

    public DetonatorItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        if (!ctx.getWorld().isClient && ctx.getPlayer() instanceof ServerPlayerEntity sp) {
            BoomManager.setSel2(sp, ctx.getBlockPos());
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            if (!world.isClient && user instanceof ServerPlayerEntity sp) {
                BoomManager.clearViaItem(sp);
            }
            return TypedActionResult.success(user.getStackInHand(hand));
        }
        if (world.isClient) {
            // gesture resolution (tap vs hold) runs in the client tick loop
            ServerRackClient.beginDetonatorHold();
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
