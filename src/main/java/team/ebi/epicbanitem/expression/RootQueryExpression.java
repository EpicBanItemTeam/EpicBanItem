/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression;

import java.util.Optional;

import org.spongepowered.api.data.persistence.*;

import com.github.bsideup.jabel.Desugar;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

@Desugar
public record RootQueryExpression(CommonQueryExpression expression) implements QueryExpression, DataSerializable {

    public RootQueryExpression() {
        this(DataContainer.createNew());
    }

    public RootQueryExpression(DataView view) {
        this(view, DataQuery.of());
    }

    public RootQueryExpression(DataView view, DataQuery query) {
        this(new CommonQueryExpression(view, query));
    }

    @Override
    public int contentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return expression.toContainer();
    }

    @Override
    public Optional<QueryResult> query(DataQuery query, DataView data) {
        return this.expression.query(query, data);
    }

    public static final class Builder extends AbstractDataBuilder<RootQueryExpression> {

        public Builder() {
            super(RootQueryExpression.class, 0);
        }

        @Override
        protected Optional<RootQueryExpression> buildContent(DataView container) throws InvalidDataException {
            return Optional.of(new RootQueryExpression(container));
        }
    }
}
