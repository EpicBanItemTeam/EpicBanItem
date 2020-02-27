package team.ebi.epicbanitem.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import org.spongepowered.api.command.CommandCallable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.BiConsumer;

/**
 * @author The EpicBanItem Team
 */
@Singleton
public class CommandMapService {
    private final Map<List<String>, CommandCallable> childrenMap = Maps.newLinkedHashMap();
    private final SortedMap<String, CommandCallable> flatMap = Maps.newTreeMap();

    public void registerCommand(ICommand command) {
        ImmutableList<String> names = ImmutableList.copyOf(command.getNameList());
        names.forEach(name -> flatMap.putIfAbsent(name, command.getCallable()));
        childrenMap.put(names, command.getCallable());
    }

    public Map<List<String>, CommandCallable> getChildrenMap() {
        return Collections.unmodifiableMap(childrenMap);
    }

    public SortedMap<String, CommandCallable> getFlatMap() {
        return Collections.unmodifiableSortedMap(flatMap);
    }
}
