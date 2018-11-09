package com.github.euonmyoji.epicbanitem.message;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@SuppressWarnings("WeakerAccess")
public class Messages {
    private static final String MISSING_MESSAGE_KEY = "epicbanitem.error.missingMessage";

    private final EpicBanItem plugin;
    private final Path messagePath;
    @Nullable
    private ResourceBundle res;
    private ResourceBundle fallbackRes;

    private Map<String, TextTemplate> cache = new ConcurrentHashMap<>();

    public Messages(EpicBanItem plugin, Path configDir) {
        this.plugin = plugin;
        this.messagePath = configDir.resolve("message.lang");
    }

    public void load() throws IOException {
        AssetManager assetManager = Sponge.getAssetManager();
        Files.createDirectories(messagePath.getParent());
        Asset fallback = assetManager.getAsset(plugin, "lang/" + Locale.getDefault().toString().toLowerCase() + ".lang").orElse(
                assetManager.getAsset(plugin, "lang/en_us.lang").orElseThrow(NoSuchFieldError::new));
//        fallback.copyToFile(messagePath, false);
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
        EpicBanItem.getLogger().warn("Missing message for key:" + key);
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
        // TODO: Should we also cache TextTemplate?
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

}
