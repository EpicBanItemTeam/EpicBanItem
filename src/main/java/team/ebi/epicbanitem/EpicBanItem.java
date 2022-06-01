/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Objects;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.resource.Resource;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.pack.Pack;
import org.spongepowered.api.resource.pack.PackContents;
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

    @Inject
    private PluginContainer plugin;

    @Inject
    private Injector injector;

    @SuppressWarnings("SpongeInjection")
    @Inject
    EpicBanItem(EBIServices services, EBIRegistries registries) {
        Objects.requireNonNull(services);
        Objects.requireNonNull(registries);
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

    @Listener
    public void onStartingEngine(final StartingEngineEvent<Server> event) {
        // TODO read external messages files
        translations = new EBITranslationRegistry();
        try (Pack pack = Sponge.server().packRepository().pack(plugin)) {
            PackContents contents = pack.contents();
            Collection<ResourcePath> paths = contents.paths(
                    PackType.server(),
                    "plugin-" + NAMESPACE,
                    "assets/messages",
                    3,
                    name -> name.startsWith("assets/messages/messages_") && name.endsWith(".properties"));
            for (ResourcePath path : paths) {
                String name = path.name();
                Resource resource = contents.requireResource(PackType.server(), path);
                String locale = name.substring(9, name.lastIndexOf(".properties"));
                PropertyResourceBundle bundle =
                        new PropertyResourceBundle(new InputStreamReader(resource.inputStream()));
                translations.registerAll(Locales.of(locale), bundle, false);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        GlobalTranslator.translator().addSource(translations);
    }
}
