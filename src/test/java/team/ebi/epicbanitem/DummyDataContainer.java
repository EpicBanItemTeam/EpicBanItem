/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem;

import java.util.Optional;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

/**
 * The default implementation of {@link DataContainer} that can be instantiated for any use. This is the primary
 * implementation of any {@link DataView} that is used throughout both SpongeAPI and Sponge implementation.
 */
public final class DummyDataContainer extends DummyDataView implements DataContainer {

    /**
     * Creates a new {@link DummyDataContainer} with a default
     * {@link org.spongepowered.api.data.persistence.DataView.SafetyMode} of
     * {@link org.spongepowered.api.data.persistence.DataView.SafetyMode#ALL_DATA_CLONED}.
     */
    public DummyDataContainer() {
        this(DataView.SafetyMode.ALL_DATA_CLONED);
    }

    /**
     * Creates a new {@link DummyDataContainer} with the provided
     * {@link org.spongepowered.api.data.persistence.DataView.SafetyMode}.
     *
     * @param safety The safety mode to use
     * @see org.spongepowered.api.data.persistence.DataView.SafetyMode
     */
    public DummyDataContainer(final DataView.SafetyMode safety) {
        super(safety);
    }

    @Override
    public Optional<DataView> parent() {
        return Optional.empty();
    }

    @Override
    public DataContainer container() {
        return this;
    }

    @Override
    public DataContainer set(final DataQuery path, final Object value) {
        return (DataContainer) super.set(path, value);
    }

    @Override
    public DataContainer remove(final DataQuery path) {
        return (DataContainer) super.remove(path);
    }
}
