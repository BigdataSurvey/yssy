package com.zywl.app.service;

import com.alibaba.fastjson2.JSON;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import net.ipip.ipdb.City;
import net.ipip.ipdb.CityInfo;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <h1>IP离线数据库查询</h1>
 *
 * <a href="https://www.ipip.net/product/client.html">官网</a><br />
 * <a href="https://www.ipip.net/free_download/">数据包下载地址</a>
 *
 * @author DOE
 */
@Service
public class IPService extends BaseService {

    private static City DB;

    private static String LANGUAGE = "CN";

    @PostConstruct
    public void _Construct() {
        loadDB();
        new Timer("更新IP数据库").schedule(new TimerTask() {
            public void run() {
                loadDB();
            }
        }, DateUtil.getTodayLastMillis(), 86400000);
    }


    public void loadDB() {
        try {
            URL resource = this.getClass().getResource("/ipipfree.ipdb");
            File ipFile = new File(resource.getPath());
            if (ipFile.exists() && ipFile.canRead()) {
                DB = new City(ipFile.getPath());
                logger.info("加载IP数据库完成");
            } else {
                logger.error("IP数据库状态异常 [" + ipFile.exists() + "][" + ipFile.canRead() + "]");
            }
        } catch (Exception e) {
            logger.error("加载IP数据库异常：" + e, e);
        }
    }

    public String buildRegionCityTxt(String ip) {
        if (isNull(ip)) {
            return ip;
        }
        String region = getRegionName(ip);
        String city = getCityName(ip);
        String txt = "";
        if (isNotNull(region)) {
            txt += region;
        }
        if (isNotNull(city)) {
            txt += (" - " + city);
        }
        return txt;
    }

    public String getCityName(String ip) {
        if (isNull(ip)) {
            return null;
        }
        CityInfo cityInfo = getCityInfo(ip);
        if (cityInfo != null) {
            return cityInfo.getCityName();
        }
        return null;
    }

    public String getRegionName(String ip) {
        if (isNull(ip)) {
            return null;
        }
        CityInfo cityInfo = getCityInfo(ip);
        if (cityInfo != null) {
            return cityInfo.getRegionName();
        }
        return null;
    }

    public String getCountryName(String ip) {
        if (isNull(ip)) {
            return null;
        }
        CityInfo cityInfo = getCityInfo(ip);
        if (cityInfo != null) {
            return cityInfo.getCountryName();
        }
        return null;
    }

    public CityInfo getCityInfo(String ip) {
        if (isNull(ip)) {
            return null;
        }
        try {
            return DB.findInfo(ip, LANGUAGE);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isInternetAddress(String ip) {
        String countryName = getCountryName(ip);
        return !(isNull(countryName) || eq(countryName, "局域网") || eq(countryName, "本机地址"));
    }

    public static void main(String[] args) {
        IPService ipService = new IPService();
        ipService._Construct();
        String ip = "223.104.19.81";
        System.out.println(ipService.isInternetAddress(ip));

        CityInfo cityInfo = ipService.getCityInfo(ip);
        System.out.println(JSON.toJSONString(cityInfo));
    }
}
