package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.api.CheckRuleTrigger;

import java.util.Arrays;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public class Triggers {
    public static final Impl USE = new Impl("use");
    public static final Impl EQUIP = new Impl("equip");
    public static final Impl CRAFT = new Impl("craft");
    public static final Impl PICKUP = new Impl("pickup");
    public static final Impl CLICK = new Impl("click");
    public static final Impl THROW = new Impl("throw");
    public static final Impl DROP = new Impl("drop");
    public static final Impl PLACE = new Impl("place");
    public static final Impl BREAK = new Impl("break");
    public static final Impl INTERACT = new Impl("interact");

    private static final SortedMap<String, CheckRuleTrigger> triggers = initTriggerMap();

    private Triggers() {
        throw new UnsupportedOperationException();
    }

    private static SortedMap<String, CheckRuleTrigger> initTriggerMap() {
        SortedMap<String, CheckRuleTrigger> triggerMap = new TreeMap<>();
        for (Impl impl : Arrays.asList(USE, EQUIP, CRAFT, PICKUP, CLICK, THROW, DROP, PLACE, BREAK, INTERACT)) {
            triggerMap.put(impl.name, impl);
        }
        return triggerMap;
    }

    public static SortedMap<String, CheckRuleTrigger> getTriggers() {
        return triggers;
    }

    @Deprecated
    public static Set<String> getDefaultTriggers() {
        // TODO: change to #getTriggers instead
        return triggers.keySet();
    }

    public static final class Impl implements CheckRuleTrigger {
        private final String name;

        public Impl(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        @Override
        public boolean equals(Object that) {
            return this == that || that instanceof CheckRuleTrigger && this.name.equals(that.toString());
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
