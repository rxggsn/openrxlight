package cn.ggsn.openrxlight.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.ggsn.openrxlight.DataCrypto;
import cn.ggsn.openrxlight.domain.AccountType;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

class MessageIdGeneratorTest {
    private static final String SECRET = EncryptUtil.generateNonce(32);
    private static final DataCrypto DATA_CRYPTO = DataCrypto.AES_GCM;

    @Test
    @DisplayName("generate and parse round-trip - CONSUMER account")
    void testRoundTrip_consumer() {
        UUID orgId = cn.ggsn.openrxlight.lang.UUID.randomUUID();
        UUID accountId = cn.ggsn.openrxlight.lang.UUID.randomUUID();

        String messageId = MessageIdGenerator.generate(
                AccountType.CONSUMER, accountId, orgId, SECRET, DATA_CRYPTO);

        assertTrue(messageId.startsWith("opr-"), "message id should start with 'opr-'");

        MessageIdGenerator.OriginMessageId parsed = MessageIdGenerator.parse(messageId, SECRET, DATA_CRYPTO);

        assertEquals(orgId, parsed.getOrganizationId());
        assertEquals(AccountType.CONSUMER, parsed.getAccountType());
        assertEquals(accountId, parsed.getAccountId());
        assertNotNull(parsed.getTimestamp());
        assertTrue(parsed.getRandomSuffix() >= 100000 && parsed.getRandomSuffix() < 1000000);
    }

    @Test
    @DisplayName("generate and parse round-trip - MERCHANT account")
    void testRoundTrip_merchant() {
        UUID orgId = cn.ggsn.openrxlight.lang.UUID.randomUUID();
        UUID accountId = cn.ggsn.openrxlight.lang.UUID.randomUUID();

        String messageId = MessageIdGenerator.generate(
                AccountType.MERCHANT, accountId, orgId, SECRET, DATA_CRYPTO);

        MessageIdGenerator.OriginMessageId parsed = MessageIdGenerator.parse(messageId, SECRET, DATA_CRYPTO);

        assertEquals(orgId, parsed.getOrganizationId());
        assertEquals(AccountType.MERCHANT, parsed.getAccountType());
        assertEquals(accountId, parsed.getAccountId());
    }

    @Test
    @DisplayName("generate and parse round-trip - ANONYMOUS account")
    void testRoundTrip_anonymous() {
        UUID orgId = cn.ggsn.openrxlight.lang.UUID.randomUUID();
        UUID accountId = cn.ggsn.openrxlight.lang.UUID.randomUUID();

        String messageId = MessageIdGenerator.generate(
                AccountType.ANONYMOUS, accountId, orgId, SECRET, DATA_CRYPTO);

        MessageIdGenerator.OriginMessageId parsed = MessageIdGenerator.parse(messageId, SECRET, DATA_CRYPTO);

        assertEquals(AccountType.ANONYMOUS, parsed.getAccountType());
    }

    @Test
    @DisplayName("generate and parse round-trip - OPERATOR account")
    void testRoundTrip_operator() {
        UUID orgId = cn.ggsn.openrxlight.lang.UUID.randomUUID();
        UUID accountId = cn.ggsn.openrxlight.lang.UUID.randomUUID();

        String messageId = MessageIdGenerator.generate(
                AccountType.OPERATOR, accountId, orgId, SECRET, DATA_CRYPTO);

        MessageIdGenerator.OriginMessageId parsed = MessageIdGenerator.parse(messageId, SECRET, DATA_CRYPTO);

        assertEquals(AccountType.OPERATOR, parsed.getAccountType());
    }

