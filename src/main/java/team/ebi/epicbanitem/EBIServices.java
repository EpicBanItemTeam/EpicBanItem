/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem;

import com.google.inject.Inject;
import team.ebi.epicbanitem.api.RestrictionService;
import team.ebi.epicbanitem.api.expression.ExpressionService;
import team.ebi.epicbanitem.api.rule.RestrictionRuleService;
import team.ebi.epicbanitem.api.rule.RulePredicateService;

/**
 * @apiNote Use {@link Inject} if possible
 */
public final class EBIServices {

    public static RulePredicateService predicateService;
    public static RestrictionService restrictionService;
    public static RestrictionRuleService ruleService;
    public static ExpressionService expressionService;

    @Inject
    EBIServices(
            RulePredicateService predicateService,
            RestrictionService restrictionService,
            RestrictionRuleService ruleService,
            ExpressionService expressionService) {
        EBIServices.predicateService = predicateService;
        EBIServices.restrictionService = restrictionService;
        EBIServices.ruleService = ruleService;
        EBIServices.expressionService = expressionService;
    }
}
