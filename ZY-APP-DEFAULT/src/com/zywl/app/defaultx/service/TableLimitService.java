package com.zywl.app.defaultx.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.util.Executer;
import com.zywl.app.defaultx.dbutil.DaoService;

@Service
public class TableLimitService extends DaoService {

	private static final Log logger = LogFactory.getLog(TableLimitService.class);
	
	private final static Map<String, SimpleDateFormat> TABLE_LIMIT = new ConcurrentHashMap<String, SimpleDateFormat>();

	private final static Map<String, List<TableInfo>> TABLE_INFOS = new ConcurrentHashMap<String, List<TableInfo>>();
	
	private static boolean READ_ONLY = false;
	
	public TableLimitService() {
		super("TableLimitMapper");
		
		new Timer("分表监控").schedule(new TimerTask() {
			public void run() {
				Executer.executeService(new Runnable() {
					public void run() {
						try {
							for(String tablePrefix : TABLE_LIMIT.keySet()) {
								Date date = new Date();
								String lastTableName = getLastTableName(tablePrefix);
								String newTableName = getTableName(tablePrefix, date);
								if(!newTableName.equals(lastTableName)) {
									if(!READ_ONLY){
										createNewTable(newTableName, lastTableName);
									}
									List<TableInfo> list = TABLE_INFOS.get(tablePrefix);
									TableInfo newTableInfo = new TableInfo();
									newTableInfo.date = date;
									newTableInfo.tableName = newTableName;
									list.add(newTableInfo);
								}
							}
						} catch (Exception e) {
							logger.error("自动分表异常：" + e, e);
						}
					}
				});
			}
		}, 0, 60000);
	}
	
	public void setReadOnly(boolean readOnly){
		logger.info("分表监控只读状态变更：" + readOnly);
		READ_ONLY = readOnly;
	}
	
	public void regist(String tablePrefix, SimpleDateFormat dateFormat) {
		JSONObject parameters = new JSONObject();
		parameters.put("tablePrefix", tablePrefix);
		List<String> tableNames = findList("findTables", parameters);
		List<TableInfo> _tableInfos = new ArrayList<TableInfo>();
		for (String tableName : tableNames) {
			String dateStr = tableName.replaceAll(tablePrefix, "");
			try {
				Date date = dateFormat.parse(dateStr);
				TableInfo tableInfo = new TableInfo();
				tableInfo.tableName = tableName;
				tableInfo.date = date;
				_tableInfos.add(tableInfo);
			} catch (ParseException e) {
				logger.error(e, e);
			}
		}
		
		Collections.sort(_tableInfos, new Comparator<TableInfo>() {
			public int compare(TableInfo arg0, TableInfo arg1) {
				return arg0.date.compareTo(arg1.date);
			}
		});
		
		TABLE_LIMIT.put(tablePrefix, dateFormat);
		TABLE_INFOS.put(tablePrefix, new CopyOnWriteArrayList<TableInfo>(_tableInfos));
	}
	
	@Transactional
	public void createNewTable(String newTableName, String oldTableName) {
		String tableSQL = getCreateTableSQL(oldTableName);
		String sql = tableSQL.replaceFirst(oldTableName, newTableName);
		
		JSONObject params = new JSONObject();
		params.put("sql", sql);
		execute("createNewTable", params);
	}
	
	public String getLastTableName(String tablePrefix) {
		TableInfo lastTableInfo = getLastTableInfo(tablePrefix);
		if(lastTableInfo != null) {
			return lastTableInfo.tableName;
		}
		return null;
	}
	
	public TableInfo getLastTableInfo(String tablePrefix) {
		List<TableInfo> list = TABLE_INFOS.get(tablePrefix);
		if(list != null && !list.isEmpty()) {
			return list.get(list.size() - 1);
		}
		return null;
	}
	
	public List<String> getAllTableNames(String tablePrefix){
		List<String> result = new ArrayList<String>();
		List<TableInfo> list = TABLE_INFOS.get(tablePrefix);
		if(list != null && !list.isEmpty()) {
			for (TableInfo tableInfo : list) {
				result.add(tableInfo.tableName);
			}
		}
		return result;
	}
	
	public String getTableName(String tablePrefix, Date date) {
		SimpleDateFormat simpleDateFormat = TABLE_LIMIT.get(tablePrefix);
		return tablePrefix + simpleDateFormat.format(date);
	}
	
	public String getCreateTableSQL(String tableName){
		JSONObject params = new JSONObject();
		params.put("tableName", tableName);
		@SuppressWarnings("unchecked")
		Map<String, String> findOne = (Map<String, String>) findOne("getCreateTableSQL", params);
		return findOne.get("Create Table");
	}

	public class TableInfo{
		public String tableName;
		
		public Date date;
	}

	@Override
	protected Log logger() {
		return logger;
	}
	
}
