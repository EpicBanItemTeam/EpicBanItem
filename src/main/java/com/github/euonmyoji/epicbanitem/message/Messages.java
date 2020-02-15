package com.github.euonmyoji.epicbanitem.message;

import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@Singleton
public class Messages {
    private static final String MISSING_MESSAGE_KEY = "epicbanitem.error.missingMessage";

    @Inject
    private PluginContainer pluginContainer;

    @Inject
    private Logger logger;

    private final Path messagePath;

    @Nullable
    private ResourceBundle res;

    private ResourceBundle fallbackRes;

    private Map<String, TextTemplate> cache = new ConcurrentHashMap<>();

    @Inject
    public Messages(@ConfigDir(sharedRoot = false) Path configDir, PluginContainer pluginContainer, EventManager eventManager) {
        this.messagePath = configDir.resolve("message.lang");

        eventManager.registerListeners(pluginContainer, this);
    }

    public void load() throws IOException {
        AssetManager assetManager = Sponge.getAssetManager();
        Files.createDirectories(messagePath.getParent());
        Asset fallback = assetManager
            .getAsset(pluginContainer, "lang/" + Locale.getDefault().toString().toLowerCase() + ".lang")
            .orElse(assetManager.getAsset(pluginContainer, "lang/en_us.lang").orElseThrow(NoSuchFieldError::new));
        //        fallback.copyToFile(messagePath, false); ??? What's this
        if (Files.exists(messagePath)) {
            res = new PropertyResourceBundle(new InputStreamReader(Files.newInputStream(messagePath), Charsets.UTF_8));
        }
        fallbackRes = new PropertyResourceBundle(new InputStreamReader(fallback.getUrl().openStream(), Charsets.UTF_8));

        String rawString = getRawString(MISSING_MESSAGE_KEY);
        if (rawString == null) {
            rawString = "Missing Message of {message_key}";
        }
        cache.put(MISSING_MESSAGE_KEY, TextUtil.parseTextTemplate(rawString, Collections.singleton("message_key")));
    }

    @Nullable
    private String getRawString(String key) {
        if (res != null && res.containsKey(key)) {
            return res.getString(key);
        }
        if (fallbackRes.containsKey(key)) {
            return fallbackRes.getString(key);
        }
        logger.warn("Missing message for key:" + key);
        return null;
    }

    public Text getMessage(String key, Map<String, ?> params) {
        if (!cache.containsKey(key)) {
            String rawString = getRawString(key);
            if (rawString != null) {
                cache.put(key, TextUtil.parseTextTemplate(rawString, params.keySet()));
            }
        }
        if (cache.containsKey(key)) {
            return cache.get(key).apply(params).build();
        } else {
            return getMessage(MISSING_MESSAGE_KEY, "message_key", key).toBuilder().color(TextColors.RED).build();
        }
    }

    public Text getMessage(String key) {
        return getMessage(key, Collections.emptyMap());
    }

    public Text getMessage(String key, String k1, Object v1) {
        return getMessage(key, ImmutableMap.of(k1, v1));
    }

    public Text getMessage(String key, String k1, Object v1, String k2, Object v2) {
        return getMessage(key, ImmutableMap.of(k1, v1, k2, v2));
    }

    public Text getMessage(String key, String k1, Object v1, String k2, Object v2, String k3, Object v3) {
        return getMessage(key, ImmutableMap.of(k1, v1, k2, v2, k3, v3));
    }

    public Text getMessage(String key, String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4) {
        return getMessage(key, ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    public Text getMessage(String key, String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5) {
        return getMessage(key, ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        try {
            this.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load EpicBanItem", e);
        }
    }
}
