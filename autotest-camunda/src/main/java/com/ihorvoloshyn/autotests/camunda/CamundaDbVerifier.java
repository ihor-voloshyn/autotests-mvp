package com.ihorvoloshyn.autotests.camunda;

import com.ihorvoloshyn.autotests.db.JdbcClient;

import java.util.List;
import java.util.Map;

public class CamundaDbVerifier {

    private final JdbcClient database;

    public CamundaDbVerifier(JdbcClient database) {
        if (database == null) {
            throw new IllegalArgumentException("database must not be null");
        }
        this.database = database;
    }

    public List<Map<String, Object>> findHistoricProcessInstances(String processInstanceId) {
        return database.query(
                """
                select *
                from act_hi_procinst
                where proc_inst_id_ = ?
                """,
                processInstanceId);
    }

    public List<Map<String, Object>> findHistoricVariables(String processInstanceId) {
        return database.query(
                """
                select *
                from act_hi_varinst
                where proc_inst_id_ = ?
                """,
                processInstanceId);
    }

    public boolean processInstanceExists(String processInstanceId) {
        return !findHistoricProcessInstances(processInstanceId).isEmpty();
    }
}