package com.example.emailservice.repository;

import com.example.emailservice.util.SqlUtil;
import org.apache.kafka.common.protocol.types.Field;
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

    public Map getMailDetails(String templateName, String db) {
        Map<String, String> map = new HashMap();
        map.put("templateName", templateName);
        String sql = """
                select td.subject subject, td.body body, td.version version, tm.priority priority,
                 td.from_email_id fromEmailId, ec.password password, ec.port port, ec.host host
                """;
        sql += "from " + db + ".template_details td " +
                "INNER JOIN " + db + ".template_mast tm ON td.template_mast_id = tm.id " +
                "INNER JOIN " + db + ".email_configuration ec ON td.from_email_id = ec.user_name " +
                "WHERE tm.template_name = :templateName AND is_active = 'Y' " +
                "ORDER BY td.created_at DESC LIMIT 1";
        return sqlUtil.getMap(sql, new MapSqlParameterSource(map));

    }

}
