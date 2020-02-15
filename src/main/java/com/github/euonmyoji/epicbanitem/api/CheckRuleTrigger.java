package com.github.euonmyoji.epicbanitem.api;

import com.github.euonmyoji.epicbanitem.check.Triggers;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.util.annotation.CatalogedBy;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
@CatalogedBy(Triggers.class)
public interface CheckRuleTrigger extends TextRepresentable, CatalogType {
    /**
     * 获取触发器名称
     *
     * @return 触发器名称
     */
    @Override
    String toString();
}
