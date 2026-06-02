package cn.ggsn.openrxlight.utils;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UUIdConverterTest {

    @Test
    void testFromHyphenRemovedString() {
        UUID original = cn.ggsn.openrxlight.lang.UUID.randomUUID();
        String withoutHyphens = original.toString().replace("-", "");

        UUID result = UUIdConverter.fromHyphenRemovedString(withoutHyphens);

        assertEquals(original, result);
    }
}
