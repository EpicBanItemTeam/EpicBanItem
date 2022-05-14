package team.ebi.epicbanitem.util.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.command.parameter.managed.Flag;
import team.ebi.epicbanitem.util.command.Parameters.Keys;

@Singleton
public final class Flags {
  public final Flag block;
  public final Flag preset;
  public final Flag trigger;
  public final Flag world;

  @Inject
  public Flags(Parameters parameters, Keys keys) {
    block = Flag.builder().aliases("block", "b").build();

    preset =
        Flag.builder()
            .aliases("preset", "p")
            .setParameter(parameters.preset.key(keys.preset).build())
            .build();

    trigger =
        Flag.builder()
            .aliases("trigger", "t")
            .setParameter(parameters.trigger.key(keys.trigger).build())
            .build();

    world =
        Flag.builder()
            .aliases("world", "w")
            .setParameter(parameters.world.key(keys.world).build())
            .build();
  }
}
