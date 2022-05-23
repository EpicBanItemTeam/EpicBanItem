/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression;

import java.util.List;

import org.spongepowered.api.data.persistence.DataView;

import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import team.ebi.epicbanitem.api.expression.ExpressionService;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.data.QueryResultRenderer;

@Singleton
public class ExpressionServiceImpl implements ExpressionService {

    @Override
    public List<Component> renderQueryResult(DataView view, QueryResult result) {
        return QueryResultRenderer.render(view, result);
    }
}
