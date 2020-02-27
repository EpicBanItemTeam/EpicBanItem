package team.ebi.epicbanitem.check;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import team.ebi.epicbanitem.api.CheckResult;
import team.ebi.epicbanitem.api.CheckRuleTrigger;
import team.ebi.epicbanitem.configuration.BanConfig;
import team.ebi.epicbanitem.util.NbtTagDataUtil;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author The EpicBanItem Team
 */
@Singleton
@NonnullByDefault
// TODO: 2020/2/21 Logger I18N
public class CheckRuleServiceImpl implements CheckRuleService {
    @Inject
    BanConfig banConfig;

    @Inject
    Logger logger;

    PluginContainer pluginContainer;

    @Inject
    private CheckRuleServiceImpl(PluginContainer pluginContainer, EventManager eventManager) {
        eventManager.registerListeners(pluginContainer, this);
        this.pluginContainer = pluginContainer;
    }

    @Override
    public CompletableFuture<Boolean> appendRule(CheckRule rule) {
        try {
            return banConfig.addRule(rule);
        } catch (IOException e) {
            logger.error("Failed to save ban config.", e);
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<Boolean> removeRule(CheckRule rule) {
        try {
            return banConfig.removeRule(rule.getName());
        } catch (IOException e) {
            logger.error("Failed to save ban config.", e);
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public <T extends Subject> CheckResult check(ItemStackSnapshot item, World world, CheckRuleTrigger trigger, @Nullable T subject) {
        DataContainer nbt = NbtTagDataUtil.toNbt(item);
        CheckResult checkResult = CheckResult.empty(nbt);
        return item.isEmpty() ? checkResult : check(checkResult, NbtTagDataUtil.getId(nbt), world, trigger, subject);
    }

    @Override
    public <T extends Subject> CheckResult check(BlockSnapshot block, World world, CheckRuleTrigger trigger, @Nullable T subject) {
        DataContainer nbt = NbtTagDataUtil.toNbt(block);
        CheckResult checkResult = CheckResult.empty(nbt);
        boolean isAir = BlockTypes.AIR.equals(block.getState().getType());
        return isAir ? checkResult : check(checkResult, NbtTagDataUtil.getId(nbt), world, trigger, subject);
    }

    @Override
    public <T extends Subject> CheckResult check(ItemStack item, World world, CheckRuleTrigger trigger, @Nullable T subject) {
        DataContainer nbt = NbtTagDataUtil.toNbt(item);
        CheckResult checkResult = CheckResult.empty(nbt);
        return item.isEmpty() ? checkResult : check(checkResult, NbtTagDataUtil.getId(nbt), world, trigger, subject);
    }

    @Override
    public Set<CheckRuleIndex> getIndexes() {
        return banConfig.getItems();
    }

    @Override
    public List<CheckRule> getCheckRulesByIndex(CheckRuleIndex index) {
        return banConfig.getRules(index);
    }

    @Override
    public Collection<CheckRule> getCheckRules() {
        return banConfig.getRules();
    }

    @Override
    public Set<String> getNames() {
        return banConfig.getRuleNames();
    }

    @Override
    public Optional<CheckRule> getCheckRuleByName(String name) {
        return banConfig.getRule(name);
    }

    @Override
    public Optional<CheckRule> getCheckRuleByNameAndIndex(CheckRuleIndex index, String name) {
        return banConfig.getRules(index).stream().filter(c -> c.getName().equals(name)).findFirst();
    }

    private CheckResult check(CheckResult origin, String id, World world, CheckRuleTrigger trigger, @Nullable Subject subject) {
        return banConfig
            .getRulesWithIdFiltered(id)
            .stream()
            .reduce(origin, (result, rule) -> rule.check(result, world, trigger, subject), (a, b) -> {throw new IllegalStateException();});
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        ServiceManager serviceManager = Sponge.getServiceManager();
        serviceManager.setProvider(pluginContainer, team.ebi.epicbanitem.api.CheckRuleService.class, this);
        serviceManager.setProvider(pluginContainer, CheckRuleService.class, this);
    }
}