    @Test
    @DisplayName("generated message ids are unique - randomSuffix differs")
    void testUniqueMessageIds() {
        UUID orgId = cn.ggsn.openrxlight.lang.UUID.randomUUID();
        UUID accountId = cn.ggsn.openrxlight.lang.UUID.randomUUID();

        String id1 = MessageIdGenerator.generate(AccountType.CONSUMER, accountId, orgId, SECRET, DATA_CRYPTO);
        String id2 = MessageIdGenerator.generate(AccountType.CONSUMER, accountId, orgId, SECRET, DATA_CRYPTO);

        assertNotEquals(id1, id2, "each generated message id should be unique");

        var parsed1 = MessageIdGenerator.parse(id1, SECRET, DATA_CRYPTO);
        var parsed2 = MessageIdGenerator.parse(id2, SECRET, DATA_CRYPTO);

        assertNotEquals(parsed1.getRandomSuffix(), parsed2.getRandomSuffix());
    }

    @Test
    @DisplayName("parse rejects null message id")
    void testParse_null() {
        assertThrows(IllegalArgumentException.class,
                () -> MessageIdGenerator.parse(null, SECRET, DATA_CRYPTO));
    }

    @Test
    @DisplayName("parse rejects empty message id")
    void testParse_empty() {
        assertThrows(IllegalArgumentException.class,
                () -> MessageIdGenerator.parse("", SECRET, DATA_CRYPTO));
    }

    @Test
    @DisplayName("parse rejects blank message id")
    void testParse_blank() {
        assertThrows(IllegalArgumentException.class,
                () -> MessageIdGenerator.parse("   ", SECRET, DATA_CRYPTO));
    }

    @Test
    @DisplayName("parse rejects message id without 'opr-' prefix")
    void testParse_missingPrefix() {
        assertThrows(IllegalArgumentException.class,
                () -> MessageIdGenerator.parse("invalid-abc123", SECRET, DATA_CRYPTO));
    }

    @Test
    @DisplayName("parse rejects message id with wrong secret")
    void testParse_wrongSecret() {
        UUID orgId = cn.ggsn.openrxlight.lang.UUID.randomUUID();
        UUID accountId = cn.ggsn.openrxlight.lang.UUID.randomUUID();

        String messageId = MessageIdGenerator.generate(
                AccountType.CONSUMER, accountId, orgId, SECRET, DATA_CRYPTO);

        assertThrows(IllegalArgumentException.class,
                () -> MessageIdGenerator.parse(messageId, "wrong-secret", DATA_CRYPTO));
    }

    @Test
    @DisplayName("parse rejects message id with invalid hex payload")
    void testParse_invalidHex() {
        assertThrows(IllegalArgumentException.class,
                () -> MessageIdGenerator.parse("opr-not-valid-hex!!", SECRET, DATA_CRYPTO));
    }

    @Test
    @DisplayName("parse rejects message id with 'opr-' but empty payload")
    void testParse_emptyPayload() {
        assertThrows(IllegalArgumentException.class,
                () -> MessageIdGenerator.parse("opr-", SECRET, DATA_CRYPTO));
    }

    @Test
    @DisplayName("parse rejects message id with random data after 'opr-'")
    void testParse_randomData() {
        assertThrows(IllegalArgumentException.class,
                () -> MessageIdGenerator.parse("opr-" + "0".repeat(64), SECRET, DATA_CRYPTO));
    }

    @Test
    @DisplayName("generate rejects blank secret")
    void testGenerate_blankSecret() {
        assertThrows(Exception.class,
                () -> MessageIdGenerator.generate(
                        AccountType.CONSUMER, cn.ggsn.openrxlight.lang.UUID.randomUUID(),
                        cn.ggsn.openrxlight.lang.UUID.randomUUID(), "", DATA_CRYPTO));
    }

    @Test
    @DisplayName("round-trip with deterministic org/account id")
    void testRoundTrip_deterministicIds() {
        UUID orgId = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        UUID accountId = UUID.fromString("abcdef00-abcd-abcd-abcd-abcdef000001");

        String messageId = MessageIdGenerator.generate(
                AccountType.CONSUMER, accountId, orgId, SECRET, DATA_CRYPTO);

        MessageIdGenerator.OriginMessageId parsed = MessageIdGenerator.parse(messageId, SECRET, DATA_CRYPTO);

        assertEquals(orgId, parsed.getOrganizationId());
        assertEquals(accountId, parsed.getAccountId());
    }
}
