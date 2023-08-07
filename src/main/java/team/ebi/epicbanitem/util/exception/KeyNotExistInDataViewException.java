/*
 * Copyright 2023 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.util.exception;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

public class KeyNotExistInDataViewException extends IllegalStateException {
    public KeyNotExistInDataViewException(DataQuery key, DataView view) {
        super(key + " not exist in view " + view);
    }
}
