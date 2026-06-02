package cn.ggsn.openrxlight.utils;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.random.RandomGenerator;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.DataCrypto;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.errorx.CommonErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MessageIdGenerator {
    private static final String PREFIX = "opr";

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OriginMessageId {
        public OriginMessageId(String decrypt) {
            try {
                this.timestamp = LocalDateTime.parse(StringUtils.substring(decrypt, 0, 14),
                        Constants.QUERY_DATE_TIME_FORMATTER);
                this.organizationId = UUIdConverter.replaceEmptyWithHyphen(StringUtils.substring(decrypt, 14, 46));
                this.accountType = AccountType.fromValue(Integer.parseInt(StringUtils.substring(decrypt, 46, 48)));
                this.accountId = UUIdConverter.replaceEmptyWithHyphen(StringUtils.substring(decrypt, 48, 80));
                this.randomSuffix = Integer.parseInt(StringUtils.substring(decrypt, 80, 86));
            } catch (Exception e) {
                throw new IllegalArgumentException("not a valid message id", e);
            }
        }

        private LocalDateTime timestamp;
        private UUID organizationId;
        private AccountType accountType;
        private UUID accountId;
        private int randomSuffix;
    }

    public static String generate(
            AccountType accountType,
            UUID accountId,
            UUID organizationId,
            String secret,
            DataCrypto dataCrypto) {
        if (StringUtils.isBlank(secret)) {
            throw new BizException(CommonErrorCode.CommandKeyIsRequired,
                    "client secret is required to generate message id");
        }
        var now = LocalDateTime.now();
        var originId = StringUtils.join(new String[] {
                now.format(Constants.QUERY_DATE_TIME_FORMATTER),
                UUIdConverter.replaceHyphenWithEmptyChar(organizationId),
                accountType.getValue() < 10 ? "0" + accountType.getValue() : String.valueOf(accountType.getValue()),
                UUIdConverter.replaceHyphenWithEmptyChar(accountId),
                String.valueOf(RandomGenerator.getDefault().nextInt(100000, 1000000)) // 6-digit random number
        });

        try {
            return String.format("%s-%s", PREFIX, Hex.encodeHexString(
                    Base64.getDecoder()
                            .decode(EncryptUtil.encrypt(originId, secret, "123456abcdef", dataCrypto))));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate message id", e);
        }

    }

    public static OriginMessageId parse(String messageId, String clientSecret, DataCrypto dataCrypto) {
        if (StringUtils.isBlank(messageId) || !messageId.startsWith(PREFIX + "-")) {
            throw new IllegalArgumentException("Invalid message id format");
        }
        String encryptedPart = messageId.substring((PREFIX + "-").length());
        try {
            String decrypt = EncryptUtil.decrypt(Base64.getEncoder().encodeToString(Hex.decodeHex(encryptedPart)),
                    clientSecret, "123456abcdef", dataCrypto);
            if (StringUtils.isBlank(decrypt)) {
                throw new IllegalArgumentException("Decryption resulted in empty string");
            }

            return new OriginMessageId(decrypt);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid message id or decryption failed", e);
        }
    }
}
