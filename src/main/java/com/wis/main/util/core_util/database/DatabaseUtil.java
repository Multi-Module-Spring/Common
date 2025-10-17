package com.wis.main.util.core_util.database;

import java.sql.Connection;
import java.sql.SQLException;


public interface DatabaseUtil {
    Connection getConnection() throws SQLException;
}

