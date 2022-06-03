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
import java.util.Objects;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.resource.pack.PackType;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import team.ebi.epicbanitem.util.PropertyResourceBundle;

/**
 * @author The EpicBanItem Team
 */
@Plugin(EpicBanItem.NAMESPACE)
public class EpicBanItem {

    public static final String NAMESPACE = "epicbanitem";

    public static TranslationRegistry translations;

    private final PluginContainer plugin;

    private final Injector injector;

    private final Path messagesDir;

    @Inject
    EpicBanItem(
            final EBIRegistries registries,
            final PluginContainer plugin,
            final Injector injector,
            final @ConfigDir(sharedRoot = false) Path configDir)
            throws IOException {
        this.plugin = plugin;
        this.injector = injector;
        this.messagesDir = configDir.resolve("messages");
        Objects.requireNonNull(registries);
        if (Files.notExists(this.messagesDir)) Files.createDirectories(this.messagesDir);
    }

    public static ResourceKey key(String value) {
        return ResourceKey.of(NAMESPACE, value);
    }

    public static String permission(String permission) {
        return NAMESPACE + "." + permission;
    }

    @Listener
    public void onRegisterCommand(RegisterCommandEvent<Command.Parameterized> event) {
        event.register(plugin, injector.getInstance(EBICommands.class).buildCommand(), NAMESPACE, "ebi");
    }

    private void loadMessages() {
        if (translations != null) GlobalTranslator.translator().removeSource(translations);
        translations = TranslationRegistry.create(EpicBanItem.key("translations"));
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
                translations.registerAll(Locales.of(locale), bundle, false);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        GlobalTranslator.translator().addSource(translations);
    }

    @Listener
    public void onStartingEngine(final StartingEngineEvent<Server> event) {
        loadMessages();
    }

    @Listener
    public void onRefreshGame(RefreshGameEvent event) {
        loadMessages();
    }
}
