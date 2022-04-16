package team.ebi.epicbanitem.expression;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.api.expression.TestResult;

public class SimpleTestResult implements TestResult {
  private final ImmutableMap<String, TestResult> children;
  private final boolean inArray;

  public SimpleTestResult(boolean inArray) {
    this(inArray, Maps.newHashMap());
  }

  public SimpleTestResult(boolean inArray, Map<String, TestResult> children) {
    this.children = ImmutableMap.copyOf(children);
    this.inArray = inArray;
  }

  @Override
  public ImmutableMap<String, TestResult> children() {
    return children;
  }

  @Override
  public TestResult merge(@NotNull TestResult target) {
    return new SimpleTestResult(
        inArray || target.inArray(),
        new LinkedHashMap<String, TestResult>(children) {
          {
            target
                .children()
                .forEach(
                    (key, value) ->
                        compute(
                            key,
                            (ignored, oldValue) ->
                                Objects.isNull(oldValue) ? value : oldValue.merge(value)));
          }
        });
  }

  @Override
  public boolean inArray() {
    return inArray;
  }
}
