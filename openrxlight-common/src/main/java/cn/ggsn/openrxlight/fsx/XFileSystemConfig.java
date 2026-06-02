package cn.ggsn.openrxlight.fsx;

import java.util.Optional;

import io.quarkus.arc.properties.IfBuildProperty;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "rxlight.fs")
@IfBuildProperty(name = "rxlight.fs.enabled", stringValue = "true", enableIfMissing = true)
public interface XFileSystemConfig {
    Optional<String> endpoint();

    Optional<String> accessKey();

    Optional<String> secretKey();

    Optional<String> region();

    FsType type();

    String bucketName();

    Optional<String> localBasePath();
}
