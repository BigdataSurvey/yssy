package com.zywl.app.defaultx.handler;

import com.alibaba.fastjson2.JSONArray;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author: lzx
 * @Create: 2025/12/24
 * @Version: V1.0
 * @Description: 声明这个Handler专门处理 JSONArray 类型
 */
@MappedTypes(JSONArray.class)
public class FastJson2JsonArrayHandler extends BaseTypeHandler<JSONArray> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JSONArray parameter, JdbcType jdbcType) throws SQLException {
        // 插入数据库时将JSONArray转为JSON 字符串
        ps.setString(i, parameter.toJSONString());
    }

    @Override
    public JSONArray getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 查询时将数据库的String转回JSONArray
        String sqlJson = rs.getString(columnName);
        return parse(sqlJson);
    }

    @Override
    public JSONArray getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 查询时（根据索引）：将数据库的 String 转回 JSONArray
        String sqlJson = rs.getString(columnIndex);
        return parse(sqlJson);
    }

    @Override
    public JSONArray getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 存储过程调用时
        String sqlJson = cs.getString(columnIndex);
        return parse(sqlJson);
    }

    private JSONArray parse(String json) {
        if (json == null || json.length() == 0) {
            return null;
        }
        return JSONArray.parseArray(json);
    }
}