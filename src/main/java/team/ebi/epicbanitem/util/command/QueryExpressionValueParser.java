/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.util.command;

import com.google.inject.Singleton;
import team.ebi.epicbanitem.expression.RootQueryExpression;

@Singleton
public class QueryExpressionValueParser extends DataSerializableValueParser<RootQueryExpression> {
    public QueryExpressionValueParser() {
        super(RootQueryExpression.class);
    }
}
