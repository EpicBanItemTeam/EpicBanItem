/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.expression;

import org.spongepowered.api.data.persistence.DataQuery;

public final class ExpressionKeys {

    // Query
    public static final String OR = "or";
    public static final String NOR = "nor";
    public static final String AND = "and";
    public static final String NOT = "not";

    public static final String EQ = "eq";
    public static final String NE = "ne";

    public static final String GT = "gt";
    public static final String LT = "lt";
    public static final String GTE = "gte";
    public static final String LTE = "lte";

    public static final String IN = "in";
    public static final String NIN = "nin";

    public static final String SIZE = "size";
    public static final String ALL = "all";
    public static final String ELEM_MATCH = "elem_match";

    public static final String EXISTS = "exists";
    public static final String REGEX = "regex";

    // Update
    public static final String SET = "set";
    public static final String UNSET = "unset";
    public static final String RENAME = "rename";
    public static final String POP = "pop";
    public static final String PULL = "pull";
    public static final String INC = "ind";
    public static final String MUL = "mul";

    private ExpressionKeys() {}

    public static DataQuery query(String key) {
        return DataQuery.of("$" + key);
    }
}
