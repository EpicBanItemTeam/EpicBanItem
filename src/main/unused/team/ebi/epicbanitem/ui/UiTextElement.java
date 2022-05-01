package team.ebi.epicbanitem.ui;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * @author The EpicBanItem Team
 */
@FunctionalInterface
public interface UiTextElement {
    Text toText(Player viewer);
}
