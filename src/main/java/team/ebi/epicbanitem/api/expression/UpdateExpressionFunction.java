package team.ebi.epicbanitem.api.expression;

import java.util.function.Function;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.util.annotation.CatalogedBy;

@CatalogedBy(UpdateExpressions.class)
@FunctionalInterface
public interface UpdateExpressionFunction
    extends Function<DataView, UpdateExpression>, DefaultedRegistryValue {}
