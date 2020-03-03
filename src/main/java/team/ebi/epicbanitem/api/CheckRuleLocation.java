package team.ebi.epicbanitem.api;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author The EpicBanItem Team
 */
public final class CheckRuleLocation implements Comparable<CheckRuleLocation> {
    private static final String NAME_PATTERN_STRING = "(?:(?<group>[a-z0-9-_]+)\\.)?(?<name>[a-z0-9-_]+)";

    private final String group;
    private final String name;

    private CheckRuleLocation(String group, String name) {
        this.group = group;
        this.name = name;
    }

    public static final Pattern NAME_PATTERN = Pattern.compile(NAME_PATTERN_STRING);

    public static CheckRuleLocation of(String nameWithGroup) {
        Matcher nameMatcher = NAME_PATTERN.matcher(Preconditions.checkNotNull(nameWithGroup));
        Preconditions.checkArgument(nameMatcher.matches(), "rule name should match " + NAME_PATTERN_STRING);
        return new CheckRuleLocation(Strings.nullToEmpty(nameMatcher.group("group")), nameMatcher.group("name"));
    }

    public static CheckRuleLocation of(String group, String name) {
        Preconditions.checkNotNull(name, "name should not be null");
        Preconditions.checkNotNull(group, "group should not be null");

        CheckRuleLocation location = CheckRuleLocation.of(group + "." + name);
        boolean areRuleNameTheSame = location.group.equals(group) && location.name.equals(name);
        Preconditions.checkArgument(areRuleNameTheSame, "rule name should match " + NAME_PATTERN_STRING);

        return location;
    }

    @Override
    public int compareTo(CheckRuleLocation that) {
        // ATTENTION: "alpha.beta" is between "alpha" and "beta"
        return this.toString().compareTo(that.toString());
    }

    @Override
    public String toString() {
        return this.group.isEmpty() ? this.name : this.group + "." + this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof CheckRuleLocation) {
            CheckRuleLocation that = (CheckRuleLocation) obj;
            return this.group.equals(that.group) && this.name.equals(that.name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.group, this.name);
    }
}
