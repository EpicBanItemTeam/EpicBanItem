package team.ebi.epicbanitem.api;

import org.spongepowered.api.data.persistence.DataQuery;

public final class ItemQueries {
  public static final DataQuery UNSAFE_DAMAGE = DataQuery.of("UnsafeDamage");
  public static final DataQuery ITEM_TYPE = DataQuery.of("ItemType");
  public static final DataQuery CREATOR = DataQuery.of("sponge-data", "sponge", "creator_tracked");
  public static final DataQuery BLOCK_ENTITY_TAG = DataQuery.of("BlockEntityTag");
  public static final DataQuery BLOCK_ID = BLOCK_ENTITY_TAG.then("id");

  public static final DataQuery X = BLOCK_ENTITY_TAG.then("x");

  public static final DataQuery Y = BLOCK_ENTITY_TAG.then("y");

  public static final DataQuery Z = BLOCK_ENTITY_TAG.then("z");

  private ItemQueries() {}
}
