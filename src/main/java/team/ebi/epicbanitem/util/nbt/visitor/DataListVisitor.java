package team.ebi.epicbanitem.util.nbt.visitor;

import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public interface DataListVisitor {
    DataListVisitor EMPTY = new DataListVisitor() {
        @Override
        public String toString() {
            return DataListVisitor.class + ".EMPTY";
        }
    };

    abstract class Impl implements DataListVisitor {
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

    default DataVisitor visitListValue() {
        return DataVisitor.EMPTY;
    }

    default void visitListEnd() {
        // do nothing here
    }
}