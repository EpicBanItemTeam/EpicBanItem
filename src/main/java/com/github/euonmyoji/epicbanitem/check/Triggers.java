package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.api.CheckRuleTrigger;
import java.util.*;
import org.spongepowered.api.text.Text;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

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
    public static final Impl JOIN = new Impl("join");

    private static final SortedMap<String, CheckRuleTrigger> triggers = initTriggerMap();

    private Triggers() {
        throw new UnsupportedOperationException();
    }

    private static SortedMap<String, CheckRuleTrigger> initTriggerMap() {
        SortedMap<String, CheckRuleTrigger> triggerMap = new TreeMap<>();
        for (Impl impl : Arrays.asList(USE, EQUIP, CRAFT, PICKUP, CLICK, THROW, DROP, PLACE, BREAK, INTERACT, JOIN)) {
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

    @NonnullByDefault
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

        @Override
        public Text toText() {
            return EpicBanItem.getMessages().getMessage("epicbanitem.triggers." + toString().toLowerCase(Locale.ROOT));
        }
    }
}
