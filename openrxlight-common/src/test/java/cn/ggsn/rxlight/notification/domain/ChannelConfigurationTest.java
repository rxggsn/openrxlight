package cn.ggsn.rxlight.notification.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration;
import cn.ggsn.openrxlight.notification.domain.NtySceneType;
import cn.ggsn.openrxlight.utils.JsonUtils;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@QuarkusTest
class ChannelConfigurationTest {

    @Test
    @TestTransaction
    void testPersistAndQuery() {
        // 创建测试渠道配置
        ChannelConfiguration config = ChannelConfiguration.builder()
                .name("Test Channel")
                .type(ChannelConfiguration.ChannelType.SMS.getCode())
                .accounts(Lists2.of(Map.of("name", "test-account", "typename", "test-type", "type",
                        TestChannelAccount.class.getName())))
                .deleted(false)
                .createdTime(LocalDateTime.now())
                .build();
        System.out.println(JsonUtils.toJson(config.getAccounts(TestChannelAccount.class)));

        // 持久化
        config.save();
        Assertions.assertNotNull(config.id);

        // 通过 ID 查询
        ChannelConfiguration foundById = ChannelConfiguration.findById(config.id);
        Assertions.assertNotNull(foundById);
        Assertions.assertEquals("Test Channel", foundById.getName());
        Assertions.assertEquals(ChannelConfiguration.ChannelType.SMS.getCode(), foundById.getType());
        Assertions.assertFalse(foundById.getDeleted());
        Assertions.assertNotNull(foundById.getAccounts());
        Assertions.assertEquals(1, foundById.getAccounts().size());
        Assertions.assertEquals("test-account", ((TestChannelAccount) foundById.getAccounts().get(0)).getName());
        Assertions.assertEquals("test-type", ((TestChannelAccount) foundById.getAccounts().get(0)).getTypename());

        // 通过名称查询
        ChannelConfiguration foundByName = ChannelConfiguration.find("name", "Test Channel").firstResult();
        Assertions.assertNotNull(foundByName);
        Assertions.assertEquals(config.id, foundByName.id);

        // 通过类型查询
        List<ChannelConfiguration> configsByType = ChannelConfiguration.list("type",
                ChannelConfiguration.ChannelType.SMS.getCode());
        Assertions.assertNotNull(configsByType);
        Assertions.assertFalse(configsByType.isEmpty());

        // 验证查询结果包含刚创建的记录
        boolean contains = configsByType.stream()
                .anyMatch(c -> c.id.equals(config.id));
        Assertions.assertTrue(contains);

        ChannelConfiguration.deleteById(config.id);
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    private static class TestChannelAccount implements ChannelConfiguration.ChannelAccount {
        private String name;
        private String typename;

        @Override
        public boolean supports(NtySceneType sceneType) {
            return true;
        }
    }
}
