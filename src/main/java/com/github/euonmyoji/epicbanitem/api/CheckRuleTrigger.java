package com.github.euonmyoji.epicbanitem.api;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public interface CheckRuleTrigger extends TextRepresentable, CatalogType {
    /**
     * 获取触发器名称
     *
     * @return 触发器名称
     */
    @Override
    String toString();

    @Override
    default Text toText() {
        return Text.of(toString());
    }
}
