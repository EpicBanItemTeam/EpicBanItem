package team.ebi.epicbanitem.util;

import java.util.List;
import java.util.Optional;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionTypes;
import org.spongepowered.api.data.persistence.DataSerializable;

public class DataSerializableValueParser<T extends DataSerializable> implements ValueParser<T> {

  private final Class<? extends T> type;

  public DataSerializableValueParser(Class<? extends T> type) {
    this.type = type;
  }

  @Override
  public Optional<? extends T> parseValue(
      Parameter.Key<? super T> parameterKey,
      ArgumentReader.Mutable reader,
      CommandContext.Builder context)
      throws ArgumentParseException {
    return Sponge.dataManager().deserialize(type, reader.parseDataContainer());
  }

  @Override
  public List<ClientCompletionType> clientCompletionType() {
    return List.of(ClientCompletionTypes.SNBT.get());
  }
}
