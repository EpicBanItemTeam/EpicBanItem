/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.expression;

import java.util.Map;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.*;

import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.expression.RootQueryExpression;

@RegistryScopes(scopes = RegistryScope.ENGINE)
public final class QueryExpressionFunctions {

    public static final DefaultedRegistryReference<QueryExpressionFunction> OR =
            key(EpicBanItem.key(ExpressionKeys.OR));
    public static final DefaultedRegistryReference<QueryExpressionFunction> NOR =
            key(EpicBanItem.key(ExpressionKeys.NOR));
    public static final DefaultedRegistryReference<QueryExpressionFunction> AND =
            key(EpicBanItem.key(ExpressionKeys.AND));
    public static final DefaultedRegistryReference<QueryExpressionFunction> NOT =
            key(EpicBanItem.key(ExpressionKeys.NOT));
    public static final DefaultedRegistryReference<QueryExpressionFunction> EQ =
            key(EpicBanItem.key(ExpressionKeys.EQ));
    public static final DefaultedRegistryReference<QueryExpressionFunction> NE =
            key(EpicBanItem.key(ExpressionKeys.NE));
    public static final DefaultedRegistryReference<QueryExpressionFunction> GT =
            key(EpicBanItem.key(ExpressionKeys.GT));
    public static final DefaultedRegistryReference<QueryExpressionFunction> LT =
            key(EpicBanItem.key(ExpressionKeys.LT));
    public static final DefaultedRegistryReference<QueryExpressionFunction> GTE =
            key(EpicBanItem.key(ExpressionKeys.GTE));
    public static final DefaultedRegistryReference<QueryExpressionFunction> LTE =
            key(EpicBanItem.key(ExpressionKeys.LTE));
    public static final DefaultedRegistryReference<QueryExpressionFunction> IN =
            key(EpicBanItem.key(ExpressionKeys.IN));
    public static final DefaultedRegistryReference<QueryExpressionFunction> NIN =
            key(EpicBanItem.key(ExpressionKeys.NIN));
    public static final DefaultedRegistryReference<QueryExpressionFunction> SIZE =
            key(EpicBanItem.key(ExpressionKeys.SIZE));
    public static final DefaultedRegistryReference<QueryExpressionFunction> ALL =
            key(EpicBanItem.key(ExpressionKeys.ALL));
    public static final DefaultedRegistryReference<QueryExpressionFunction> ELEM_MATCH =
            key(EpicBanItem.key(ExpressionKeys.ELEM_MATCH));
    public static final DefaultedRegistryReference<QueryExpressionFunction> EXISTS =
            key(EpicBanItem.key(ExpressionKeys.EXISTS));
    public static final DefaultedRegistryReference<QueryExpressionFunction> REGEX =
            key(EpicBanItem.key(ExpressionKeys.REGEX));

    public static Map<String, QueryExpressionFunction> expressions;

    static {
        Sponge.dataManager().registerBuilder(RootQueryExpression.class, new RootQueryExpression.Builder());
    }

    private QueryExpressionFunctions() {}

    public static Registry<QueryExpressionFunction> registry() {
        return EBIRegistries.QUERY_EXPRESSION.get();
    }

    private static DefaultedRegistryReference<QueryExpressionFunction> key(final ResourceKey location) {
        return RegistryKey.of(EBIRegistries.QUERY_EXPRESSION, location).asDefaultedReference(Sponge::server);
    }
}
