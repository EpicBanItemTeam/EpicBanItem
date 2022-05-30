/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.util;

import java.util.Locale;

import org.spongepowered.api.event.Cause;
import org.spongepowered.api.util.locale.LocaleSource;

public final class EventUtils {
    public static Locale locale(Cause cause) {
        return cause.last(LocaleSource.class).map(LocaleSource::locale).orElse(Locale.getDefault());
    }
}
