/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.util;

import java.io.IOException;
import java.io.Reader;
import java.util.ResourceBundle;

public final class PropertyResourceBundle extends java.util.PropertyResourceBundle {

    public PropertyResourceBundle(Reader reader) throws IOException {
        super(reader);
    }

    @Override
    public void setParent(ResourceBundle parent) {
        super.setParent(parent);
    }
}
