package cn.ggsn.openrxlight.dbx;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.duckdb.DuckDBConnection;
import org.eclipse.microprofile.config.inject.ConfigProperties;

import cn.ggsn.openrxlight.lang.Lists2;
import io.quarkus.arc.properties.IfBuildProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@IfBuildProperty(name = "quarkus.datasourceext.duckdb.enabled", stringValue = "true")
@ConfigProperties(prefix = "quarkus.datasourceext.duckdb")
public interface DuckDBDatasourceFactory {
    public Optional<Boolean> enabled();

    public String url();

    public String baseDir();

    public Optional<List<Secret>> secrets();

    public default DuckDBDatasource duckDBDatasource() throws Exception {
        DuckDBConnection connection = DuckDBConnection.newConnection(this.url(), false, null);
        Lists2.foreach(this.secrets().orElse(null), secret -> {
            String secretInitialization = null;
            if (StringUtils.equals(secret.getType(), "s3")) {
                secretInitialization = String.format("CREATE OR REPLACE SECRET %s (\n" + //
                        "    TYPE %s,\n" + //
                        "    PROVIDER config,\n" + //
                        "    KEY_ID '%s',\n" + //
                        "    SECRET '%s',\n" + //
                        "    ENDPOINT '%s',\n" + // Added endpoint
                        "    REGION '%s'\n" + //
                        ");",
                        secret.getName(),
                        secret.getType(),
                        secret.getKeyId(),
                        secret.getSecret(),
                        secret.getEndpoint(), // Added endpoint
                        secret.getRegion());
            }

            try {
                if (StringUtils.isNotBlank(secretInitialization)) {
                    connection.prepareStatement(secretInitialization).execute();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        return new DuckDBDatasource(connection);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Secret {
        private String endpoint;
        private String name;
        private String type;
        private String keyId;
        private String secret;
        private String region;
    }
}
