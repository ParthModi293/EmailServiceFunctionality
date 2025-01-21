package com.example.emailservice.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

@SuppressWarnings("JavadocDeclaration")
@Repository
public class SqlUtil {

    private static final Logger log = LogManager.getLogger(SqlUtil.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public SqlUtil() {
    }

    /**
     * @return
     */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * return underlying connection
     *
     * @return
     */
    public Connection getConnection() {
        Connection conn = null;
        try {
            // conn = (Connection) jdbcTemplate.getDataSource().getConnection();
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource()); // underlying connection
        } catch (CannotGetJdbcConnectionException ex) {
        }
        return conn;

    }

    /**
     * Insert Records
     *
     * @param query
     * @return no of records affected.
     * @throws Exception
     */
    public int persist(final String query) throws Exception {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((Connection connection) -> {
            PreparedStatement ps = connection.prepareStatement(query);
            return ps;
        }, keyHolder);
        return Integer.parseInt(keyHolder.getKey().toString());
    }

    /**
     * Insert Records with parameter
     *
     * @param query
     * @param paramMap
     * @return
     */
    public int persist(String query, SqlParameterSource paramMap) throws DataAccessException {
        NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
        return namedTempalte.update(query, paramMap);
    }

    /**
     * Insert Records and return auto generated ID.
     *
     * @param sql
     * @param paramMap
     */
    public int persistKey(String sql, SqlParameterSource paramMap) throws DataAccessException {
        // Assert.doesNotContain("INSERT", sql);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        @SuppressWarnings("unchecked")
        SqlParameterSource paramkey = new MapSqlParameterSource((Map<String, ?>) paramMap);
        NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
        namedTempalte.update(sql, paramkey, keyHolder);
        if (keyHolder.getKey() == null) {
            // insert igonre into... statement will have no auto id.
            return 0;
        }
        return Integer.parseInt(keyHolder.getKey().toString());
    }

