/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.util.command;

import com.google.inject.Singleton;
import team.ebi.epicbanitem.expression.RootUpdateExpression;

@Singleton
public class UpdateExpressionValueParser extends DataSerializableValueParser<RootUpdateExpression> {
    public UpdateExpressionValueParser() {
        super(RootUpdateExpression.class);
    }
}
