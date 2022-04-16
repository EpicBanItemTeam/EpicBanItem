package team.ebi.epicbanitem.api.expression;

import org.spongepowered.api.data.persistence.DataView;

@FunctionalInterface
public interface ModifyExpression {

  /**
   * @param result The result from test expression
   * @param data NBT view to modify
   * @return result
   */
  ModifyResult process(TestResult result, DataView data);
}
