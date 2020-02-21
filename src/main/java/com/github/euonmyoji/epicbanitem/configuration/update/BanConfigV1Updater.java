package com.github.euonmyoji.epicbanitem.configuration.update;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.api.CheckRuleTrigger;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleIndex;
import com.github.euonmyoji.epicbanitem.configuration.BanConfig;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.Types;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@Singleton
public class BanConfigV1Updater implements IConfigUpdater {

    @Inject
    private Logger logger;

    @Inject
    private BanConfig banConfig;
    @Inject
    @ConfigDir(sharedRoot = false)
    Path configDir;

    @Override
    public int getTargetVersion() {
        return 2;
    }

    @Override
    public boolean canAccept(int version) {
        return version == 1;
    }

    @Override
    public void doUpdate(Path configPath) throws IOException {
        Path backupDir = configDir.resolve("backup");
        String baseName = "banitem_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmm"));
        int i = 0;
        Path newPath = backupDir.resolve(baseName + ".conf");
        while (Files.exists(newPath)) {
            newPath = backupDir.resolve(baseName + "_" + i++ + ".conf");
            if (i > 10) {
                throw new IllegalStateException("Unable to find a place for backup " + configPath);
            }
        }
        logger.warn("Move old config {} to {}", configPath, newPath);
        Files.createDirectories(backupDir);
        Files.move(configPath, newPath);

        TypeSerializerCollection serializers = TypeSerializers.getDefaultSerializers().newChild();
        serializers.registerType(TypeToken.of(CheckRule.class), new Serializer());
        ConfigurationOptions options = ConfigurationOptions.defaults().setSerializers(serializers);
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(newPath).build();

        ConfigurationNode node = loader.load(options);
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.getNode("epicbanitem").getChildrenMap().entrySet()) {
            CheckRuleIndex index = CheckRuleIndex.of(entry.getKey().toString());
            for (ConfigurationNode checkRuleNode : entry.getValue().getChildrenList()) {
                // fix id
                if (!index.isWildcard()) {
                    ConfigurationNode queryIndex = checkRuleNode.getNode("query", "id");
                    if (queryIndex.isVirtual()) {
                        queryIndex.setValue(index.toString());
                    }
                }
                // fix name
                ConfigurationNode nameNode = checkRuleNode.getNode("name");
                ConfigurationNode legacyNameNode = checkRuleNode.getNode("legacy-name");
                String name = nameNode.getValue(Types::asString, legacyNameNode.getString(""));
                if (!CheckRule.NAME_PATTERN.matcher(name).matches() || banConfig.getRule(name).isPresent()) {
                    String newName = findNewName(name, s -> banConfig.getRule(s).isPresent());

                    legacyNameNode.setValue(name);
                    nameNode.setValue(newName);

                    String msg = "Find duplicate or illegal name, renamed \"{}\" in {} to \"{}\"";
                    logger.warn(msg, name, index, newName);
                }
                // add to rules
                try {
                    CheckRule rule = Objects.requireNonNull(checkRuleNode.getValue(TypeToken.of(CheckRule.class)));
                    banConfig.addRule(rule);
                } catch (ObjectMappingException e) {
                    throw new IOException(e);
                }
            }
        }
    }

    private static String findNewName(@Nullable String name, Predicate<String> alreadyExists) {
        if (name == null) {
            name = "undefined-1";
        }
        name = name.toLowerCase();
        if (!CheckRule.NAME_PATTERN.matcher(name).matches()) {
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

    public static class Serializer implements TypeSerializer<CheckRule> {

        @Override
        public CheckRule deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
            String name = Objects.requireNonNull(node.getNode("name").getString());
            String legacyName = node.getNode("legacy-name").getString("");
            int priority = node.getNode("priority").getInt(5);
            Map<String, Boolean> enableWorld = new HashMap<>();
            Tristate worldDefaultSetting = Tristate.UNDEFINED;
            if (node.getNode("enabled-worlds").hasListChildren()) {
                node.getNode("enabled-worlds").getList(TypeToken.of(String.class)).forEach(s -> enableWorld.put(s, true));
                worldDefaultSetting = Tristate.FALSE;
            } else {
                node.getNode("enabled-worlds").getChildrenMap().forEach((k, v) -> enableWorld.put(k.toString(), v.getBoolean()));
                if (!node.getNode("world-default-setting").isVirtual()) {
                    worldDefaultSetting = Tristate.fromBoolean(node.getNode("world-default-setting").getBoolean());
                }
            }
            Map<CheckRuleTrigger, Boolean> enableTriggers = new HashMap<>();
            ConfigurationNode triggerNode = node.getNode("use-trigger");
            triggerNode
                .getChildrenMap()
                .forEach(
                    (k, v) -> {
                        String key = k.toString();
                        Optional<CheckRuleTrigger> optionalTrigger;
                        if (key.indexOf(':') == -1) {
                            optionalTrigger = Sponge.getRegistry().getType(CheckRuleTrigger.class, EpicBanItem.PLUGIN_ID + ":" + key);
                        } else {
                            optionalTrigger = Sponge.getRegistry().getType(CheckRuleTrigger.class, key);
                        }
                        if (!optionalTrigger.isPresent()) {
                            EpicBanItem.getLogger().warn("Find unknown trigger {} at check rule {}, it will be ignored.", key, name);
                        } else {
                            enableTriggers.put(optionalTrigger.get(), v.getBoolean());
                        }
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
            return CheckRule
                .builder()
                .name(name)
                .legacyName(legacyName)
                .queryNode(queryNode)
                .updateNode(updateNode.isVirtual() ? null : updateNode)
                .priority(priority)
                .worldDefaultSetting(worldDefaultSetting)
                .enableWorlds(enableWorld)
                .triggerDefaultSetting(triggerDefaultSetting)
                .enableTriggers(enableTriggers)
                .customMessage(customMessageString)
                .build();
        }

        @Override
        public void serialize(TypeToken<?> type, @Nullable CheckRule rule, ConfigurationNode node) {
            //do not need
        }
    }
}
