package com.zywl.app.base.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class JSONUtil {

	public static JSONArray sortArray(JSONArray array ,String condition){
		array.sort((Object o1, Object o2) -> {
			// 强制转换为JSONObject
			JSONObject json1Obj = (JSONObject) o1;
			JSONObject json2Obj = (JSONObject) o2;
			// 获取status字段并进行比较
			Integer status1 = json1Obj.getInteger(condition);
			Integer status2 = json2Obj.getInteger(condition);

			// 按status值从大到小排序
			return status2.compareTo(status1);
		});
		return array;
	}
	
	
	public static JSONObject getReturnDate(int code,Object data,String message) {
		JSONObject result = new JSONObject();
		result.put("code",code);
		result.put("data", data);
		result.put("message", message);
		return result;
	}
	public static JSONArray mergeJSONArray(JSONArray array1, JSONArray array2) {
		// 使用一个Map存储合并后的数据
		Map<String, BigDecimal> mergedData = new HashMap<>();

		// 遍历第一个JSONArray并合并到mergedData
		processJSONArray(array1, mergedData);

		// 遍历第二个JSONArray并合并到mergedData
		processJSONArray(array2, mergedData);

		// 将合并后的数据转换回JSONArray
		JSONArray result = new JSONArray();
		for (Map.Entry<String, BigDecimal> entry : mergedData.entrySet()) {
			String[] keys = entry.getKey().split(":");
			int id = Integer.parseInt(keys[0]);
			int type = Integer.parseInt(keys[1]);

			JSONObject obj = new JSONObject();
			obj.put("id", id);
			obj.put("type", type);
			obj.put("number", entry.getValue());
			result.add(obj);
		}

		return result;
	}

	// 处理单个JSONArray的数据合并
	private static void processJSONArray(JSONArray array, Map<String, BigDecimal> mergedData) {
		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = array.getJSONObject(i);
			int id = obj.getIntValue("id");
			int type = obj.getIntValue("type");
			BigDecimal number = obj.getBigDecimal("number");

			// 使用 "id:type" 的形式作为Map的键，存储对应的number值
			String key = id + ":" + type;
			mergedData.put(key, mergedData.getOrDefault(key, BigDecimal.ZERO) .add( number ));
		}
	}

	// 创建一个 JSONObject 的辅助方法
	// 创建一个 JSONObject 的辅助方法
	private static JSONObject createJsonObject(int type, int id, BigDecimal number) {
		JSONObject obj = new JSONObject();
		obj.put("type", type);
		obj.put("id", id);
		obj.put("number", number);
		return obj;
	}

	public static JSONArray mergeAndCalculateDifference(JSONArray jsonArray1, JSONArray jsonArray2) {
		// 使用 Map 存储第一个 JSONArray 的 (type, id) 和 number
		Map<String, Integer> map1 = new HashMap<>();
		jsonArray1.forEach(obj -> {
			JSONObject jsonObj = (JSONObject) obj;
			String key = createKey(jsonObj.getString("type"), jsonObj.getInteger("id"));
			map1.put(key, jsonObj.getInteger("number"));
		});

		// 结果数组
		JSONArray resultArray = new JSONArray();

		// 遍历第二个 JSONArray，计算差值
		jsonArray2.forEach(obj -> {
			JSONObject jsonObj = (JSONObject) obj;
			String type = jsonObj.getString("type");
			int id = jsonObj.getInteger("id");
			int number2 = jsonObj.getInteger("number");

			// 生成联合键 (type, id)
			String key = createKey(type, id);

			// 如果第一个 JSONArray 中有对应的 key，计算差值
			if (map1.containsKey(key)) {
				int number1 = map1.get(key);
				int finalNumber = number2 - number1;

				// 差值不为 0 时加入结果
				if (finalNumber != 0) {
					JSONObject resultObj = new JSONObject();
					resultObj.put("type", type);
					resultObj.put("id", id);
					resultObj.put("number", finalNumber);
					resultArray.add(resultObj);
				}

				// 从 map1 中移除，表示已处理
				map1.remove(key);
			} else {
				// 如果第一个 JSONArray 中没有对应的 key，直接添加正数值
				JSONObject resultObj = new JSONObject();
				resultObj.put("type", type);
				resultObj.put("id", id);
				resultObj.put("number", number2);
				resultArray.add(resultObj);
			}
		});

		// 处理第一个 JSONArray 中未出现在第二个 JSONArray 的数据
		map1.forEach((key, number1) -> {
			String[] parts = key.split("#");
			String type = parts[0];
			int id = Integer.parseInt(parts[1]);

			// 添加负数值
			JSONObject resultObj = new JSONObject();
			resultObj.put("type", type);
			resultObj.put("id", id);
			resultObj.put("number", -number1);
			resultArray.add(resultObj);
		});

		return resultArray;
	}

	// 辅助方法：生成联合键 (type, id)
	private static String createKey(String type, int id) {
		return type + "#" + id;
	}

}

