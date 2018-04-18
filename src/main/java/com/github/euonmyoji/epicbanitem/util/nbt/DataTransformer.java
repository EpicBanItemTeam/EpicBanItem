package com.github.euonmyoji.epicbanitem.util.nbt;

import org.spongepowered.api.data.DataView;

/**
 * @author ustc_zzzz
 */
public interface DataTransformer {
    UpdateResult update(QueryResult result, DataView view);
}
