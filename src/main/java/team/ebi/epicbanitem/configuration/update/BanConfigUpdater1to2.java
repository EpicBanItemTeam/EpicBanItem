package team.ebi.epicbanitem.configuration.update;

import com.google.inject.Singleton;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.Types;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.Updater;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.util.TextUtil;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author The EpicBanItem Team
 */
@NonnullByDefault
@Singleton
public class BanConfigUpdater1to2 implements Updater<ConfigurationNode> {
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9-_]+");

    @Override
    public int getInputVersion() {
        return 1;
    }

    @Override
    public int getOutputVersion() {
        return 2;
    }

    @Override
    public ConfigurationNode update(ConfigurationNode node) {

        Map<String, CheckRuleData> dataMap = new LinkedHashMap<>();

        for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.getNode("epicbanitem").getChildrenMap().entrySet()) {
            String index = entry.getKey().toString();
            for (ConfigurationNode checkRuleNode : entry.getValue().getChildrenList()) {
                // fix id
                if (!"*".equals(index)) {
                    ConfigurationNode queryIndex = checkRuleNode.getNode("query", "id");
                    if (queryIndex.isVirtual()) {
                        queryIndex.setValue(index);
                    }
                }
                // fix name
                ConfigurationNode nameNode = checkRuleNode.getNode("name");
                ConfigurationNode legacyNameNode = checkRuleNode.getNode("legacy-name");
                String name = nameNode.getValue(Types::asString, legacyNameNode.getString(""));
                if (!NAME_PATTERN.matcher(name).matches() || dataMap.containsKey(name)) {
                    String newName = findNewName(name, dataMap::containsKey);

                    legacyNameNode.setValue(name);
                    nameNode.setValue(newName);

                    String msg = "Find duplicate or illegal name, renamed \"{}\" in {} to \"{}\"";
                    EpicBanItem.getLogger().warn(msg, name, index, newName);
                }
                // add to rules
                CheckRuleData data = readV1Node(checkRuleNode);
                dataMap.put(data.name, data);
            }
        }

        //write
        ConfigurationNode result = SimpleConfigurationNode.root(node.getOptions());
        result.getNode("epicbanitem-version").setValue(getOutputVersion());
        for (CheckRuleData checkRule : dataMap.values()) {
            ConfigurationNode ruleNode = result.getNode("epicbanitem", checkRule.name);
            writeV2Node(ruleNode, checkRule);
        }

        return result;
    }


    private static String findNewName(@Nullable String name, Predicate<String> alreadyExists) {
        if (name == null) {
            name = "undefined-1";
        }
        name = name.toLowerCase();
        if (!NAME_PATTERN.matcher(name).matches()) {
            name = "unrecognized-1";
        }
        if (alreadyExists.test(name)) {
            int defNumber = 2;
            int dashIndex = name.lastIndexOf('-');
            int number = parseOrElse(name.substring(dashIndex + 1), defNumber);
            String prefix = dashIndex >= 0 ? name.substring(0, dashIndex) : name;
            for (name = prefix + '-' + number; alreadyExists.test(name); name = prefix + '-' + number) {
                ++number;
            }
        }
        return name;
    }

    private static int parseOrElse(String string, int orElse) {
        try {
            return Integer.parseUnsignedInt(string);
        } catch (NumberFormatException e) {
            return orElse;
        }
    }

    private static class CheckRuleData {
        private String name;
        private String legacyName;
        private int priority;
        private Tristate worldDefaultSetting;
        private Map<String, Boolean> worldSettings;
        private Tristate triggerDefaultSetting;
        private Map<String, Boolean> triggerSettings;
        private ConfigurationNode queryNode;
        @Nullable
        private ConfigurationNode updateNode;
        @Nullable
        private String customMessageString;

        public CheckRuleData(String name, String legacyName, int priority, Tristate worldDefaultSetting, Map<String, Boolean> worldSettings, Tristate triggerDefaultSetting, Map<String, Boolean> triggerSettings, ConfigurationNode queryNode, @Nullable ConfigurationNode updateNode, @Nullable String customMessageString) {
            this.name = name;
            this.legacyName = legacyName;
            this.priority = priority;
            this.worldDefaultSetting = worldDefaultSetting;
            this.worldSettings = worldSettings;
            this.triggerDefaultSetting = triggerDefaultSetting;
            this.triggerSettings = triggerSettings;
            this.queryNode = queryNode;
            this.updateNode = updateNode;
            this.customMessageString = customMessageString;
        }
    }

    private CheckRuleData readV1Node(ConfigurationNode node) {
        String name = Objects.requireNonNull(node.getNode("name").getString());
        String legacyName = node.getNode("legacy-name").getString("");
        int priority = node.getNode("priority").getInt(5);
        Map<String, Boolean> worldSettings = new HashMap<>();
        Tristate worldDefaultSetting = Tristate.UNDEFINED;
        if (node.getNode("enabled-worlds").hasListChildren()) {
            node.getNode("enabled-worlds").getChildrenList().stream()
                    .map(ConfigurationNode::getString)
                    .forEach(s -> worldSettings.put(s, true));
            worldDefaultSetting = Tristate.FALSE;
        } else {
            node.getNode("enabled-worlds").getChildrenMap().forEach((k, v) -> worldSettings.put(k.toString(), v.getBoolean()));
            if (!node.getNode("world-default-setting").isVirtual()) {
                worldDefaultSetting = Tristate.fromBoolean(node.getNode("world-default-setting").getBoolean());
            }
        }
        Map<String, Boolean> enableTriggers = new HashMap<>();
        ConfigurationNode triggerNode = node.getNode("use-trigger");
        triggerNode
            .getChildrenMap()
            .forEach(
                (k, v) -> {
                    String key = k.toString();
                    String trigger;
                    if (key.indexOf(':') == -1) {
                        trigger = "epicbanitem:" + key;
                    } else {
                        trigger = key;
                    }
                    enableTriggers.put(trigger, v.getBoolean());
                }
            );
        Tristate triggerDefaultSetting;
        if (!node.getNode("trigger-default-setting").isVirtual()) {
            triggerDefaultSetting = Tristate.fromBoolean(node.getNode("trigger-default-setting").getBoolean());
        } else {
            triggerDefaultSetting = Tristate.UNDEFINED;
        }
        ConfigurationNode queryNode = node.getNode("query");
        ConfigurationNode updateNode = node.getNode("update");
        if (updateNode.isVirtual() && node.getNode("remove").getBoolean(false)) {
            try {
                updateNode = TextUtil.serializeStringToConfigNode("{\"$set\": {id: \"minecraft:air\", Damage: 0}}");
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        String customMessageString = node.getNode("custom-message").getString();
        return new CheckRuleData(name, legacyName, priority, worldDefaultSetting, worldSettings, triggerDefaultSetting, enableTriggers, queryNode, updateNode.isVirtual() ? null : updateNode, customMessageString);
    }

    private void writeV2Node(ConfigurationNode node, CheckRuleData data) {
        if (!data.legacyName.isEmpty()) {
            node.getNode("legacy-name").setValue(data.legacyName);
        }
        node.getNode("priority").setValue(data.priority);
        node.getNode("world-default-setting").setValue(data.worldDefaultSetting.name());
        data.worldSettings.forEach((k, v) -> node.getNode("enabled-worlds", k).setValue(v));
        node.getNode("trigger-default-setting").setValue(data.triggerDefaultSetting.name());
        data.triggerSettings.forEach((k, v) -> node.getNode("use-trigger", k).setValue(v));
        node.getNode("query").setValue(data.queryNode);
        node.getNode("update").setValue(data.updateNode);
        node.getNode("custom-message").setValue(data.customMessageString);
    }
}
