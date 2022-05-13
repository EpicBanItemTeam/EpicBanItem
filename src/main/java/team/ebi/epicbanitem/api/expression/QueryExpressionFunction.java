package team.ebi.epicbanitem.api.expression;

import java.util.function.BiFunction;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.util.annotation.CatalogedBy;

@CatalogedBy(QueryExpressionFunctions.class)
@FunctionalInterface
public interface QueryExpressionFunction
    extends BiFunction<DataView, DataQuery, QueryExpression>, DefaultedRegistryValue {}
