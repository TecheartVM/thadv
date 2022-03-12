package techeart.thadv.utils;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Utils
{
    public  static void hurtAndBreakItem(Player player, InteractionHand hand) { hurtAndBreakItem(player, hand, 1); }

    public static void hurtAndBreakItem(Player player, InteractionHand hand, int damage)
    {
        ItemStack item = player.getItemInHand(hand);
        item.hurtAndBreak(damage, player, (p) -> p.broadcastBreakEvent(hand));
    }
}
