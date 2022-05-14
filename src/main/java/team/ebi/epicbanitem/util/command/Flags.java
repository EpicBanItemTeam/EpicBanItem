package team.ebi.epicbanitem.util.command;

import org.spongepowered.api.command.parameter.managed.Flag;

public final class Flags {

  public static final Flag BLOCK = Flag.builder().aliases("block", "b").build();

  public static final Flag PRESET =
      Flag.builder()
          .aliases("preset", "p")
          .setParameter(Parameters.PRESET.key(Parameters.Keys.PRESET).build())
          .build();
}
