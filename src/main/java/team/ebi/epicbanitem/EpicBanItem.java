package team.ebi.epicbanitem;

import com.google.inject.Inject;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.plugin.builtin.jvm.Plugin;

/**
 * @author The EpicBanItem Team
 */
@Plugin(EpicBanItem.NAMESPACE)
public class EpicBanItem {
  public static final String NAMESPACE = "epicbanitem";

  public static ResourceKey key(String value) {
    return ResourceKey.of(NAMESPACE, value);
  }

  public static String permission(String permission) {
    return NAMESPACE + "." + permission;
  }

  private static EBITranslator translator = null;

  @Inject
  public EpicBanItem(EBIRegistries registries, EBITranslator translator) {
    Objects.requireNonNull(registries);
    EpicBanItem.translator = translator;
  }

  @NotNull
  public static EBITranslator translator() {
    return Objects.requireNonNull(translator);
  }
}
