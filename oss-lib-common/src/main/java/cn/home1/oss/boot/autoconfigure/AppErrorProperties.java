package cn.home1.oss.boot.autoconfigure;

import static cn.home1.oss.boot.autoconfigure.AppErrorProperties.SearchStrategy.HIERARCHY_FIRST;
import static java.lang.Boolean.FALSE;

import lombok.Data;

@SuppressWarnings({"PMD.ImmutableField", "PMD.SingularField", "PMD.UnusedPrivateField"})
@Data
public class AppErrorProperties {

  public enum SearchStrategy {

    ORDER_FIRST, HIERARCHY_FIRST
  }

  /**
   * Experimental.
   */
  private Boolean handlerEnabled;

  /**
   * ORDER_FIRST, HIERARCHY_FIRST.
   */
  private SearchStrategy searchStrategy;

  public AppErrorProperties() {
    this.handlerEnabled = FALSE;
    this.searchStrategy = HIERARCHY_FIRST;
  }
}
