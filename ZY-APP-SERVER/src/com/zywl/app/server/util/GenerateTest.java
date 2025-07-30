package com.zywl.app.server.util;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class GenerateTest {

    /// 数据库连接配置
    private static final String DB_URL = "jdbc:mysql://localhost:3306/tsg-db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    private static final String DB_DRIVER ="com.mysql.jdbc.Driver";


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // 加载数据库驱动
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("找不到数据库驱动类: " + DB_DRIVER);
            System.err.println("请确保已添加相应的JDBC驱动依赖");
            return;
        }
        // 获取包名
        System.out.print("请输入包名 (例如: com.example.entity): ");
        String packageName = scanner.nextLine().trim();

        // 获取输出目录
        System.out.print("请输入输出目录 (直接回车使用当前目录): ");
        String outputDir = scanner.nextLine().trim();

        // 获取表名列表
        System.out.println("请输入表名（输入空行结束，支持多个表，用逗号或空格分隔）:");
        System.out.println("例如: user,order,product 或 user order product");
        System.out.print("> ");
        String tableNamesInput = scanner.nextLine().trim();

        // 解析表名
        List<String> tableNames = new ArrayList<>();
        if (!tableNamesInput.isEmpty()) {
            // 支持逗号、空格、制表符分隔的表名
            String[] names = tableNamesInput.split("[,\\s]+");
            tableNames.addAll(Arrays.asList(names));
        }

        if (tableNames.isEmpty()) {
            System.out.println("未输入表名，程序终止");
            return;
        }

        System.out.println("\n准备生成以下表的实体类:");
        for (String tableName : tableNames) {
            System.out.println("- " + tableName);
        }
        System.out.println();

        // 批量生成实体类
        generateEntities(tableNames, packageName, outputDir);

        System.out.println("=====================================");
        System.out.printf("共处理 %d 个表，成功生成 %d 个实体类%n",
                tableNames.size(),
                tableNames.size());

        scanner.close();
    }


    // 字段信息类
    static class Field{
        private String name;//字段名称
        private String value;//字段类型
        private String comment;//字段注释

        public Field(String name, String value, String comment) {

            this.name = name;
            this.value = value;
            this.comment = comment;
        }
        public String getName() {return name;}
        public String getValue() {return value;}
        public String getComment() {return comment;}
    }

    //生成实体类
    public static String generateEntityCode(String className, List<Field> fields,String packageName){
        StringBuilder sb = new StringBuilder();

        //添加包声明
        if(packageName !=null && packageName.isEmpty()){
            sb.append("package ").append(packageName).append(";");
        }

        //添加必要的导入
        sb.append("ipport lobok.Data;\n");
        sb.append("import java.io.Serializable;\n");

        // 添加类注释
        sb.append("/**\n");
        sb.append(" * ").append(className).append(" 实体类\n");
        sb.append(" */\n");

        //添加类声明
        sb.append(" @Data\n");
        sb.append("public class ").append(className).append(" {\n");
        sb.append("    private static final long serialVersionUID = 1L;\n");

        //添加类字段
        for (Field field : fields) {
            sb.append("    /**\n");
            sb.append("     * ").append(field.getComment()).append("\n");
            sb.append("     */\n");
            sb.append("    private ").append(field.getValue()).append(" ").append(field.getName()).append(";\n\n");
        }
        // 类结束
        sb.append("}");
        return sb.toString();
    }

    // 将代码写入文件地址
    public static void writeToFile(String code, String filePath){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))){
            writer.write(code);
            System.out.println("实体类已成功生成到项目目录下：" + filePath );
        }catch (IOException e){
            System.out.println("写入文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 从表名获取类名（将下划线命名转为驼峰命名）
    public static String tableNameToClassName(String tableName) {
        StringBuilder className = new StringBuilder();
        String[] parts = tableName.toLowerCase().split("_");
        for (String part : parts) {
            if (!part.isEmpty()) {
                className.append(Character.toUpperCase(part.charAt(0)));
                className.append(part.substring(1));
            }
        }
        return className.toString();
    }

    // 从列名获取字段名（将下划线命名转为驼峰命名）
    public static String columnNameToFieldName(String columnName) {
        StringBuilder fieldName = new StringBuilder();
        String[] parts = columnName.toLowerCase().split("_");
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                if (i == 0) {
                    fieldName.append(parts[i]);
                } else {
                    fieldName.append(Character.toUpperCase(parts[i].charAt(0)));
                    fieldName.append(parts[i].substring(1));
                }
            }
        }
        return fieldName.toString();
    }

    // 将SQL类型转换为Java类型
    public static String sqlTypeToJavaType(String sqlType) {
        sqlType = sqlType.toUpperCase();
        if (sqlType.contains("INT")) {
            return "Integer";
        } else if (sqlType.contains("BIGINT")) {
            return "Long";
        } else if (sqlType.contains("VARCHAR") || sqlType.contains("CHAR") || sqlType.contains("TEXT")) {
            return "String";
        } else if (sqlType.contains("DECIMAL") || sqlType.contains("NUMERIC")) {
            return "BigDecimal";
        } else if (sqlType.contains("DATE") || sqlType.contains("TIME") || sqlType.contains("TIMESTAMP")) {
            return "LocalDateTime";
        } else if (sqlType.contains("BOOLEAN")) {
            return "Boolean";
        } else if (sqlType.contains("FLOAT")) {
            return "Float";
        } else if (sqlType.contains("DOUBLE")) {
            return "Double";
        }
        return "Object"; // 默认使用Object类型
    }

    // 获取表的字段信息
    public static List<Field> getTableFields(String tableName) {
        List<Field> fields = new ArrayList<>();
        String url = DB_URL + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC";
        try (Connection conn = DriverManager.getConnection(url, DB_USER, DB_PASSWORD)) {
            // 获取列信息
            String columnSql = "SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT " +
                    "FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(columnSql)) {
                pstmt.setString(1, conn.getCatalog());
                pstmt.setString(2, tableName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String columnName = rs.getString("COLUMN_NAME");
                        String columnType = rs.getString("COLUMN_TYPE");
                        String columnComment = rs.getString("COLUMN_COMMENT");

                        String fieldName = columnNameToFieldName(columnName);
                        String javaType = sqlTypeToJavaType(columnType);

                        fields.add(new Field(fieldName, javaType, columnComment));
                    }
                }
            }

            System.out.println("成功获取表 " + tableName + " 的结构信息");
        } catch (SQLException e) {
            System.err.println("获取表 " + tableName + " 结构时出错: " + e.getMessage());
            e.printStackTrace();
        }

        return fields;
    }


    // 批量生成实体类
    public static void generateEntities(List<String> tableNames, String packageName, String outputDir) {
        for (String tableName : tableNames) {
            try {
                // 获取表结构
                List<Field> fields = getTableFields(tableName);
                if (fields.isEmpty()) {
                    System.out.println("警告: 表 " + tableName + " 的结构信息为空，跳过生成");
                    continue;
                }
                // 生成类名
                String className = tableNameToClassName(tableName);
                System.out.println("表名 " + tableName + " 对应的类名将是: " + className);

                // 生成代码
                String entityCode = generateEntityCode(className, fields, packageName);

                // 写入文件
                String filePath = (outputDir.isEmpty() ? "" : outputDir + "/") + className + ".java";
                writeToFile(entityCode, filePath);

                System.out.println();
            } catch (Exception e) {
                System.err.println("生成表 " + tableName + " 的实体类时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}







