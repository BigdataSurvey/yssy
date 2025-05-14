package com.zywl.app.server.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestMain {
	
	public static void main(String[] args) throws IOException, Exception {
		Map<String, String> map1 = new HashMap<String, String>();
		Map<String, String> map2 = new ConcurrentHashMap<String, String>();
		map1.put("a", "b");
		map2.put("c", "d");
		long a1 = 0;
		long a2 = 0;
		for (int i = 0; i < 10000000; i++) {
			long t1 = System.nanoTime();
			map1.get("a");
			a1 += (System.nanoTime() - t1);
			
			long t2 = System.nanoTime();
			map2.get("c");
			a2 += (System.nanoTime() - t2);
		}
		System.out.println(a1);
		System.out.println(a2);
		
//		String data = "9Ay7do6tGOUdRDojW3sCWuUqjdJADbThGAWABAxaqMBOQXCy%2BJNFyA3SnF7G7xy52WKcBft4RfRh%0AmpvAZVBBRBsD7x1BZoUhn6vdK6hIoPaBTdt7f6vw84Oq%2B3dl6nbsEBR0c0HvpDkPCtweM%2BN%2Fd5Fp%0AXlmOXREqN7m9PuLimfEsWVIbcvFaniIrmTNXYXmDqRdk8lL7ptyBDt7Hd3nG4wm8EV2Ow%2FFYhcOu%0AmlnCdortMrSoW9poy%2Fykl3KOadTC%0A";
//		System.out.println(data.contains("%"));
//		System.out.println(DesUtil.decrypt(URLDecoder.decode(data, "UTF-8"), "lR9w366HI9HYw2RjjW31lGyfaOvMxqWF"));
	}
}
