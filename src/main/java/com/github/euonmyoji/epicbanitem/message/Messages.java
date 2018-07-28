package com.github.euonmyoji.epicbanitem.message;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author EpicBanItem Team
 */
@SuppressWarnings("WeakerAccess")
public class Messages {
    private static final String MISSING_MESSAGE_KEY = "epicbanitem.error.missingMessage";

    private final EpicBanItem plugin;
    private final Path messagePath;
    private ResourceBundle res;

    private Map<String, TextTemplate> cache = new HashMap<>();

    public Messages(EpicBanItem plugin, Path configDir) {
        this.plugin = plugin;
        this.messagePath = configDir.resolve("message.lang");
    }

    public void load() throws IOException {
        AssetManager assetManager = Sponge.getAssetManager();
        Files.createDirectories(messagePath.getParent());
        assetManager.getAsset(plugin, "lang/" + Locale.getDefault().toString().toLowerCase() + ".lang").orElse(
                assetManager.getAsset(plugin, "lang/en_us.lang").get()).copyToFile(messagePath, false);
        res = new PropertyResourceBundle(new InputStreamReader(Files.newInputStream(messagePath), Charsets.UTF_8));

        String rawString;
        if(res.containsKey(MISSING_MESSAGE_KEY)){
            rawString = res.getString(MISSING_MESSAGE_KEY);
        }else {
            rawString = "Missing Message of {message_key}";
            EpicBanItem.logger.warn("Missing message for key:" + MISSING_MESSAGE_KEY);
        }
        cache.put(MISSING_MESSAGE_KEY,TextUtil.parseTextTemplate(rawString,Collections.singleton("message_key")));
    }

    public Text getMessage(String key, Map<String, ?> params) {
        if (!cache.containsKey(key)) {
            if (res.containsKey(key)) {
                String rawString = res.getString(key);
                cache.put(key, TextUtil.parseTextTemplate(rawString, params.keySet()));
            } else {
                EpicBanItem.logger.warn("Missing message for key:" + key);
            }
        }
        if (cache.containsKey(key)) {
            return cache.get(key).apply(params).build();
        } else {
            return getMessage("epicbanitem.error.missingMessage","message_key",key);
        }
    }

    public Text getMessage(String key) {
        //如果文本不包含变量 也缓存TextTemplate么
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
