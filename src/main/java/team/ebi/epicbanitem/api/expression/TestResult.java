package team.ebi.epicbanitem.api.expression;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.expression.SimpleTestResult;

public interface TestResult {
  static Optional<TestResult> failed() {
    return Optional.empty();
  }

  static TestResult success(Map<String, TestResult> children) {
    return new SimpleTestResult(false, children);
  }

  static TestResult array(Map<String, TestResult> children) {
    return new SimpleTestResult(true, children);
  }

  static TestResult success() {
    return new SimpleTestResult(false);
  }

  static Optional<TestResult> from(boolean b) {
    return b ? Optional.of(success()) : failed();
  }

  static Optional<TestResult> from(boolean b, Map<String, TestResult> children) {
    return b ? Optional.of(success(children)) : failed();
  }

  static Optional<TestResult> fromArray(boolean b, Map<String, TestResult> children) {
    return b ? Optional.of(array(children)) : failed();
  }

  ImmutableMap<String, TestResult> children();

  @Contract("_ -> new")
  TestResult merge(@NotNull TestResult target);

  boolean inArray();
}
