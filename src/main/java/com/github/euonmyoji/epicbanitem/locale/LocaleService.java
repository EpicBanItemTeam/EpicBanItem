package com.github.euonmyoji.epicbanitem.locale;

import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.github.euonmyoji.epicbanitem.util.file.ObservableFileService;
import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import org.slf4j.Logger;
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
    private static final String MESSAGE_FILE_NAME = "message.lang";

    private ResourceBundle resourceBundle;

    private Map<String, TextTemplate> cache;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    private ObservableFileService fileService;

    @Inject
    private Logger logger;

    @Inject
    public LocaleService(AssetManager assetManager, PluginContainer pluginContainer, EventManager eventManager)
        throws IOException {
        this.cache = Maps.newConcurrentMap();

        Asset fallbackAsset = assetManager
            .getAsset(pluginContainer, "lang/" + Locale.getDefault().toString().toLowerCase() + ".lang")
            .orElse(assetManager.getAsset(pluginContainer, "lang/en_us.lang").orElseThrow(NoSuchFieldError::new));

        this.resourceBundle = new PropertyResourceBundle(new InputStreamReader(fallbackAsset.getUrl().openStream(), Charsets.UTF_8));

        cache.put(
            MISSING_MESSAGE_KEY,
            TextUtil.parseTextTemplate(
                getString(MISSING_MESSAGE_KEY).orElse("Missing language key: {message_key}"),
                Collections.singleton("message_key")
            )
        );

        eventManager.registerListener(pluginContainer, GamePreInitializationEvent.class, this::onPreInit);
    }

    @SafeVarargs
    public final String getStringWithFallback(String path, Tuple<String, ?>... tuples) {
        return getTextWithFallback(path, tuples).toPlain();
    }

    @SafeVarargs
    public final Optional<String> getString(String path, Tuple<String, ?>... tuples) {
        return getText(path, tuples).map(Text::toPlain);
    }

    public Optional<String> getString(String path) {
        Optional<String> stringOptional = Optional.empty();
        try {
            stringOptional = Optional.of(resourceBundle.getString(path));
        } catch (MissingResourceException ignore) {}
        return stringOptional;
    }

    @SuppressWarnings("unchecked")
    public final Text getTextWithFallback(String path, Iterable<Tuple<String, ?>> tuples) {
        return this.getTextWithFallback(path, Iterables.toArray(tuples, Tuple.class));
    }

    @SafeVarargs
    public final Text getTextWithFallback(String path, Tuple<String, ?>... tuples) {
        return getText(path, tuples)
            .orElseGet(() -> getTextWithFallback(MISSING_MESSAGE_KEY, Tuple.of("message_key", path)).toBuilder().color(TextColors.RED).build());
    }

    @SafeVarargs
    public final Optional<Text> getText(String path, Tuple<String, ?>... tuples) {
        return getText(path, Arrays.stream(tuples).collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond)));
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
        fileService.register(
            ObservableLocaleFile
                .builder()
                .path(this.configDir.resolve(MESSAGE_FILE_NAME))
                .updateConsumer(
                    reader -> {
                        try {
                            PropertyResourceBundle extraResourceBundle = new PropertyResourceBundle(reader);
                            extraResourceBundle.setParent(this.resourceBundle);
                            this.resourceBundle = extraResourceBundle;
                            this.cache.clear();
                            cache.put(
                                MISSING_MESSAGE_KEY,
                                TextUtil.parseTextTemplate(
                                    getString(MISSING_MESSAGE_KEY).orElse("Missing language key: {message_key}"),
                                    Collections.singleton("message_key")
                                )
                            );
                            getString("epicbanitem.info.reload", Tuple.of("name", MESSAGE_FILE_NAME)).ifPresent(logger::info);
                        } catch (IOException e) {
                            logger.error(getStringWithFallback("epicbanitem.info.cannotBeLoaded", Tuple.of("name", MESSAGE_FILE_NAME)), e);
                        }
                    }
                )
                .build()
        );
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
