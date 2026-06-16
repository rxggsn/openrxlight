package cn.ggsn.openrxlight.dbx;

import java.util.Optional;

import org.duckdb.DuckDBConnection;
import io.quarkus.arc.properties.IfBuildProperty;
import io.smallrye.config.ConfigMapping;

@IfBuildProperty(name = "quarkus.datasourceext.duckdb", stringValue = "true")
@ConfigMapping(prefix = "quarkus.datasourceext.duckdb")
public interface DuckDBDatasourceFactory {
    public Optional<Boolean> enabled();

    public String url();

    public default DuckDBDatasource duckDBDatasource() throws Exception {
        DuckDBConnection connection = DuckDBConnection.newConnection(this.url(), false, null);
        return new DuckDBDatasource(connection);
    }

}
