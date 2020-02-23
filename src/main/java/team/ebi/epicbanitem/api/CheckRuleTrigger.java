package team.ebi.epicbanitem.api;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.util.annotation.CatalogedBy;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.check.Triggers;

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
