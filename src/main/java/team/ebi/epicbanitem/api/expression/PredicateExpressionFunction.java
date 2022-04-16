package team.ebi.epicbanitem.api.expression;

import java.util.function.Function;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.util.annotation.CatalogedBy;

@CatalogedBy(PredicateExpressions.class)
@FunctionalInterface
public interface PredicateExpressionFunction
    extends Function<DataView, PredicateExpression>, DefaultedRegistryValue {}
