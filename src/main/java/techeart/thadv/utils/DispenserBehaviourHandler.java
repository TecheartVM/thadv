package techeart.thadv.utils;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DispenserBehaviourHandler
{
    private static final Map<Supplier<ItemLike>, DispenseItemBehavior> REGISTERED = new HashMap<>();

    public static void registerBehaviour(Supplier<ItemLike> item, DispenseItemBehavior behavior) { REGISTERED.put(item, behavior); }

    public static void register() { REGISTERED.forEach((i, b) -> DispenserBlock.registerBehavior(i.get(), b)); }
}
