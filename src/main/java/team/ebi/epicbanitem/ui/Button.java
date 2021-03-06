package team.ebi.epicbanitem.ui;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.command.CommandCallback;

import java.util.function.Supplier;

/**
 * @author The EpicBanItem Team
 */
@NonnullByDefault
public abstract class Button implements UiTextElement {
    protected Supplier<Text.Builder> display;

    protected Button(Supplier<Text.Builder> display) {
        this.display = display;
    }

    public abstract void onClick(CommandSource source);

    @Override
    public Text toText(Player viewer) {
        return display.get().onClick(CommandCallback.addCallback(viewer.getUniqueId(), this::onClick)).build();
    }
}
