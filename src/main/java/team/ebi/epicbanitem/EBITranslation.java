/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventListenerRegistration;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.resource.pack.PackType;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.plugin.PluginContainer;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import team.ebi.epicbanitem.util.PropertyResourceBundle;

import static team.ebi.epicbanitem.EpicBanItem.NAMESPACE;

public final class EBITranslation {
    public TranslationRegistry registry;
    private final Path messagesDir;

    private final Map<Path, Properties> externalProperties = Maps.newHashMap();

    private final PluginContainer plugin;

    @Inject
    public EBITranslation(
            final @ConfigDir(sharedRoot = false) Path configDir,
            final PluginContainer plugin,
            final EventManager eventManager)
            throws IOException {
        this.messagesDir = configDir.resolve("messages");
        if (Files.notExists(this.messagesDir)) Files.createDirectories(this.messagesDir);
        this.plugin = plugin;
        eventManager.registerListener(EventListenerRegistration.builder(new TypeToken<StartingEngineEvent<Server>>() {})
                .listener(ignored -> {
                    loadMessages();
                    loadExternal();
                })
                .order(Order.DEFAULT)
                .plugin(plugin)
                .build());

        eventManager.registerListener(EventListenerRegistration.builder(RefreshGameEvent.class)
                .listener(ignored -> loadMessages())
                .order(Order.DEFAULT)
                .plugin(plugin)
                .build());
    }

    private void loadExternal() throws IOException {
        Files.walk(messagesDir, 1)
                .filter(it -> {
                    final var name = it.getFileName().toString();
                    return name.startsWith("messages_") && name.endsWith(".properties");
                })
                .forEach(it -> {
                    try {
                        final var properties = new Properties();
                        properties.load(Files.newInputStream(it));
                        externalProperties.put(it, properties);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void setExternal(String key, String value) {
        for (Properties properties : externalProperties.values()) properties.setProperty(key, value);
    }

    public void saveExternal() {
        externalProperties.forEach((path, properties) -> {
            try {
                properties.store(Files.newOutputStream(path), null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadMessages() {
        if (registry != null) GlobalTranslator.translator().removeSource(registry);
        registry = TranslationRegistry.create(EpicBanItem.key("translations"));
        try (final var pack = Sponge.server().packRepository().pack(plugin)) {
            final var contents = pack.contents();
            final var paths = contents.paths(
                    PackType.server(),
                    "plugin-" + NAMESPACE,
                    "assets/messages",
                    3,
                    name -> name.startsWith("assets/messages/messages_") && name.endsWith(".properties"));
            for (final var path : paths) {
                final var name = path.name();
                final var resource = contents.requireResource(PackType.server(), path);
                final var locale = name.substring(9, name.lastIndexOf(".properties"));
                var bundle = new PropertyResourceBundle(new InputStreamReader(resource.inputStream()));
                final var external = messagesDir.resolve(name);
                if (Files.notExists(external)) Files.createFile(external);
                try (final var reader = Files.newBufferedReader(external)) {
                    final var externalBundle = new PropertyResourceBundle(reader);
                    externalBundle.setParent(bundle);
                    bundle = externalBundle;
                }
                registry.registerAll(Locales.of(locale), bundle, false);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        GlobalTranslator.translator().addSource(registry);
    }
}
