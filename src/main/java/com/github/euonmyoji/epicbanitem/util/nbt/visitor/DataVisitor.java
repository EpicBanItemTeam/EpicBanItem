package com.github.euonmyoji.epicbanitem.util.nbt.visitor;

import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public interface DataVisitor {
    void visitByte(byte b);

    void visitShort(short s);

    void visitInt(int i);

    void visitLong(long l);

    void visitFloat(float f);

    void visitDouble(double d);

    void visitString(String s);

    void visitByteArray(byte... bytes);

    void visitIntArray(int... ints);

    void visitLongArray(long... longs);

    DataListVisitor visitList();

    DataCompoundVisitor visitCompound();

    public abstract class Impl implements DataVisitor {
        private final DataVisitor parent;

        protected Impl(DataVisitor parent) {
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
}
