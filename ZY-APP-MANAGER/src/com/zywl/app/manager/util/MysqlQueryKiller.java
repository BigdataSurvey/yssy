package com.zywl.app.manager.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class MysqlQueryKiller {
    // MySQL 连接配置
    private static final String URL = "jdbc:mysql://rm-bp1sfq939izoac33x.mysql.rds.aliyuncs.com:3306/kapai-db?allowMultiQueries=true&autoReconnect=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&serverTimezone=GMT%2B8";
    private static final String USER = "root";
    private static final String PASSWORD = "bCZbgkUr9pQVlN7m";

    public static void main(String[] args) {
        System.out.println("[INFO] MySQL Query Killer started...");

        while (true) {
            try {
                checkAndKillLongRunningQueries();
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("[ERROR] " + e.getMessage());
            }
        }
    }

    private static void checkAndKillLongRunningQueries() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String sql = "SELECT ID FROM INFORMATION_SCHEMA.PROCESSLIST WHERE COMMAND != 'Sleep' AND TIME > 8";
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    int processId = rs.getInt("ID");
                    String killSql = "KILL " + processId;
                    try (PreparedStatement killStmt = conn.prepareStatement(killSql)) {
                        killStmt.execute();
                        System.out.println("[INFO] Killed long-running query: " + processId);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to check or kill queries: " + e.getMessage());
        }
    }
}
