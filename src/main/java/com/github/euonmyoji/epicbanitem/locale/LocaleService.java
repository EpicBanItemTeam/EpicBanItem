package com.github.euonmyoji.epicbanitem.locale;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.configuration.AutoFileLoader;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@SuppressWarnings("SpellCheckingInspection")
@Singleton
public class LocaleService {
    private static final String MISSING_MESSAGE_KEY = "epicbanitem.error.missingMessage";

    private ConfigurationNode node;

    private Map<String, TextTemplate> cache;

    @Inject
    private AutoFileLoader fileLoader;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    public LocaleService(AssetManager assetManager, PluginContainer pluginContainer, EventManager eventManager)
        throws IOException {
        this.cache = Maps.newConcurrentMap();

        Asset fallbackAsset = assetManager
            .getAsset(pluginContainer, "lang/" + Locale.getDefault().toString().toLowerCase() + ".lang")
            .orElse(assetManager.getAsset(pluginContainer, "lang/en_us.lang").orElseThrow(NoSuchFieldError::new));

        /* FIXME: 2020/2/17 Conflict with SpongeForge
            HoconConfigurationLoader configurationLoader = HoconConfigurationLoader
                .builder()
                .setURL(fallbackAsset.getUrl())
                .setParseOptions(ConfigParseOptions.defaults().setSyntax(ConfigSyntax.PROPERTIES))
                .build();
        */

        this.node = getConfigLoader(fallbackAsset).load();

        cache.put(
            MISSING_MESSAGE_KEY,
            TextUtil.parseTextTemplate(
                getString(MISSING_MESSAGE_KEY).orElse("Missing language key: {message_key}"),
                Collections.singleton("message_key")
            )
        );

        eventManager.registerListener(pluginContainer, GamePreInitializationEvent.class, this::onPreInit);
    }

    private ConfigurationLoader<CommentedConfigurationNode> getConfigLoader(Asset asset) {
        HoconConfigurationLoader.Builder builder = HoconConfigurationLoader.builder().setURL(asset.getUrl());

        try {
            for (Method setParseOptionsMethod : HoconConfigurationLoader.Builder.class.getMethods()) {
                if (Objects.equals("setParseOptions", setParseOptionsMethod.getName())) {
                    Class<?> parseOptionsClass = setParseOptionsMethod.getParameterTypes()[0];
                    Object defaultParseOptions = parseOptionsClass.getMethod("defaults").invoke(null);
                    for (Method setSyntaxMethod : parseOptionsClass.getMethods()) {
                        if (Objects.equals("setSyntax", setSyntaxMethod.getName())) {
                            Class<?> configSyntaxClass = setSyntaxMethod.getParameterTypes()[0];
                            builder =
                                (HoconConfigurationLoader.Builder) setParseOptionsMethod.invoke(
                                    builder,
                                    setSyntaxMethod.invoke(defaultParseOptions, configSyntaxClass.getEnumConstants()[2])
                                );
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            EpicBanItem.getLogger().error("Language files cannot be loaded", e);
            return builder.build();
        }

        return builder.build();
    }

    private ConfigurationNode getNode(String path) {
        ConfigurationNode node = this.node;
        for (String key : path.split("\\.")) {
            node = node.getNode(key);
        }
        return node;
    }

    public Optional<String> getString(String path) {
        return Optional.ofNullable(getNode(path).getString());
    }

    @SafeVarargs
    public final Text getMissableText(String path, Pair<String, ?>... pairs) {
        return getText(path, pairs)
            .orElseGet(() -> getMissableText(MISSING_MESSAGE_KEY, ImmutablePair.of("message_key", path)).toBuilder().color(TextColors.RED).build());
    }

    @SafeVarargs
    public final Optional<Text> getText(String path, Pair<String, ?>... pairs) {
        return getText(path, Arrays.stream(pairs).collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
    }

    public Optional<Text> getText(String path, Map<String, ?> params) {
        Optional<Text> textOptional = Optional.empty();
        if (!cache.containsKey(path)) {
            getString(path).ifPresent(value -> cache.put(path, TextUtil.parseTextTemplate(value, params.keySet())));
        }
        if (cache.containsKey(path)) {
            textOptional = Optional.of(cache.get(path).apply(params).build());
        }
        return textOptional;
    }

    @Deprecated
    public Text getMessage(String path, Map<String, ?> params) {
        return getText(path, params)
            .orElseGet(() -> getMissableText(MISSING_MESSAGE_KEY, ImmutablePair.of("message_key", path)).toBuilder().color(TextColors.RED).build());
    }

    @Deprecated
    public Text getMessage(String key) {
        return getMessage(key, Collections.emptyMap());
    }

    @Deprecated
    public Text getMessage(String key, String k1, Object v1) {
        return getMessage(key, ImmutableMap.of(k1, v1));
    }

    @Deprecated
    public Text getMessage(String key, String k1, Object v1, String k2, Object v2) {
        return getMessage(key, ImmutableMap.of(k1, v1, k2, v2));
    }

    @Deprecated
    public Text getMessage(String key, String k1, Object v1, String k2, Object v2, String k3, Object v3) {
        return getMessage(key, ImmutableMap.of(k1, v1, k2, v2, k3, v3));
    }

    @Deprecated
    public Text getMessage(String key, String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4) {
        return getMessage(key, ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    @Deprecated
    public Text getMessage(String key, String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5) {
        return getMessage(key, ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
    }

    private void onPreInit(GamePreInitializationEvent event) {
        this.fileLoader.addListener(
                this.configDir.resolve("message.lang"),
                thatNode -> {
                    this.node = thatNode.mergeValuesFrom(node);
                    this.cache.clear();
                    cache.put(
                        MISSING_MESSAGE_KEY,
                        TextUtil.parseTextTemplate(
                            getString(MISSING_MESSAGE_KEY).orElse("Missing language key: {message_key}"),
                            Collections.singleton("message_key")
                        )
                    );
                }
            );
    }
}
