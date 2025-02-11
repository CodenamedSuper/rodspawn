package com.codenamed.rodspawn.registry;

import com.google.common.collect.Sets;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RodspawnItemAbilities {

    public static final ItemAbility SPEAR_FLAMETHROW = ItemAbility.get("spear_flamethrow");
    public static final Set<ItemAbility> DEFAULT_SPEAR_ACTIONS;


    private static Set<ItemAbility> of(ItemAbility... actions) {
        return (Set) Stream.of(actions).collect(Collectors.toCollection(Sets::newIdentityHashSet));
    }

    public static void init() {

    }

    static {
        DEFAULT_SPEAR_ACTIONS = of(SPEAR_FLAMETHROW);
    }
}
