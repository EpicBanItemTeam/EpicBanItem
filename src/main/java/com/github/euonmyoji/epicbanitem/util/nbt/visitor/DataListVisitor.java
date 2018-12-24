package com.github.euonmyoji.epicbanitem.util.nbt.visitor;

import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public interface DataListVisitor {
    DataVisitor visitListValue();

    void visitListEnd();

    public abstract class Impl implements DataListVisitor {
        private final DataListVisitor parent;

        public Impl(DataListVisitor parent) {
            this.parent = parent;
        }

        @Override
        public DataVisitor visitListValue() {
            return this.parent.visitListValue();
        }

        @Override
        public void visitListEnd() {
            this.parent.visitListEnd();
        }
    }
}
