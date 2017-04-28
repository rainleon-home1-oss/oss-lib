package cn.home1.oss.boot.autoconfigure;

import org.springframework.beans.factory.annotation.Value;

/**
 * Created by zhanghaolun on 16/9/22.
 */
@SuppressWarnings({"PMD.ImmutableField", "PMD.SingularField", "PMD.UnusedPrivateField"})
public class AppLogProperties {

  @Value("${log.flag:logTrace}")
  private String flag = "logTrace";
}
