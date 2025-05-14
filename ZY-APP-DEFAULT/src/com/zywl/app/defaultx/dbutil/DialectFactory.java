package com.zywl.app.defaultx.dbutil;

/**
 * 数据库方言工厂
 * @author DOE
 *
 */
public abstract  class DialectFactory {
    public static Dialect buildDialect(Dialect.Type dialectType){
        switch (dialectType){
            case MYSQL:
                return new MySQLDialect();
            default:
                throw new UnsupportedOperationException();
        }
    } 
}
