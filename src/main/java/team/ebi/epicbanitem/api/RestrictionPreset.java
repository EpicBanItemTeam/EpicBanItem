package team.ebi.epicbanitem.api;

import java.util.function.Function;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.util.annotation.CatalogedBy;

@CatalogedBy(RestrictionPresets.class)
@FunctionalInterface
public interface RestrictionPreset extends DefaultedRegistryValue, Function<DataView, DataView> {}
