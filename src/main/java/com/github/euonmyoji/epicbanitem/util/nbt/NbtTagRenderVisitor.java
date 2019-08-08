package com.github.euonmyoji.epicbanitem.util.nbt;

import com.github.euonmyoji.epicbanitem.util.nbt.visitor.DataCompoundVisitor;
import com.github.euonmyoji.epicbanitem.util.nbt.visitor.DataListVisitor;
import com.github.euonmyoji.epicbanitem.util.nbt.visitor.DataVisitor;
import com.google.common.base.Strings;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public class NbtTagRenderVisitor extends DataCompoundVisitor.Impl {
    private static final TextColor COLOR = TextColors.GREEN;
    private static final int INDENT = 2;

    public NbtTagRenderVisitor(Text.Builder builder, QueryResult result) {
        super(new CompoundVisitor(result, false, builder, INDENT));
    }

    public NbtTagRenderVisitor(Text.Builder builder) {
        super(new CompoundVisitor(null, true, builder, INDENT));
    }

    private static class CompoundVisitor implements DataCompoundVisitor {
        private final Map<String, QueryResult> children;
        private final Text.Builder builder;
        private final boolean unselected;
        private final int indent;

        private Text separator;

        private CompoundVisitor(@Nullable QueryResult result, boolean unselected, Text.Builder builder, int indent) {
            this.children = unselected ? Collections.emptyMap() : Objects.requireNonNull(result).getChildren();
            this.builder = builder.append(unselected ? Text.of("{") : Text.of(COLOR, "{"));
            this.unselected = unselected;
            this.separator = Text.of();
            this.indent = indent;
        }

        @Override
        public DataVisitor visitCompoundValue(String key) {
            this.builder.append(this.separator).append(Text.NEW_LINE);
            this.builder.append(Text.of(Strings.repeat(" ", this.indent)));
            this.builder.append(this.children.containsKey(key) ? Text.of(COLOR, key, ": ") : Text.of(key, ": "));

            this.separator = Text.of(",");
            return new Visitor(this.children.get(key), this.builder, this.indent);
        }

        @Override
        public void visitCompoundEnd() {
            this.builder.append(Text.NEW_LINE).append(Text.of(Strings.repeat(" ", this.indent - INDENT)));
            this.builder.append(this.unselected ? Text.of("}") : Text.of(COLOR, "}"));
        }
    }

    private static class ListVisitor implements DataListVisitor {
        private final Map<String, QueryResult> children;
        private final Text.Builder builder;
        private final boolean unselected;
        private final int indent;

        private Text separator;
        private int index = 0;

        private ListVisitor(@Nullable QueryResult result, boolean unselected, Text.Builder builder, int indent) {
            this.children = unselected ? Collections.emptyMap() : Objects.requireNonNull(result).getChildren();
            this.builder = builder.append(unselected ? Text.of("[") : Text.of(COLOR, "["));
            this.unselected = unselected;
            this.separator = Text.of();
            this.indent = indent;
        }

        @Override
        public DataVisitor visitListValue() {
            String key = Integer.toString(this.index++);
            this.builder.append(this.separator).append(Text.NEW_LINE);
            this.builder.append(Text.of(Strings.repeat(" ", this.indent)));

            this.separator = Text.of(",");
            return new Visitor(this.children.get(key), this.builder, this.indent);
        }

        @Override
        public void visitListEnd() {
            this.builder.append(Text.NEW_LINE).append(Text.of(Strings.repeat(" ", this.indent - INDENT)));
            this.builder.append(this.unselected ? Text.of("]") : Text.of(COLOR, "]"));
        }
    }

    private static class Visitor implements DataVisitor {
        @Nullable
        private final QueryResult queryResult;
        private final boolean unselected;
        private final Text.Builder builder;
        private final int indent;

        private Visitor(@Nullable QueryResult result, Text.Builder builder, int indent) {
            this.unselected = result == null;
            this.queryResult = result;
            this.builder = builder;
            this.indent = indent;
        }

        @Override
        public void visitByte(byte b) {
            this.builder.append(Text.of(Byte.toString(b), "b"));
        }

        @Override
        public void visitShort(short s) {
            this.builder.append(Text.of(Short.toString(s), "s"));
        }

        @Override
        public void visitInt(int i) {
            this.builder.append(Text.of(Integer.toString(i)));
        }

        @Override
        public void visitLong(long l) {
            this.builder.append(Text.of(Long.toString(l), "l"));
        }

        @Override
        public void visitFloat(float f) {
            this.builder.append(Text.of(Float.toString(f), "f"));
        }

        @Override
        public void visitDouble(double d) {
            this.builder.append(Text.of(Double.toString(d), "d"));
        }

        @Override
        public void visitString(String s) {
            this.builder.append(Text.of("\"", s
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\u00a7", "\\u00a7"), "\""));
        }

        @Override
        public void visitByteArray(byte... bytes) {
            this.builder.append(Text.of("[B;"));
            Text separator = Text.of();
            for (byte b : bytes) {
                this.builder.append(separator).append(Text.of(Byte.toString(b), "b"));
                separator = Text.of(", ");
            }
            this.builder.append(Text.of("]"));
        }

        @Override
        public void visitIntArray(int... ints) {
            this.builder.append(Text.of("[I;"));
            Text separator = Text.of();
            for (int i : ints) {
                this.builder.append(separator).append(Text.of(Integer.toString(i)));
                separator = Text.of(", ");
            }
            this.builder.append(Text.of("]"));
        }

        @Override
        public void visitLongArray(long... longs) {
            this.builder.append(Text.of("[L;"));
            Text separator = Text.of();
            for (long l : longs) {
                this.builder.append(separator).append(Text.of(Long.toString(l), "l"));
                separator = Text.of(", ");
            }
            this.builder.append(Text.of("]"));
        }

        @Override
        public DataListVisitor visitList() {
            return new ListVisitor(this.queryResult, this.unselected, this.builder, this.indent + INDENT);
        }

        @Override
        public DataCompoundVisitor visitCompound() {
            return new CompoundVisitor(this.queryResult, this.unselected, this.builder, this.indent + INDENT);
        }
    }
}
