package team.ebi.epicbanitem.api;

import org.spongepowered.api.data.persistence.DataQuery;

public final class ItemQueries {
  public static final DataQuery UNSAFE_DAMAGE = DataQuery.of("UnsafeDamage");
  public static final DataQuery ITEM_TYPE = DataQuery.of("ItemType");
  public static final DataQuery CREATOR =
      DataQuery.of("UnsafeData", "BlockEntityTag", "sponge-data", "sponge", "creator_tracked");
}
