package com.ahsmart.campusmarket.service.order;

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
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OrderItemDeliveryStatusSchemaCompatibilityService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OrderItemDeliveryStatusSchemaCompatibilityService.class);
    private static final String ORDER_ITEMS_TABLE = "order_items";
    private static final String DELIVERY_STATUS_COLUMN = "delivery_status";
    private static final String MYSQL_ENUM_DEFINITION =
            "ENUM('PENDING','IN_CAMPUS','DELIVERED','RECEIVED') NOT NULL DEFAULT 'PENDING'";

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureReceivedDeliveryStatusExists();
    }

    void ensureReceivedDeliveryStatusExists() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            String databaseName = normalizeDatabaseName(metadata.getDatabaseProductName());
            if (!databaseName.equals("mysql") && !databaseName.equals("mariadb")) {
                return;
            }

            String columnType = jdbcTemplate.query(
                    "SELECT COLUMN_TYPE FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    ps -> {
                        ps.setString(1, ORDER_ITEMS_TABLE);
                        ps.setString(2, DELIVERY_STATUS_COLUMN);
                    },
                    rs -> rs.next() ? rs.getString(1) : null
            );

            if (columnType == null || columnType.toUpperCase(Locale.ROOT).contains("'RECEIVED'")) {
                return;
            }

            jdbcTemplate.execute(
                    "ALTER TABLE " + ORDER_ITEMS_TABLE +
                            " MODIFY COLUMN " + DELIVERY_STATUS_COLUMN + " " + MYSQL_ENUM_DEFINITION
            );
            log.info("Extended {}.{} to include RECEIVED.", ORDER_ITEMS_TABLE, DELIVERY_STATUS_COLUMN);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update order_items.delivery_status for RECEIVED.", ex);
        }
    }

    private String normalizeDatabaseName(String databaseProductName) {
        return databaseProductName == null ? "" : databaseProductName.toLowerCase(Locale.ROOT);
    }
}
