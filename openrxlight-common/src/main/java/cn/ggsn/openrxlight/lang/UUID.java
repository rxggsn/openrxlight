package cn.ggsn.openrxlight.lang;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;

public class UUID {
    private static final TimeBasedEpochGenerator GENERATOR = Generators.timeBasedEpochGenerator();

    public static java.util.UUID randomUUID() {
        return GENERATOR.generate();
    }
}
