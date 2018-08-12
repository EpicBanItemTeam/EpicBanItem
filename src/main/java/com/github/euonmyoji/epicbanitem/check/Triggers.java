package com.github.euonmyoji.epicbanitem.check;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * @author yinyangshi
 * 都有什么触发的类
 */
public class Triggers {
    public static final String USE = "use";
    public static final String PICKUP = "pickup";
    public static final String CLICK = "click";
    public static final String THROW = "throw";
    public static final String DROP = "drop";

    private Triggers() {
        throw new UnsupportedOperationException();
    }

    public static Set<String> getDefaultTriggers() {
        return ImmutableSet.of(USE, PICKUP, CLICK, THROW, DROP);
    }
}
