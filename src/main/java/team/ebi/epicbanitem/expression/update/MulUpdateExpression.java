/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.update;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;

import team.ebi.epicbanitem.expression.MathUpdateExpression;

public class MulUpdateExpression extends MathUpdateExpression {

    public MulUpdateExpression(DataQuery query, Number argNumber) {
        super(query, argNumber, MulUpdateExpression::mul);
    }

    public MulUpdateExpression(DataView view, DataQuery query) {
        super(view, query, MulUpdateExpression::mul);
    }

    private static Number mul(Number source, Number arg) {
        if (source instanceof Byte) {
            return (byte) (source.byteValue() * arg.byteValue());
        }
        if (source instanceof Short) {
            return (short) (source.shortValue() * arg.shortValue());
        }
        if (source instanceof Integer) {
            return source.intValue() * arg.intValue();
        }
        if (source instanceof Long) {
            return source.longValue() * arg.longValue();
        }
        if (source instanceof Float) {
            return source.floatValue() * arg.floatValue();
        }
        if (source instanceof Double) {
            return source.doubleValue() * arg.doubleValue();
        }
        throw new InvalidDataException("Source isn't a number");
    }
}
