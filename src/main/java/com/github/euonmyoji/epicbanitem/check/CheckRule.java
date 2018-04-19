package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryExpression;
import com.github.euonmyoji.epicbanitem.util.nbt.UpdateExpression;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yinyangshi
 */
public class CheckRule {
    private List<String> enableWorlds;
    private List<String> ignorePermissions;
    private List<String> enableTrigger;
    private QueryExpression query;
    private UpdateExpression update;


    private CheckRule(List<String> enableWorlds, List<String> ignorePermissions, List<String> enableTrigger, QueryExpression query, UpdateExpression update) {
        this.enableWorlds = enableWorlds;
        this.ignorePermissions = ignorePermissions;
        this.enableTrigger = enableTrigger;
        this.query = query;
        this.update = update;
    }

    /**
     * @param item       被检查的物品
     * @param world      检查发生世界名
     * @param trigger    检查发生trigger
     * @param permission 被检查的物品的...的权限
     * @param from       检查发生地点
     * @return 检查结果
     */
    public CheckResult checkItemStack(ItemStack item, String world, String trigger, @Nullable String permission, @Nullable Location from) {
        boolean ban = false;
        CheckResult.Builder builder = CheckResult.builder().setFrom(from).setWorld(world).setCheckedObject(item);
        if (enableTrigger.contains(trigger) && !ignorePermissions.contains(permission) && enableWorlds.contains(world)) {
            ban = query.query(DataQuery.of(), NbtTagDataUtil.toNbt(item)).isPresent();
        }
        return ban ? builder.setBanned(true).setBreakRule(this).build() :
                builder.setBanned(false).setRemove(update != null).setUpdateExpression(update).build();
    }

    public static class Serializer implements TypeSerializer<CheckRule> {

        @Override
        public CheckRule deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            TypeToken<String> strType = TypeToken.of(String.class);
            return new CheckRule(value.getNode("bypass-permissions").getList(strType, new ArrayList<>()),
                    value.getNode("enabled-worlds").getList(strType, new ArrayList<>()),
                    value.getNode("use-trigger").getList(strType, new ArrayList<>()),
                    new QueryExpression(value), new UpdateExpression(value));
        }

        @Override
        public void serialize(TypeToken<?> type, CheckRule obj, ConfigurationNode value) throws ObjectMappingException {
            TypeToken<List<String>> strType = new TypeToken<List<String>>() {
            };
            value.getNode("bypass-permissions").setValue(strType, obj.ignorePermissions);
            value.getNode("enabled-worlds").setValue(strType, obj.enableWorlds);
            value.getNode("use-trigger").setValue(strType, obj.enableTrigger);
            value.getNode("query").setValue(obj.query);
            value.getNode("update").setValue(obj.update);
        }
    }
}
