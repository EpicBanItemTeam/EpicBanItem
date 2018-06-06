package com.github.euonmyoji.epicbanitem.message;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("WeakerAccess")
/**
 * @author EpicBanItem Authors
 */
public class Messages {
    //todo:
    private TextTemplate MISSING;

    private final EpicBanItem plugin;
    private final Path messagePath;
    private ResourceBundle res;

    private Map<String, TextTemplate> cache = new HashMap<>();

    public Messages(EpicBanItem plugin, Path configDir) {
        this.plugin = plugin;
        //todo:语言文件叫什么名字
        this.messagePath = configDir.resolve("");
    }

    public void load() throws IOException {
        AssetManager assetManager = Sponge.getAssetManager();
        assetManager.getAsset(plugin, "lang/" + Locale.getDefault().toString().toLowerCase() + ".lang").orElse(
                assetManager.getAsset(plugin, "lang/en_us.lang").get()).copyToFile(messagePath, false);
        res = new PropertyResourceBundle(new InputStreamReader(Files.newInputStream(messagePath), Charsets.UTF_8));
    }

    public Text getMessage(String key, Map<String, ?> params) {
        if (!cache.containsKey(key)) {
            //todo:MissingResourceException
            String rawString = res.getString(key);
            cache.put(key, parseTextTemplate(rawString, params.keySet()));
        }
        return cache.getOrDefault(key, MISSING).apply(params).build();
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

    private static TextTemplate parseTextTemplate(String origin, Set<String> keySet) {
        boolean checkFirst = origin.startsWith("{");
        List<Object> objects = new ArrayList<>();
        String[] subStrings = origin.split("\\{");
        for (int i = 0; i < subStrings.length; i++) {
            String subString = subStrings[i];
            if (i == 0 && !checkFirst) {
                objects.add(parseFormatText(subString));
                continue;
            }
            String[] muSub = subString.split("}");
            if (muSub.length == 1 && subString.endsWith("}") && keySet.contains(muSub[0])) {
                objects.add(TextTemplate.arg(muSub[0]));
            } else if (muSub.length > 1 && keySet.contains(muSub[0])) {
                objects.add(TextTemplate.arg(muSub[0]));
                StringBuilder left = new StringBuilder(muSub[1]);
                for (int j = 2; j < muSub.length; j++) {
                    left.append("}");
                    left.append(muSub[j]);
                }
                if (subString.endsWith("}")) {
                    left.append("}");
                }
                objects.add(parseFormatText(left.toString()));
            } else {
                objects.add(parseFormatText("{" + subString));
            }
        }
        return TextTemplate.of(objects.toArray());
    }

    public static Text parseFormatText(String in) {
        return TextSerializers.FORMATTING_CODE.deserializeUnchecked(in);
    }
}
