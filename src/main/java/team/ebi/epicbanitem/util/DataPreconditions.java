package team.ebi.epicbanitem.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.persistence.InvalidDataException;

public final class DataPreconditions {
  public static void checkData(boolean expression) {
    if (!expression) {
      throw new InvalidDataException();
    }
  }

  public static void checkData(boolean expression, @Nullable Object errorMessage) {
    if (!expression) {
      throw new InvalidDataException(String.valueOf(errorMessage));
    }
  }

  public static void checkData(
      boolean expression,
      @NotNull String errorMessageTemplate,
      @Nullable Object... errorMessageArgs) {
    if (!expression) {
      throw new IllegalArgumentException(String.format(errorMessageTemplate, errorMessageArgs));
    }
  }
}
