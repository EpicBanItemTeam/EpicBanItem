package com.github.euonmyoji.epicbanitem.locale;

import com.github.euonmyoji.epicbanitem.configuration.ConfigFileManager;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tuple;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@SuppressWarnings("SpellCheckingInspection")
@Singleton
public class LocaleService {
    private static final String MISSING_MESSAGE_KEY = "epicbanitem.error.missingMessage";

    private ResourceBundle resourceBundle;
    private ResourceBundle buildInResource;

    private Map<String, TextTemplate> cache;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;
    private Path langPath;

    @Inject
    private ConfigFileManager fileManager;

    @Inject
    public LocaleService(AssetManager assetManager, PluginContainer pluginContainer, EventManager eventManager)
        throws IOException {
        this.cache = Maps.newConcurrentMap();

        Asset fallbackAsset = assetManager
            .getAsset(pluginContainer, "lang/" + Locale.getDefault().toString().toLowerCase() + ".lang")
            .orElse(assetManager.getAsset(pluginContainer, "lang/en_us.lang").orElseThrow(NoSuchFieldError::new));

        this.resourceBundle = buildInResource = new PropertyResourceBundle(new InputStreamReader(fallbackAsset.getUrl().openStream(), Charsets.UTF_8));

        cache.put(
            MISSING_MESSAGE_KEY,
            TextUtil.parseTextTemplate(
                getString(MISSING_MESSAGE_KEY).orElse("Missing language key: {message_key}"),
                Collections.singleton("message_key")
            )
        );

        eventManager.registerListener(pluginContainer, GamePreInitializationEvent.class, this::onPreInit);
    }

    public Optional<String> getString(String path) {
        Optional<String> stringOptional = Optional.empty();
        try {
            stringOptional = Optional.of(resourceBundle.getString(path));
        } catch (MissingResourceException ignore) {}
        return stringOptional;
    }

    @SafeVarargs
    public final Text getTextWithFallback(String path, Tuple<String, ?>... tuples) {
        return getText(path, Arrays.stream(tuples))
            .orElseGet(() -> getTextWithFallback(MISSING_MESSAGE_KEY, Tuple.of("message_key", path)).toBuilder().color(TextColors.RED).build());
    }

    public final Text getTextWithFallback(String path, Collection<Tuple<String, ?>> tuples) {
        return getText(path, tuples.stream())
                .orElseGet(() -> getTextWithFallback(MISSING_MESSAGE_KEY, Tuple.of("message_key", path)).toBuilder().color(TextColors.RED).build());
    }

    private Optional<Text> getText(String path, Stream<Tuple<String, ?>> tuples) {
        return getText(path, tuples.collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond)));
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

    private void onPreInit(GamePreInitializationEvent event) throws IOException {
        langPath = this.configDir.resolve("message.lang");
        Files.createDirectories(langPath.getParent());
        fileManager.getRootLoader().addListener(langPath, this::onLoad, this::onLoad, ()->{});
    }

    private void onLoad() throws IOException {
        if (!Files.exists(langPath)) Files.createFile(langPath);
        PropertyResourceBundle extraResourceBundle = new PropertyResourceBundle(Files.newBufferedReader(langPath, Charsets.UTF_8));
        extraResourceBundle.setParent(this.buildInResource);
        this.resourceBundle = extraResourceBundle;
        cache.clear();
    }


    private static final class PropertyResourceBundle extends java.util.PropertyResourceBundle {

        public PropertyResourceBundle(Reader reader) throws IOException {
            super(reader);
        }

        @Override
        public void setParent(ResourceBundle parent) {
            super.setParent(parent);
        }
    }
}
