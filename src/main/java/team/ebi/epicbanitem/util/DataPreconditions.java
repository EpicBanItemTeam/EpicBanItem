package team.ebi.epicbanitem.util;

import java.text.MessageFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.persistence.InvalidDataException;

public final class DataPreconditions {
  public static void checkData(boolean expression) {
    if (!expression) {
      throw new InvalidDataException();
    }
  }

  public static void checkData(boolean expression, String errorMessage) {
    if (!expression) {
      throw new InvalidDataException(errorMessage);
    }
  }

  public static void checkData(
      boolean expression,
      @NotNull String errorMessageTemplate,
      @Nullable Object... errorMessageArgs) {
    if (!expression) {
      throw new IllegalArgumentException(
          MessageFormat.format(errorMessageTemplate, errorMessageArgs));
    }
  }
}
