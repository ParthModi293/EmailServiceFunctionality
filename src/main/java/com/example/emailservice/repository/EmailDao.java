package com.example.emailservice.repository;

import com.example.emailservice.util.SqlUtil;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class EmailDao {

    private final SqlUtil sqlUtil;

    public EmailDao(SqlUtil sqlUtil) {
        this.sqlUtil = sqlUtil;
    }

    public Map getMailDetails(String templateName) {
        Map<String, String> map = new HashMap();
        map.put("templateName", templateName);

        String sql = """
                select td.subject as "subject", td.body as "body", td.version as "version", tm.priority as "priority"
                from template_details td
                inner join template_mast tm on td.template_mast_id = tm.id
                where tm.template_name = :templateName and is_active = 'Y'
                """;
        return sqlUtil.getMap(sql, new MapSqlParameterSource(map));

    }

}
