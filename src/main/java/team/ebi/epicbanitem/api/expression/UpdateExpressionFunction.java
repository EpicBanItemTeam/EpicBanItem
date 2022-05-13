package team.ebi.epicbanitem.api.expression;

import java.util.function.BiFunction;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.util.annotation.CatalogedBy;

@CatalogedBy(UpdateExpressionFunctions.class)
@FunctionalInterface
public interface UpdateExpressionFunction
    extends BiFunction<DataView, DataQuery, UpdateExpression>, DefaultedRegistryValue {}
