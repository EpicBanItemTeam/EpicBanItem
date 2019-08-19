package com.github.euonmyoji.epicbanitem.util.nbt.visitor;

import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public interface DataVisitor {
    DataVisitor EMPTY = new DataVisitor() {
        @Override
        public String toString() {
            return DataVisitor.class.getName() + ".EMPTY";
        }
    };

    abstract class Impl implements DataVisitor {
        private final DataVisitor parent;

        public Impl(DataVisitor parent) {
            this.parent = parent;
        }

        @Override
        public void visitByte(byte b) {
            this.parent.visitByte(b);
        }

        @Override
        public void visitShort(short s) {
            this.parent.visitShort(s);
        }

        @Override
        public void visitInt(int i) {
            this.parent.visitInt(i);
        }

        @Override
        public void visitLong(long l) {
            this.parent.visitLong(l);
        }

        @Override
        public void visitFloat(float f) {
            this.parent.visitFloat(f);
        }

        @Override
        public void visitDouble(double d) {
            this.parent.visitDouble(d);
        }

        @Override
        public void visitString(String s) {
            this.parent.visitString(s);
        }

        @Override
        public void visitByteArray(byte... bytes) {
            this.parent.visitByteArray(bytes);
        }

        @Override
        public void visitIntArray(int... ints) {
            this.parent.visitIntArray(ints);
        }

        @Override
        public void visitLongArray(long... longs) {
            this.parent.visitLongArray(longs);
        }

        @Override
        public DataListVisitor visitList() {
            return this.parent.visitList();
        }

        @Override
        public DataCompoundVisitor visitCompound() {
            return this.parent.visitCompound();
        }
    }

    default void visitByte(byte b) {
        // do nothing here
    }

    default void visitShort(short s) {
        // do nothing here
    }

    default void visitInt(int i) {
        // do nothing here
    }

    default void visitLong(long l) {
        // do nothing here
    }

    default void visitFloat(float f) {
        // do nothing here
    }

    default void visitDouble(double d) {
        // do nothing here
    }

    default void visitString(String s) {
        // do nothing here
    }

    default void visitByteArray(byte... bytes) {
        // do nothing here
    }

    default void visitIntArray(int... ints) {
        // do nothing here
    }

    default void visitLongArray(long... longs) {
        // do nothing here
    }

    default DataListVisitor visitList() {
        return DataListVisitor.EMPTY;
    }

    default DataCompoundVisitor visitCompound() {
        return DataCompoundVisitor.EMPTY;
    }
}
