package com.zywl.app.defaultx.util;

import com.alibaba.fastjson2.JSONArray;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

public class JSONArrayTypeHandler extends BaseTypeHandler<JSONArray> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JSONArray parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.toJSONString());
    }

    @Override
    public JSONArray getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseSafe(rs.getString(columnName));
    }

    @Override
    public JSONArray getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseSafe(rs.getString(columnIndex));
    }

    @Override
    public JSONArray getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseSafe(cs.getString(columnIndex));
    }

    private JSONArray parseSafe(String json) {
        if (json == null) return new JSONArray();
        String s = json.trim();
        if (s.isEmpty()) return new JSONArray();

        try {
            if (s.startsWith("[")) {
                return JSONArray.parseArray(s);
            }
        } catch (Exception ignore) {
        }

        JSONArray arr = new JSONArray();
        s = s.replace("|", ",");
        if (s.contains(",")) {
            String[] parts = s.split(",");
            for (String p : parts) {
                String v = p.trim();
                if (!v.isEmpty()) {
                    arr.add(v);
                }
            }
        } else {
            arr.add(s);
        }
        return arr;
    }
}
