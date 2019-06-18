package com.github.euonmyoji.epicbanitem.check;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import java.util.Set;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public class Triggers {
    public static final String USE = "use";
    public static final String EQUIP = "equip";
    public static final String CRAFT = "craft";
    public static final String PICKUP = "pickup";
    public static final String CLICK = "click";
    public static final String THROW = "throw";
    public static final String DROP = "drop";
    public static final String PLACE = "place";
    public static final String BREAK = "break";
    public static final String INTERACT = "interact";

    private Triggers() {
        throw new UnsupportedOperationException();
    }

    public static Set<String> getDefaultTriggers() {
        return ImmutableSortedSet.of(USE, EQUIP, CRAFT, PICKUP, CLICK, THROW, DROP, PLACE, BREAK, INTERACT);
    }
}
