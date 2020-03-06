package com.github.euonmyoji.epicbanitem.api;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 * @deprecated since it has been renamed
 * @see team.ebi.epicbanitem.api.CheckRuleService
 */
@Deprecated
@NonnullByDefault
public interface CheckRuleService {
    static CheckRuleService instance() {
        return Proxy.INSTANCE;
    }

    <T extends Subject> CheckResult check(ItemStackSnapshot snapshot, World world, CheckRuleTrigger trigger, @Nullable T subject);

    <T extends Subject> CheckResult check(BlockSnapshot snapshot, World world, CheckRuleTrigger trigger, @Nullable T subject);

    <T extends Subject> CheckResult check(ItemStack stack, World world, CheckRuleTrigger trigger, @Nullable T subject);

    Optional<CheckRuleTrigger> getTrigger(String name, boolean registerIfAbsent);

    @Deprecated
    @NonnullByDefault
    enum Proxy implements CheckRuleService {
        INSTANCE;

        private final team.ebi.epicbanitem.api.CheckRuleService proxy;
        private final Class<team.ebi.epicbanitem.api.CheckRuleTrigger> cls;

        Proxy() {
            PluginContainer plugin = Sponge.getPluginManager().getPlugin("epicbanitem").orElseThrow(IllegalStateException::new);
            this.proxy = Sponge.getServiceManager().provideUnchecked(team.ebi.epicbanitem.api.CheckRuleService.class);
            Sponge.getServiceManager().setProvider(plugin, CheckRuleService.class, this);
            this.cls = team.ebi.epicbanitem.api.CheckRuleTrigger.class;
        }

        @Override
        public <T extends Subject> CheckResult check(ItemStackSnapshot snapshot, World world, CheckRuleTrigger trigger, @Nullable T subject) {
            team.ebi.epicbanitem.api.CheckRuleTrigger proxy = getTrigger(trigger.toString()).orElseThrow(IllegalStateException::new);
            return new CheckResult(this.proxy.check(snapshot, world, proxy, subject));
        }

        @Override
        public <T extends Subject> CheckResult check(BlockSnapshot snapshot, World world, CheckRuleTrigger trigger, @Nullable T subject) {
            team.ebi.epicbanitem.api.CheckRuleTrigger proxy = getTrigger(trigger.toString()).orElseThrow(IllegalStateException::new);
            return new CheckResult(this.proxy.check(snapshot, world, proxy, subject));
        }

        @Override
        public <T extends Subject> CheckResult check(ItemStack stack, World world, CheckRuleTrigger trigger, @Nullable T subject) {
            team.ebi.epicbanitem.api.CheckRuleTrigger proxy = getTrigger(trigger.toString()).orElseThrow(IllegalStateException::new);
            return new CheckResult(this.proxy.check(stack, world, proxy, subject));
        }

        @Override
        public Optional<CheckRuleTrigger> getTrigger(String name, boolean registerIfAbsent) {
            Optional<team.ebi.epicbanitem.api.CheckRuleTrigger> optional = getTrigger(name);
            if (!optional.isPresent() && registerIfAbsent) {
                optional = Optional.of(registerTrigger(name));
            }
            return optional.map(CheckRuleTrigger.Proxy::new);
        }

        private team.ebi.epicbanitem.api.CheckRuleTrigger registerTrigger(String name) {
            TriggerImpl impl = new TriggerImpl(name, "epicbanitem:" + name);
            Sponge.getRegistry().register(cls, impl);
            return impl;
        }

        private Optional<team.ebi.epicbanitem.api.CheckRuleTrigger> getTrigger(String name) {
            return Sponge.getRegistry().getType(cls, "epicbanitem:" + name);
        }

        private static class TriggerImpl implements team.ebi.epicbanitem.api.CheckRuleTrigger {

            private String name;
            private String id;

            private TriggerImpl(String name, String id) {
                this.name = name;
                this.id = id;
            }

            @Override
            public String toString() {
                return name;
            }

            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Text toText() {
                return Text.builder(name)
                    .onHover(TextActions.showText(Text.of("")))
                    .build();
            }
        }
    }
}
