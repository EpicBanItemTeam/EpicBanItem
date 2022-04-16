package team.ebi.epicbanitem;

import com.google.inject.Inject;
import java.util.Objects;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.plugin.builtin.jvm.Plugin;

/**
 * @author The EpicBanItem Team
 */
@Plugin(EpicBanItem.NAMESPACE)
public class EpicBanItem {
  public static final String NAMESPACE = "epicbanitem";
  public static final ResourceKey ROOT_KEY = EpicBanItem.key("root");

  public static ResourceKey key(String value) {
    return ResourceKey.of(NAMESPACE, value);
  }

  @Inject
  public EpicBanItem(EBIRegistries registries) {
    Objects.requireNonNull(registries);
  }
}
