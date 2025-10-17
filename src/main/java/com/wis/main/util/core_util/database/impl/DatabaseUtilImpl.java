package com.wis.main.util.core_util.database.impl;

import com.wis.main.util.core_util.database.DatabaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseUtilImpl implements DatabaseUtil {
    private final DataSource dataSource;

    @Autowired
    public DatabaseUtilImpl(DataSource dataSource) {
        System.out.println("DataSource được inject: " + dataSource);
        this.dataSource = dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}

