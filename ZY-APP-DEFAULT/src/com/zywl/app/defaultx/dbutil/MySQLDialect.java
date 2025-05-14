package com.zywl.app.defaultx.dbutil;

/**
 * Mysql数据库方言
 * @author DOE
 *
 */
public class MySQLDialect extends Dialect{
    @Override
    public boolean supportsLimit() {
        return true;
    }
 
    @Override
    public String getLimitString(final String sql, final int offset, final int limit) {
    	StringBuilder stringBuilder = new StringBuilder(getLineSql(sql));
        stringBuilder.append(" limit ");
        if(offset > 0){
            stringBuilder.append(Integer.toString(offset)).append(",").append(Integer.toString(limit));
        }else{
            stringBuilder.append(Integer.toString(limit));
        }

        return stringBuilder.toString();
    }
}
