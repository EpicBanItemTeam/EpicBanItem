/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.rule;

import org.spongepowered.api.data.persistence.DataQuery;

public final class RestrictionRuleQueries {

    public static final DataQuery RULE = DataQuery.of("Rule");
    public static final DataQuery PRIORITY = DataQuery.of("Priority");
    public static final DataQuery QUERY = DataQuery.of("Query");
    public static final DataQuery UPDATE = DataQuery.of("Update");
    public static final DataQuery PREDICATE = DataQuery.of("Predicate");
    public static final DataQuery NEED_CANCEL = DataQuery.of("NeedCancel");
    public static final DataQuery DEFAULT_WORLD_STATE = DataQuery.of("DefaultWorldState");
    public static final DataQuery DEFAULT_TRIGGER_STATE = DataQuery.of("DefaultTriggerState");

    private RestrictionRuleQueries() {}
}
