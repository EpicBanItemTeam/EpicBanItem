package com.github.euonmyoji.epicbanitem.util.nbt.visitor;

import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public interface DataCompoundVisitor {
    DataCompoundVisitor EMPTY = new DataCompoundVisitor() {
        @Override
        public String toString() {
            return DataCompoundVisitor.class + ".EMPTY";
        }
    };

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

    default DataVisitor visitCompoundValue(String key) {
        return DataVisitor.EMPTY;
    }

    default void visitCompoundEnd() {
        // do nothing here
    }
}
