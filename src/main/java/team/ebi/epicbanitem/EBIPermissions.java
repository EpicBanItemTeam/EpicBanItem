package team.ebi.epicbanitem;

import team.ebi.epicbanitem.api.RestrictionRule;

public final class EBIPermissions {
  public static String bypass(RestrictionRule rule) {
    return EpicBanItem.permission("bypass" + rule.key());
  }
}
