/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.query;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import team.ebi.epicbanitem.expression.CompareQueryExpression;

public class GteQueryExpression extends CompareQueryExpression {

    public GteQueryExpression(double value) {
        super(value, (i, j) -> i >= j);
    }

    public GteQueryExpression(DataView data, DataQuery query) {
        super(data, query, (i, j) -> i >= j);
    }
}
