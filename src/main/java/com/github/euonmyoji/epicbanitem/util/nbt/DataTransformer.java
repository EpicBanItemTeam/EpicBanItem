package com.github.euonmyoji.epicbanitem.util.nbt;

import org.spongepowered.api.data.DataView;

/**
 * @author ustc_zzzz
 */
public interface DataTransformer {
    /**
     * todo:javadoc
     *
     * @param result
     * @param view
     * @return
     */
    UpdateResult update(QueryResult result, DataView view);
}
