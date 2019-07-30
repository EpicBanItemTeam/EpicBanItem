package com.github.euonmyoji.epicbanitem.api;

import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public interface CheckRuleTrigger {
    /**
     * 获取触发器名称
     *
     * @return 触发器名称
     */
    @Override
    String toString();
}
