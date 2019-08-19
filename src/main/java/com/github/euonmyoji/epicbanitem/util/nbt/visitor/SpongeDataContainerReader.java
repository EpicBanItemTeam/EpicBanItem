package com.github.euonmyoji.epicbanitem.util.nbt.visitor;

import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Map;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public class SpongeDataContainerReader {
    private final DataView view;

    public <T extends DataSerializable> SpongeDataContainerReader(T value) {
        this.view = value.toContainer();
    }

    public SpongeDataContainerReader(DataView view) {
        this.view = view;
    }

    public void accept(DataCompoundVisitor visitor) {
        this.accept(visitor, this.view.getValues(false));
    }

    private void accept(DataCompoundVisitor compoundVisitor, Map<?, ?> values) {
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            this.accept(compoundVisitor.visitCompoundValue(entry.getKey().toString()), entry.getValue());
        }
        compoundVisitor.visitCompoundEnd();
    }

    private void accept(DataListVisitor listVisitor, List<?> values) {
        for (Object value : values) {
            this.accept(listVisitor.visitListValue(), value);
        }
        listVisitor.visitListEnd();
    }

    private void accept(DataVisitor visitor, Object value) {
        if (value instanceof DataView) {
            this.accept(visitor.visitCompound(), ((DataView) value).getValues(false));
        } else if (value instanceof DataSerializable) {
            this.accept(visitor.visitCompound(), ((DataSerializable) value).toContainer().getValues(false));
        } else if (value instanceof Map<?, ?>) {
            this.accept(visitor.visitCompound(), (Map<?, ?>) value);
        } else if (value instanceof List<?>) {
            this.accept(visitor.visitList(), (List<?>) value);
        } else if (value instanceof Boolean) {
            visitor.visitByte((Boolean) value ? (byte) 1 : (byte) 0);
        } else if (value instanceof Byte) {
            visitor.visitByte((Byte) value);
        } else if (value instanceof Short) {
            visitor.visitShort((Short) value);
        } else if (value instanceof Integer) {
            visitor.visitInt((Integer) value);
        } else if (value instanceof Long) {
            visitor.visitLong((Long) value);
        } else if (value instanceof Float) {
            visitor.visitFloat((Float) value);
        } else if (value instanceof Double) {
            visitor.visitDouble((Double) value);
        } else if (value instanceof String) {
            visitor.visitString((String) value);
        } else if (value instanceof byte[]) {
            visitor.visitByteArray((byte[]) value);
        } else if (value instanceof Byte[]) {
            Byte[] oldArray = (Byte[]) value;
            byte[] newArray = new byte[oldArray.length];
            for (int i = oldArray.length - 1; i >= 0; --i) {
                newArray[i] = oldArray[i];
            }
            visitor.visitByteArray(newArray);
        } else if (value instanceof int[]) {
            visitor.visitIntArray((int[]) value);
        } else if (value instanceof Integer[]) {
            Integer[] oldArray = (Integer[]) value;
            int[] newArray = new int[oldArray.length];
            for (int i = oldArray.length - 1; i >= 0; --i) {
                newArray[i] = oldArray[i];
            }
            visitor.visitIntArray(newArray);
        } else if (value instanceof long[]) {
            visitor.visitLongArray((long[]) value);
        } else if (value instanceof Long[]) {
            Long[] oldArray = (Long[]) value;
            long[] newArray = new long[oldArray.length];
            for (int i = oldArray.length - 1; i >= 0; --i) {
                newArray[i] = oldArray[i];
            }
            visitor.visitLongArray(newArray);
        }
    }
}