    /**
     * Insert Records
     *
     * @param query
     * @param paramMap
     * @return
     */
    public int persistKey(String query, MapSqlParameterSource paramMap) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
            namedTempalte.update(query, paramMap, keyHolder);
        } catch (DataAccessException ex) {
            throw ex;
        }
        return Integer.parseInt(keyHolder.getKey().toString());
    }

    public int persistKey(String query, MapSqlParameterSource paramMap, String idKey) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
            namedTempalte.update(query, paramMap, keyHolder, new String[]{idKey});
        } catch (DataAccessException ex) {
            throw ex;
        }
        return Integer.parseInt(keyHolder.getKey().toString());
    }

    /**
     * Operation: Method to update or delete record in database
     *
     * @param sql static SQL to execute
     * @return the number of rows affected
     */
    public int updateDelete(String sql) throws DataAccessException {
        return jdbcTemplate.update(sql);
    }

    /**
     * Operation: Method to update or delete record in database
     *
     * @param sql static SQL to execute
     * @return the number of rows affected
     */
    public int updateDelete(String sql, Map<String, Object> params) throws DataAccessException {
        try {
            NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
            return namedTempalte.update(sql, params);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    public void execute(String sql) {
        jdbcTemplate.execute(sql);
    }

    /**
     * Operation: Method returns list from database
     *
     * @param sql
     * @return List<Map < String, Object>>
     */
    public List<Map<String, Object>> getList(String sql) throws DataAccessException {
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Return Single Record in Map
     *
     * @param sql
     * @return
     * @throws DataAccessException
     */

    public Map getMap(String sql) throws DataAccessException {
        return jdbcTemplate.queryForMap(sql);
    }

    /**
     * Return Single Record in Map
     *
     * @param sql
     * @param paramMap
     * @return
     * @throws DataAccessException
     */
    public Map getMap(String sql, SqlParameterSource paramMap) throws DataAccessException {
        try {
            NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
            return namedTempalte.queryForMap(sql, paramMap);
        }catch(EmptyResultDataAccessException exception){
            return null;
        }
    }

    public Map getMapOptional(String sql, SqlParameterSource paramMap) throws DataAccessException {
        try {
            NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
            return namedTempalte.queryForMap(sql, paramMap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * The results will be mapped to a List (one entry for each row) of Maps (one
     * entry for each column, using the column name as the key).
     *
     * @param sql
     * @param paramMap
     * @return List<Map < String, Object>>
     */
    public List<Map<String, Object>> getList(String sql, SqlParameterSource paramMap) throws DataAccessException {
        NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
        return namedTempalte.queryForList(sql, paramMap);
    }

    /**
     * get String List
     *
     * @param sql
     * @param paramMap
     * @return
     * @throws DataAccessException
     */
    public List<String> getStringList(String sql, SqlParameterSource paramMap) throws DataAccessException {
        NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
        return namedTempalte.queryForList(sql, paramMap, String.class);
    }

    /**
     * get Long List
     *
     * @param sql
     * @param paramMap
     * @return
     * @throws DataAccessException
     */
    public List<Long> getLongList(String sql, SqlParameterSource paramMap) throws DataAccessException {
        NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
        return namedTempalte.queryForList(sql, paramMap, Long.class);
    }

    /**
     * get Integer List
     *
     * @param sql
     * @param paramMap
     * @return
     * @throws DataAccessException
     */
    public List<Integer> getIntegerList(String sql, SqlParameterSource paramMap) throws DataAccessException {
        NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
        return namedTempalte.queryForList(sql, paramMap, Integer.class);
    }

    /**
     * Execute a query for a result Integer
     *
     * @param sql
     * @return
     * @throws Exception
     */
    public int getInteger(String sql) {
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Execute a query for a result Integer
     *
     * @param sql
     * @param parmaMap
     * @return
     * @throws Exception
     */
    public int getInteger(String sql, SqlParameterSource parmaMap) {
        try {
            NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
            return namedTempalte.queryForObject(sql, parmaMap, Integer.class);
        } catch (DataAccessException ex) {
            log.error("getInteger", ex);
            return 0;
        }
    }

    /**
     * Execute a query for a result Double
     *
     * @param sql
     * @return
     * @throws Exception
     */
    public double getDouble(String sql) throws Exception {
        try {
            return jdbcTemplate.queryForObject(sql, Double.class);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * get Double Value
     *
     * @param sql
     * @param parmaMap
     * @return
     * @throws Exception
     */
    public double getDouble(String sql, SqlParameterSource parmaMap) {
        try {
            NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
            return namedTempalte.queryForObject(sql, parmaMap, Double.class);
        } catch (DataAccessException ex) {
            return 0;
//            throw ex;
        }
    }

    /**
     * Execute a query for a result String
     *
     * @param sql
     * @return
     * @throws Exception
     */
    public String getString(String sql) throws Exception {
        try {
            return jdbcTemplate.queryForObject(sql, String.class);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Execute a query for a result String
     *
     * @param sql
     * @param parmaMap
     * @return
     * @throws Exception
     */
    public String getString(String sql, SqlParameterSource parmaMap) {
        try {
            NamedParameterJdbcTemplate namedTempalte = new NamedParameterJdbcTemplate(jdbcTemplate);
            return namedTempalte.queryForObject(sql, parmaMap, String.class);
        } catch (DataAccessException ex) {
//            throw ex;
            return null;
        }
    }

    /**
     * Execute procedure
     *
     * @param sProcName
     * @param paramMap
     * @return
     * @throws Exception
     */
    public Map callProcedure(String sProcName, Map paramMap) throws Exception {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName(sProcName);
        SqlParameterSource in = new MapSqlParameterSource(paramMap);
        Map<String, Object> out = jdbcCall.execute(in);
        return out;
    }

    public Object getBean(String sql, Map parmaMap, Class object) {
        try {
            Object obj = object.getDeclaredConstructor().newInstance();
            MapSqlParameterSource param = new MapSqlParameterSource(parmaMap);
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            return namedTemplate.queryForObject(sql, param, new BeanPropertyRowMapper<>(obj.getClass()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//
    public <T> List<T> getBeanList(String sql, Map parmaMap, T object) {
        try {
            MapSqlParameterSource param = new MapSqlParameterSource(parmaMap);
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            return namedTemplate.query(sql, param, new BeanPropertyRowMapper(object.getClass()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
