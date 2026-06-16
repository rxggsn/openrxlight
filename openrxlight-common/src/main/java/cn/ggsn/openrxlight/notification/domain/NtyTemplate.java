package cn.ggsn.openrxlight.notification.domain;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.type.PostgreSQLUUIDJdbcType;

import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(name = "nty_message_template")
@Data
@EqualsAndHashCode(callSuper = true)
public class NtyTemplate extends BaseEntity {

        /**
         * 屏蔽类型
         *
         * @author zh
         */
        @Getter
        @ToString
        @AllArgsConstructor
        public enum ShieldType {

                /**
                 * 模板设置为夜间不屏蔽
                 */
                NIGHT_NO_SHIELD(10, "夜间不屏蔽"),
                /**
                 * 模板设置为夜间屏蔽 -- 凌晨接受到的消息会过滤掉
                 */
                NIGHT_SHIELD(20, "夜间屏蔽"),
                /**
                 * 模板设置为夜间屏蔽(次日早上9点发送) -- 凌晨接受到的消息会次日发送
                 */
                NIGHT_SHIELD_BUT_NEXT_DAY_SEND(30, "夜间屏蔽(次日早上9点发送)");

                private final Integer code;
                private final String description;
        }

        /**
         * 发送ID类型枚举
         *
         * @author zh
         */
        @Getter
        @ToString
        @AllArgsConstructor
        public enum IdType {
                /**
                 * 站内userId
                 */
                USER_ID(10, "userId"),
                /**
                 * 手机设备号
                 */
                DID(20, "did"),
                /**
                 * 手机号
                 */
                PHONE(30, "phone"),
                /**
                 * 微信体系的openId
                 */
                WX_OPEN_ID(40, "wx_open_id"),
                /**
                 * 邮件
                 */
                EMAIL(50, "email"),
                /**
                 * 企业微信userId
                 */
                WXCOM_USER_ID(60, "wxcom_user_id"),
                /**
                 * 钉钉userId
                 */
                DINGTALK_USER_ID(70, "dingtalk_user_id"),
                /**
                 * 个推cid
                 */
                CID(80, "cid"),
                /**
                 * 飞书userId
                 */
                FEI_SHU_USER_ID(90, "fei_shu_user_id"),
                ;

                private final Integer code;
                private final String description;

        }

        @Column(name = "name", nullable = false)
        private String name; // 模板标题

        @Column(name = "status", nullable = false)
        private Short status; // 当前消息状态：10.新建 20.停用 30.启用 40.等待发送 50.发送中 60.发送成功 70.发送失败

        @Column(name = "id_type", nullable = false)
        private Short idType; // 发送的Id类型，消息的发送ID类型：10. userId 20.did 30.手机号 40.openId 50.email 60.企业微信userId

        @Column(name = "channel_config_id", nullable = false)
        private Long configId; // 关联的通道配置账号 （邮件下可有多个发送账号、短信可有多个发送账号..）

        @Column(name = "shield_type", nullable = false)
        private Short shieldType; // 屏蔽类型，10.夜间不屏蔽 20.夜间屏蔽 30.夜间屏蔽(次日早上9点发送)

        @Column(name = "crontab", nullable = true)
        private String crontab; // 推送消息的时间,null：立即发送,else：crontab 表达式

        @Column(name = "content")
        private String content; // 消息内容 {} 为占位符

        @Column(name = "account_id", nullable = true)
        @JdbcType(PostgreSQLUUIDJdbcType.class)
        @JdbcTypeCode(org.hibernate.type.SqlTypes.UUID)
        private UUID accountId; // 账号ID, null表示通用模板，支持所有账号使用

        @Column(name = "account_type", nullable = false)
        private Short accountType; // 目标账号类型

        @Column(name = "deleted", nullable = false)
        private Boolean deleted;

        @Column(name = "scene_type", nullable = false)
        private Short sceneType; // 场景类型

        public static List<NtyTemplate> getByAccountIdAndAccountType(UUID accountId, AccountType accountType) {
                CriteriaBuilder cb = NtyTemplate.getEntityManager().getCriteriaBuilder();
                var query = cb.createQuery(NtyTemplate.class);
                var root = query.from(NtyTemplate.class);

                return NtyTemplate.getEntityManager().createQuery(
                                query.select(root).where(cb.and(
                                                cb.or(Lists2.of(cb.equal(root.get("accountId"), accountId),
                                                                cb.isNull(root.get("accountId")))),
                                                cb.equal(root.get("accountType"), accountType.getValue()),
                                                cb.equal(root.get("deleted"), false),
                                                cb.equal(root.get("status"), MessageStatus.RUN.getCode()))))
                                .getResultList();
        }

        public static List<NtyTemplate> getBySceneTypeAndAccountTypes(NtySceneType sceneType,
                        List<AccountType> accountTypes) {
                CriteriaBuilder cb = NtyTemplate.getEntityManager().getCriteriaBuilder();
                var query = cb.createQuery(NtyTemplate.class);
                var root = query.from(NtyTemplate.class);

                return NtyTemplate.getEntityManager().createQuery(
                                query.select(root).where(cb.and(
                                                root.get("sceneType").equalTo(sceneType.getCode()),
                                                root.get("accountType")
                                                                .in(Lists2.map(accountTypes, ty -> ty.getValue())),
                                                cb.equal(root.get("deleted"), false),
                                                cb.equal(root.get("status"), MessageStatus.RUN.getCode()))))
                                .getResultList();
        }

        public NtySceneType checkSceneType() {
                return NtySceneType.fromCode(this.sceneType);
        }
}
