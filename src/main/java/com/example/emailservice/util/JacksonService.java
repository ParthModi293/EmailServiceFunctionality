package com.example.emailservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JacksonService {

    private static final JacksonService INSTANCE = new JacksonService();

    private ObjectMapper mapper;

    private JacksonService() {
        super();
        mapper = new ObjectMapper();
    }

    public static JacksonService getInstance() {
        return INSTANCE;
    }

//	public static JSONObject getJsonObject(Object pojo) {
//		return new JSONObject(JacksonService.getInstance().convertToJsonString(pojo));
//	}
//
//	public static JSONArray getJsonArray(Object pojo) {
//		return new JSONArray(JacksonService.getInstance().convertToJsonString(pojo));
//	}

    /**
     * Converts DTO object to Json String
     *
     * @param objectDto
     * @return
     */
    public String convertToJsonString(Object objectDto) {
        try {
            this.mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return mapper.writeValueAsString(objectDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts String to DTO object for the provided target class
     *
     * @param jsonString
     * @param target
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T convertJsonToDto(String jsonString, Class<?> target) {
        try {
            this.mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return (T) mapper.readValue(jsonString, target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public <T> T convertMapToDto(Map map, Class<?> target) { // NOSONAR generic solution to convert any map to POJO
        this.mapper = new ObjectMapper();
        return (T) mapper.convertValue(map, target); // NOSONAR generic solution to convert any map to POJO
    }

    public <T> List<T> convertJsonToDtoList(String jsonString, TypeReference<List<T>> target) {
        try {
            this.mapper = new ObjectMapper();
            return mapper.readValue(jsonString, target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static  <T> T getDataFromJson(JSONObject ipInfoJson, String key, Class<T> type){
        if(type == Integer.class && ipInfoJson.has(key)){
            return type.cast(ipInfoJson.getInt(key));
        }else if(type == String.class && ipInfoJson.has(key)){
            return type.cast(ipInfoJson.getString(key));
        }else if(type == Double.class && ipInfoJson.has(key)){
            return type.cast(ipInfoJson.getDouble(key));
        }else if(type == Float.class && ipInfoJson.has(key)){
            return type.cast(ipInfoJson.getFloat(key));
        }else if(type == Long.class && ipInfoJson.has(key)){
            return type.cast(ipInfoJson.getLong(key));
        }else{
            return null;
        }
    }
}
