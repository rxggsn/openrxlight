package cn.ggsn.openrxlight.dbx;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.duckdb.DuckDBConnection;

import cn.hutool.db.ds.simple.AbstractDataSource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DuckDBDatasource extends AbstractDataSource {
    private final DuckDBConnection connection;

    @Override
    public Connection getConnection() throws SQLException {
        return this.connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return this.connection;
    }

    @Override
    public void close() throws IOException {
        try {
            this.connection.close();
        } catch (SQLException e) {
            throw new IOException("Failed to close DuckDB connection", e);
        }
    }

}
