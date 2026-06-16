package cn.ggsn.openrxlight.notification.domain;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonbType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.notification.domain.model.AlipayMiniProgramContentModel;
import cn.ggsn.openrxlight.notification.domain.model.ContentModel;
import cn.ggsn.openrxlight.notification.domain.model.DingDingRobotContentModel;
import cn.ggsn.openrxlight.notification.domain.model.DingDingWorkContentModel;
import cn.ggsn.openrxlight.notification.domain.model.EmailContentModel;
import cn.ggsn.openrxlight.notification.domain.model.EnterpriseWeChatContentModel;
import cn.ggsn.openrxlight.notification.domain.model.EnterpriseWeChatRobotContentModel;
import cn.ggsn.openrxlight.notification.domain.model.FeiShuRobotContentModel;
import cn.ggsn.openrxlight.notification.domain.model.ImContentModel;
import cn.ggsn.openrxlight.notification.domain.model.MiniProgramContentModel;
import cn.ggsn.openrxlight.notification.domain.model.OfficialAccountsContentModel;
import cn.ggsn.openrxlight.notification.domain.model.PhoneVoiceContentModel;
import cn.ggsn.openrxlight.notification.domain.model.PushContentModel;
import cn.ggsn.openrxlight.notification.domain.model.SmsContentModel;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.openrxlight.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

/**
 * @author other
 *         渠道账号信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nty_channel_configuration")
@EqualsAndHashCode(callSuper = true)
public class ChannelConfiguration extends BaseEntity {

    /**
     * 发送渠道类型枚举
     *
     * @author zh
     */
    @Getter
    @ToString
    @AllArgsConstructor
    public enum ChannelType {

        /**
         * IM(站内信)
         */
        IM(10, "IM(站内信)", ImContentModel.class, "im"),
        /**
         * push(通知栏) --安卓 已接入 个推
         */
        PUSH(20, "push(通知栏)", PushContentModel.class, "push"),
        /**
         * sms(短信) -- 腾讯云、云片
         */
        SMS(30, "sms(短信)", SmsContentModel.class, "sms"),
        /**
         * email(邮件) -- QQ、163邮箱
         */
        EMAIL(40, "email(邮件)", EmailContentModel.class, "email"),
        /**
         * officialAccounts(微信服务号) -- 官方测试账号
         */
        OFFICIAL_ACCOUNT(50, "officialAccounts(服务号)", OfficialAccountsContentModel.class, "official_accounts"),
        /**
         * miniProgram(微信小程序)
         */
        MINI_PROGRAM(60, "miniProgram(小程序)", MiniProgramContentModel.class, "mini_program"),
        /**
         * enterpriseWeChat(企业微信)
         */
        WX_COM_CHAT(70, "enterpriseWeChat(企业微信)", EnterpriseWeChatContentModel.class, "enterprise_we_chat"),
        /**
         * dingDingRobot(钉钉机器人)
         */
        DING_TALK_ROBOT(80, "dingDingRobot(钉钉机器人)", DingDingRobotContentModel.class, "ding_ding_robot"),
        /**
         * dingDingWorkNotice(钉钉工作通知)
         */
        DINGTALK_WORK_NOTICE(90, "dingDingWorkNotice(钉钉工作通知)", DingDingWorkContentModel.class,
                "ding_ding_work_notice"),
        /**
         * wxCom(企业微信机器人)
         */
        WX_COM_ROBOT(100, "wxCom(企业微信机器人)", EnterpriseWeChatRobotContentModel.class,
                "wx_com_robot"),
        /**
         * feiShuRoot(飞书机器人)
         */
        FEI_SHU_ROBOT(110, "feiShuRoot(飞书机器人)", FeiShuRobotContentModel.class, "fei_shu_robot"),
        /**
         * alipayMiniProgram(支付宝小程序)
         */
        ALIPAY_MINI_PROGRAM(120, "alipayMiniProgram(支付宝小程序)", AlipayMiniProgramContentModel.class,
                "alipay_mini_program"),
        /**
         * phoneVoice(电话语音)
         */
        PHONE_VOICE(130, "phoneVoice(电话语音)", PhoneVoiceContentModel.class, "phone_voice"),
        ;

        /**
         * 编码值
         */
        private final Integer code;

        /**
         * 描述
         */
        private final String description;

        /**
         * 内容模型Class
         */
        private final Class<? extends ContentModel> contentModel;

        /**
         * 英文标识
         */
        private final String codeEn;

        /**
         * 通过code获取class
         *
         * @param code
         * @return
         */
        public static Class<? extends ContentModel> getChanelModelClazzByCode(Integer code) {
            return Arrays.stream(values()).filter(channelType -> Objects.equals(code, channelType.getCode()))
                    .map(ChannelType::getContentModel)
                    .findFirst().orElse(null);
        }

        public static ChannelType fromCode(Integer code) {
            for (ChannelType type : ChannelType.values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
    public interface ChannelAccount {
        boolean supports(NtySceneType sceneType);

        default <T extends ChannelAccount> Optional<T> getAccount(Class<T> clazz) {
            if (clazz.isAssignableFrom(this.getClass())) {
                return Optional.of(clazz.cast(this));
            }
            return Optional.ofNullable(JsonUtils.fromJson(JsonUtils.toJson(this), clazz));
        }
    }

    // @Data
    // @NoArgsConstructor
    // @AllArgsConstructor
    // @Builder
    // private static class ChannelAccountsWrapper {

    // private List<ChannelAccount> accounts;
    // }

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "type", nullable = false)
    private Integer type;
    @Column(name = "accounts", nullable = true)
    @JdbcType(PostgreSQLJsonPGObjectJsonbType.class)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private List<Map<String, Object>> accounts;
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    public ChannelType checkChannelType() {
        return ChannelType.fromCode(this.type);
    }

    // @JsonIgnore
    // public List<ChannelAccount> getChannelAccounts() {
    // if (this.accounts == null) {
    // return Lists2.empty();
    // }
    // return this.accounts.getAccounts();
    // }

    @JsonIgnore
    @Transient
    public <T extends ChannelAccount> List<T> getAccounts(Class<T> clazz) {
        if (this.accounts == null) {
            return Lists2.empty();
        }
        return Lists2.map(this.accounts, account -> {
            try {
                return JsonUtils.fromJson(JsonUtils.toJson(account), clazz);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize channel account: " + account, e);
            }
        });
    }
}
