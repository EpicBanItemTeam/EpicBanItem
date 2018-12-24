package com.github.euonmyoji.epicbanitem.util.nbt.visitor;

import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public interface DataCompoundVisitor {
    DataVisitor visitCompoundValue(String key);

    void visitCompoundEnd();

    abstract class Impl implements DataCompoundVisitor {
        private final DataCompoundVisitor parent;

        public Impl(DataCompoundVisitor parent) {
            this.parent = parent;
        }

        @Override
        public DataVisitor visitCompoundValue(String key) {
            return this.parent.visitCompoundValue(key);
        }

        @Override
        public void visitCompoundEnd() {
            this.parent.visitCompoundEnd();
        }
    }

    class Empty implements DataCompoundVisitor {
        @Override
        public DataVisitor visitCompoundValue(String key) {
            return new DataVisitor.Empty();
        }

        @Override
        public void visitCompoundEnd() {
            // do nothing here
        }
    }
}
