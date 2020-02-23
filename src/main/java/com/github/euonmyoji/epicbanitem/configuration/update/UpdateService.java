package com.github.euonmyoji.epicbanitem.configuration.update;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.util.Updater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class UpdateService {
    public static FileType<ConfigurationNode> BAN_CONF = new FileType<>("BAN_CONF", TypeToken.of(ConfigurationNode.class));
    public static FileType<ConfigurationNode> SETTINGS = new FileType<>("SETTINGS", TypeToken.of(ConfigurationNode.class));

    private Map<FileType<?>, List<Updater<?>>> updaterMap = new HashMap<>();

    private Injector injector;

    @Inject
    private UpdateService(Injector injector) {
        this.injector = injector;
        addUpdaters();
    }

    public void addUpdaters() {
        addUpdater(BAN_CONF, injector.getInstance(BanConfigUpdater1to2.class));
    }

    public <T> void addUpdater(FileType<T> type, Updater<T> updater) {
        updaterMap.computeIfAbsent(type, t -> new ArrayList<>()).add(updater);
    }

    public <T> T update(FileType<T> type, T o, int fromVersion, int toVersion) {
        //noinspection unchecked
        List<Updater<T>> updaters = (List<Updater<T>>)(Object)(updaterMap.getOrDefault(type, Collections.emptyList()));
        List<Updater<T>> list = new ArrayList<>();
        int version = fromVersion;
        for (Updater<T> updater : updaters) {
            if (updater.getInputVersion() == version) {
                if (updater.getOutputVersion() > toVersion) {
                    continue;
                }
                version = updater.getOutputVersion();
                list.add(updater);
            }
            if (version == toVersion) {
                break;
            }
        }
        if (version != toVersion) {
            throw new RuntimeException("Unable to update " + type +" from " + fromVersion + " to " + toVersion);
        }
        return list.stream().reduce(o, (t, updater) -> updater.update(t), (a, b) -> b);
    }

    public static class FileType<T> {
        private String name;
        private TypeToken<T> typeToken;

        private FileType(String name, TypeToken<T> typeToken) {
            this.name = name;
            this.typeToken = typeToken;
        }

        @Override
        public String toString() {
            return "FileType(" + name + ")<" + typeToken + ">";
        }
    }
}
