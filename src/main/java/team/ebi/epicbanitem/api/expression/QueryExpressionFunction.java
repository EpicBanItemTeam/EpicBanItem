package team.ebi.epicbanitem.api.expression;

import java.util.function.Function;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.util.annotation.CatalogedBy;

@CatalogedBy(QueryExpressions.class)
@FunctionalInterface
public interface QueryExpressionFunction
    extends Function<DataView, QueryExpression>, DefaultedRegistryValue {}
