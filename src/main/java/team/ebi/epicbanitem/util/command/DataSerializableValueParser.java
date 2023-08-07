/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.util.command;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionTypes;
import org.spongepowered.api.data.persistence.DataSerializable;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

public class DataSerializableValueParser<T extends DataSerializable> implements ValueParser<T> {

    private final Class<? extends T> type;

    public DataSerializableValueParser(Class<? extends T> type) {
        this.type = type;
    }

    @Override
    public Optional<? extends T> parseValue(
            Parameter.Key<? super T> parameterKey,
            ArgumentReader.@NotNull Mutable reader,
            CommandContext.Builder context)
            throws ArgumentParseException {
        return Sponge.dataManager().deserialize(type, reader.parseDataContainer());
    }

    @Override
    public List<ClientCompletionType> clientCompletionType() {
        return Lists.newArrayList(ClientCompletionTypes.SNBT.get());
    }
}
