package com.github.euonmyoji.epicbanitem.util.nbt;

import org.spongepowered.api.data.DataView;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@FunctionalInterface
public interface DataTransformer {
    UpdateResult update(QueryResult result, DataView view);
}
