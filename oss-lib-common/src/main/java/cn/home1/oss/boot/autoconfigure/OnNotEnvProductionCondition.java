package cn.home1.oss.boot.autoconfigure;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

/**
 * Created on 16/9/26.
 *
 * @author Yuliang Jin
 */
@Slf4j
public class OnNotEnvProductionCondition extends SpringBootCondition {

  @Override
  public ConditionOutcome getMatchOutcome( //
      final ConditionContext context, //
      final AnnotatedTypeMetadata metadata //
  ) {
    final MultiValueMap<String, Object> allAnnotationAttributes = metadata.getAllAnnotationAttributes( //
        ConditionalOnNotEnvProduction.class.getName(), true //
    );

    final String environment = AppProperties.getEnvironment(context.getEnvironment());
    final Boolean prodEnvironment = AppProperties.getProdEnvironment(environment);
    final ConditionOutcome outcome;
    if (allAnnotationAttributes != null && prodEnvironment) {
      outcome = ConditionOutcome.noMatch("Environment: " + environment + " not match, is production environment.");
    } else {
      outcome = ConditionOutcome.match();
    }
    return outcome;
  }
}
