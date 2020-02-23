package team.ebi.epicbanitem.util.nbt;

import org.spongepowered.api.data.DataView;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@FunctionalInterface
public interface DataTransformer {
    /**
     * update
     *
     * @param result the result of query
     * @param view   the data view of something
     * @return update result
     */
    UpdateResult update(QueryResult result, DataView view);
}
