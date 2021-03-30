package com.mqi.address.jsonutil;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class JacksonUtil {
	private static final Logger log = LoggerFactory.getLogger(JacksonUtil.class);

	private static final ObjectMapper mapper;

	static {

		mapper = new ObjectMapper();

		//序列化时候统一日期格式

		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

		//设置null时候不序列化(只针对对象属性)

		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		//反序列化时，属性不存在的兼容处理

		mapper.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		//单引号处理

		mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

	}



	public static String BeanToJson(Object obj) throws IOException {
		StringWriter sw = new StringWriter();
		JsonGenerator gen = new JsonFactory().createGenerator(sw);
		mapper.writeValue(gen, obj);
		gen.close();
		return sw.toString();
	}
	public static String parseString(String body, String field) {
		JsonNode node = null;
		try {
			node = mapper.readTree(body);
			JsonNode leaf = node.get(field);
			if (leaf != null)
				return leaf.asText();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<String> parseStringList(String body, String field) {
		JsonNode node = null;
		try {
			node = mapper.readTree(body);
			JsonNode leaf = node.get(field);

			if (leaf != null)
				return mapper.convertValue(leaf, new TypeReference<List<String>>() {
				});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Integer parseInteger(String body, String field) {
		JsonNode node = null;
		try {
			node = mapper.readTree(body);
			JsonNode leaf = node.get(field);
			if (leaf != null)
				return leaf.asInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<Integer> parseIntegerList(String body, String field) {
		JsonNode node = null;
		try {
			node = mapper.readTree(body);
			JsonNode leaf = node.get(field);

			if (leaf != null)
				return mapper.convertValue(leaf, new TypeReference<List<Integer>>() {
				});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Boolean parseBoolean(String body, String field) {
		JsonNode node = null;
		try {
			node = mapper.readTree(body);
			JsonNode leaf = node.get(field);
			if (leaf != null)
				return leaf.asBoolean();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Short parseShort(String body, String field) {
		JsonNode node = null;
		try {
			node = mapper.readTree(body);
			JsonNode leaf = node.get(field);
			if (leaf != null) {
				Integer value = leaf.asInt();
				return value.shortValue();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Byte parseByte(String body, String field) {
		JsonNode node = null;
		try {
			node = mapper.readTree(body);
			JsonNode leaf = node.get(field);
			if (leaf != null) {
				Integer value = leaf.asInt();
				return value.byteValue();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T> T parseObject(String body, String field, Class<T> clazz) {
		JsonNode node = null;
		try {
			node = mapper.readTree(body);
			node = node.get(field);
			return mapper.treeToValue(node, clazz);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object toNode(String json) {
		if (json == null) {
			return null;
		}
		try {
			JsonNode jsonNode = mapper.readTree(json);
			return jsonNode;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	//将json转成相应的List

	public static <T> List<T>  parseObjectToList(String jsonStr,Class<T> clazz) {

		JavaType javaType = JacksonUtil.mapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);

		try {

			return mapper.readValue(jsonStr, javaType);

		} catch (IOException e) {

			log.error(e.getMessage(), e);

		}

		return null;

	}


	/**
	 * 根据对象类型转换
	 * @param content
	 * @param valueType
	 * @param <T>
	 * @return
	 */
	public static <T> T readValue(String content, Class<T> valueType) {

		if (StringUtils.isEmpty(content) || null == valueType) {
			return null;
		}
		try {
			return mapper.readValue(content, valueType);
		} catch (Exception e) {
			return null;
		}
	}

}
