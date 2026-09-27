package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.db.JdbcClient;

public final class DatabaseConnectionCheck implements ConnectionCheck {
    private final String name;
    private final JdbcClient client;
    public DatabaseConnectionCheck(String name, JdbcClient client){
        if(name==null||name.isBlank())throw new IllegalArgumentException("name must not be blank");
        if(client==null)throw new IllegalArgumentException("client must not be null");
        this.name=name;this.client=client;
    }
    public ConnectionCheckResult check(){
        long start=System.nanoTime();
        if(!client.isValid(5))return ConnectionCheckResult.failure(name,"JDBC connection validation failed",elapsed(start));
        if(!client.isSchemaAccessible())return ConnectionCheckResult.failure(name,"Configured database schema is not accessible",elapsed(start));
        return ConnectionCheckResult.success(name,"JDBC connection and configured schema are accessible",elapsed(start));
    }
    private static long elapsed(long start){return(System.nanoTime()-start)/1_000_000;}
}