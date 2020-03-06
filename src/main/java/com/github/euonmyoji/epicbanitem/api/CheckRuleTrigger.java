package com.github.euonmyoji.epicbanitem.api;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 * @deprecated since it has been renamed
 * @see team.ebi.epicbanitem.api.CheckRuleTrigger
 */
@Deprecated
@NonnullByDefault
public interface CheckRuleTrigger extends TextRepresentable {
    @Override
    String toString();

    @Override
    default Text toText() {
        return Text.of(toString());
    }

    @Deprecated
    @NonnullByDefault
    class Proxy implements CheckRuleTrigger {
        private final team.ebi.epicbanitem.api.CheckRuleTrigger proxy;

        Proxy(team.ebi.epicbanitem.api.CheckRuleTrigger proxy) {
            this.proxy = proxy;
        }

        @Override
        public Text toText() {
            return this.proxy.toText();
        }

        @Override
        public void applyTo(Text.Builder builder) {
            this.proxy.applyTo(builder);
        }

        @Override
        public String toString() {
            return this.proxy.toString();
        }
    }
}
