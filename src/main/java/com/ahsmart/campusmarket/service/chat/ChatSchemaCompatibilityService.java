package com.ahsmart.campusmarket.service.chat;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatSchemaCompatibilityService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ChatSchemaCompatibilityService.class);
    private static final String CHAT_TABLE = "chats";
    private static final String ORDER_ID_COLUMN = "order_id";
    private static final String ORDER_ID_LOOKUP_INDEX = "idx_chats_order_id_lookup";

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        removeLegacyUniqueOrderIdIndex();
    }

    public void removeLegacyUniqueOrderIdIndex() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            String tableName = resolveTableName(metadata, connection.getCatalog(), connection.getSchema(), CHAT_TABLE);
            if (tableName == null) {
                return;
            }

            Map<String, IndexMetadata> indexes = loadIndexes(metadata, connection.getCatalog(), connection.getSchema(), tableName);
            List<String> legacyUniqueIndexes = indexes.values().stream()
                    .filter(IndexMetadata::isUnique)
                    .filter(index -> index.hasOnlyColumn(ORDER_ID_COLUMN))
                    .map(IndexMetadata::name)
                    .toList();

            if (legacyUniqueIndexes.isEmpty()) {
                return;
            }

            if (indexes.values().stream().noneMatch(index -> !index.isUnique() && index.hasOnlyColumn(ORDER_ID_COLUMN))) {
                createLookupIndex(metadata.getDatabaseProductName(), tableName);
            }

            for (String indexName : legacyUniqueIndexes) {
                dropIndex(metadata.getDatabaseProductName(), tableName, indexName);
                log.info("Dropped legacy unique index {} on {}.{}", indexName, tableName, ORDER_ID_COLUMN);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to repair legacy chats.order_id schema for per-item chats.", ex);
        }
    }

    private Map<String, IndexMetadata> loadIndexes(DatabaseMetaData metadata,
                                                   String catalog,
                                                   String schema,
                                                   String tableName) throws SQLException {
        Map<String, IndexMetadata> indexes = new LinkedHashMap<>();
        try (ResultSet resultSet = metadata.getIndexInfo(catalog, schema, tableName, false, false)) {
            while (resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                short type = resultSet.getShort("TYPE");
                if (indexName == null || columnName == null || type == DatabaseMetaData.tableIndexStatistic) {
                    continue;
                }

                boolean nonUnique = resultSet.getBoolean("NON_UNIQUE");
                indexes.computeIfAbsent(indexName, name -> new IndexMetadata(name, !nonUnique))
                        .addColumn(columnName);
            }
        }
        return indexes;
    }

    private String resolveTableName(DatabaseMetaData metadata,
                                    String catalog,
                                    String schema,
                                    String expectedTableName) throws SQLException {
        List<String> candidateSchemas = new ArrayList<>();
        candidateSchemas.add(schema);
        candidateSchemas.add(null);

        for (String schemaCandidate : candidateSchemas) {
            try (ResultSet tables = metadata.getTables(catalog, schemaCandidate, null, new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    if (tableName != null && tableName.equalsIgnoreCase(expectedTableName)) {
                        return tableName;
                    }
                }
            }
        }
        return null;
    }

    private void createLookupIndex(String databaseProductName, String tableName) {
        String sql = switch (normalizeDatabaseName(databaseProductName)) {
            case "mysql", "mariadb" ->
                    "CREATE INDEX " + ORDER_ID_LOOKUP_INDEX + " ON " + tableName + "(" + ORDER_ID_COLUMN + ")";
            case "h2", "postgresql" ->
                    "CREATE INDEX IF NOT EXISTS " + ORDER_ID_LOOKUP_INDEX + " ON " + tableName + "(" + ORDER_ID_COLUMN + ")";
            default ->
                    "CREATE INDEX " + ORDER_ID_LOOKUP_INDEX + " ON " + tableName + "(" + ORDER_ID_COLUMN + ")";
        };

        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ex) {
            String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);
            if (!message.contains("already exists") && !message.contains("duplicate")) {
                throw ex;
            }
        }
    }

    private void dropIndex(String databaseProductName, String tableName, String indexName) {
        String safeIndexName = sanitizeIdentifier(indexName);
        String sql = switch (normalizeDatabaseName(databaseProductName)) {
            case "mysql", "mariadb" ->
                    "ALTER TABLE " + tableName + " DROP INDEX " + safeIndexName;
            case "h2", "postgresql" ->
                    "DROP INDEX IF EXISTS " + safeIndexName;
            default ->
                    "DROP INDEX " + safeIndexName;
        };
        jdbcTemplate.execute(sql);
    }

    private String sanitizeIdentifier(String identifier) {
        if (identifier == null || !identifier.matches("[A-Za-z0-9_]+")) {
            throw new IllegalStateException("Unsafe database identifier encountered: " + identifier);
        }
        return identifier;
    }

    private String normalizeDatabaseName(String databaseProductName) {
        return databaseProductName == null ? "" : databaseProductName.toLowerCase(Locale.ROOT);
    }

    private static final class IndexMetadata {
        private final String name;
        private final boolean unique;
        private final Set<String> columns = new LinkedHashSet<>();

        private IndexMetadata(String name, boolean unique) {
            this.name = name;
            this.unique = unique;
        }

        private void addColumn(String columnName) {
            columns.add(columnName.toLowerCase(Locale.ROOT));
        }

        private boolean hasOnlyColumn(String columnName) {
            return columns.size() == 1 && columns.contains(columnName.toLowerCase(Locale.ROOT));
        }

        private boolean isUnique() {
            return unique;
        }

        private String name() {
            return name;
        }
    }
}
