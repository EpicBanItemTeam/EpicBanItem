package com.github.euonmyoji.epicbanitem.util.nbt.visitor;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public class SpongeDataContainerWriter implements DataCompoundVisitor {
    private final Consumer<? super DataContainer> setter;
    private final DataContainer container;

    private SpongeDataContainerWriter(Consumer<? super DataContainer> setter) {
        this.container = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
        this.setter = setter;
    }

    public SpongeDataContainerWriter() {
        this(Objects::requireNonNull);
    }

    public DataContainer toContainer() throws InvalidDataException {
        return this.container.copy(DataView.SafetyMode.ALL_DATA_CLONED);
    }

    @Override
    public DataVisitor visitCompoundValue(String key) {
        return new SpongeDataWriter(value -> this.container.set(DataQuery.of(key), Objects.requireNonNull(value)));
    }

    @Override
    public void visitCompoundEnd() {
        this.setter.accept(this.container);
    }

    private static class SpongeDataListWriter implements DataListVisitor {
        private final Consumer<? super List<Object>> setter;
        private final ImmutableList.Builder<Object> builder;

        public SpongeDataListWriter(Consumer<? super List<Object>> setter) {
            this.builder = ImmutableList.builder();
            this.setter = setter;
        }

        @Override
        public DataVisitor visitListValue() {
            return new SpongeDataWriter(value -> this.builder.add(Objects.requireNonNull(value)));
        }

        @Override
        public void visitListEnd() {
            this.setter.accept(this.builder.build());
        }
    }

    private static class SpongeDataWriter implements DataVisitor {
        private final Consumer<Object> setter;

        private SpongeDataWriter(Consumer<Object> setter) {
            this.setter = setter;
        }

        @Override
        public void visitByte(byte b) {
            this.setter.accept(b);
        }

        @Override
        public void visitShort(short s) {
            this.setter.accept(s);
        }

        @Override
        public void visitInt(int i) {
            this.setter.accept(i);
        }

        @Override
        public void visitLong(long l) {
            this.setter.accept(l);
        }

        @Override
        public void visitFloat(float f) {
            this.setter.accept(f);
        }

        @Override
        public void visitDouble(double d) {
            this.setter.accept(d);
        }

        @Override
        public void visitString(String s) {
            this.setter.accept(s);
        }

        @Override
        public void visitByteArray(byte... bytes) {
            this.setter.accept(bytes);
        }

        @Override
        public void visitIntArray(int... ints) {
            this.setter.accept(ints);
        }

        @Override
        public void visitLongArray(long... longs) {
            this.setter.accept(longs);
        }

        @Override
        public DataListVisitor visitList() {
            return new SpongeDataListWriter(this.setter);
        }

        @Override
        public DataCompoundVisitor visitCompound() {
            return new SpongeDataContainerWriter(this.setter);
        }
    }
}
