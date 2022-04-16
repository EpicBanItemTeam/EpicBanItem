package team.ebi.epicbanitem.api;

import org.spongepowered.api.ResourceKeyed;

public interface RestrictionRule extends ResourceKeyed {
    /**
     * @return The priority of current rule (ASC, lower first).
     */
    int priority();


}
