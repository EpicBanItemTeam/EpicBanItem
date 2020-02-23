package team.ebi.epicbanitem.check;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public final class CheckRuleIndex implements Comparable<CheckRuleIndex> {
    private static final DataQuery ID = DataQuery.of("id");

    private final String name;

    private CheckRuleIndex(String name) {
        int colonIndex = name.indexOf(':');
        this.name = colonIndex < 0 || !Sponge.getPluginManager().isLoaded(name.substring(0, colonIndex)) ? "*" : name;
    }

    private CheckRuleIndex(ItemType type) {
        this.name = ItemTypes.AIR.equals(type) || ItemTypes.NONE.equals(type) ? "minecraft:air" : type.getId();
    }

    public static CheckRuleIndex of() {
        return new CheckRuleIndex("*");
    }

    public static CheckRuleIndex of(String name) {
        return new CheckRuleIndex(name);
    }

    public static CheckRuleIndex of(ItemType itemType) {
        return new CheckRuleIndex(itemType);
    }

    public static CheckRuleIndex of(ItemStack itemStack) {
        return new CheckRuleIndex(itemStack.getType());
    }

    public static CheckRuleIndex of(ItemStackSnapshot snapshot) {
        return new CheckRuleIndex(snapshot.getType());
    }

    public static CheckRuleIndex of(ConfigurationNode node) {
        return new CheckRuleIndex(node.getNode("id").getString("*"));
    }

    public static CheckRuleIndex of(DataView view) {
        return new CheckRuleIndex(view.getString(ID).orElse("*"));
    }

    public boolean isWildcard() {
        return "*".equals(this.name);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return this == that || that instanceof CheckRuleIndex && this.name.equals(((CheckRuleIndex) that).name);
    }

    @Override
    public int compareTo(CheckRuleIndex that) {
        return this.name.compareTo(that.name);
    }
}
