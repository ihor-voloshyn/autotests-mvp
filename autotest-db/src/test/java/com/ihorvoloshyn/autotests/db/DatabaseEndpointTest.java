package com.ihorvoloshyn.autotests.db;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseEndpointTest {
    @Test void schemaIsMandatory() {
        assertThrows(IllegalArgumentException.class, () -> new DatabaseEndpoint(
                DatabaseType.POSTGRESQL,"db",null,"testdb","", "user","pass"));
    }
    @Test void postgresDefaultPort() {
        var e=new DatabaseEndpoint(DatabaseType.POSTGRESQL,"db",null,"testdb","client","u","p");
        assertEquals(5432,e.effectivePort());
    }
    @Test void oracleDefaultPort() {
        var e=new DatabaseEndpoint(DatabaseType.ORACLE,"db",null,"ORCL","CLIENT","u","p");
        assertEquals(1521,e.effectivePort());
    }
    @Test void customPortIsPreserved() {
        var e=new DatabaseEndpoint(DatabaseType.POSTGRESQL,"10.0.0.1",15432,"testdb","client","u","p");
        assertEquals(15432,e.effectivePort());
    }
}