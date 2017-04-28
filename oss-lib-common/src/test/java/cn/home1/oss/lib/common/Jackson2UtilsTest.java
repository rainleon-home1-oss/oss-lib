package cn.home1.oss.lib.common;

import static org.junit.Assert.assertTrue;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

import java.util.Set;

import cn.home1.oss.lib.common.Jackson2Configurator.BuildinJackson2Configurators;

/**
 * Created by zhanghaolun on 16/11/15.
 */
@Slf4j
public class Jackson2UtilsTest {

  @Test
  public void testScanJackson2Configurators() {
    final Set<Jackson2Configurator> configurators = Jackson2Utils.scanJackson2Configurators();
    log.info("configurators: {}", configurators);
    assertTrue(configurators.contains(BuildinJackson2Configurators.JACKSON2_DATETIME_CONFIGURATOR));
  }
}
