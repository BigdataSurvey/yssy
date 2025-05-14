package com.live.app.ws.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DNSMoniter {

	public static void main(String[] args) {
		String hostString = "us1.vpn.goldenfrog.com,us2.vpn.goldenfrog.com,us3.vpn.goldenfrog.com,us4.vpn.goldenfrog.com,us5.vpn.goldenfrog.com,us6.vpn.goldenfrog.com,us7.vpn.goldenfrog.com,us8.vpn.goldenfrog.com,ca1.vpn.goldenfrog.com,eu1.vpn.goldenfrog.com,dk1.vpn.goldenfrog.com,se1.vpn.goldenfrog.com,hk1.vpn.goldenfrog.com,uk1.vpn.goldenfrog.com,fr1.vpn.goldenfrog.com,de1.vpn.goldenfrog.com,ch1.vpn.goldenfrog.com,ru1.vpn.goldenfrog.com,lu1.vpn.goldenfrog.com,ro1.vpn.goldenfrog.com,sg1.vpn.goldenfrog.com,ie1.vpn.goldenfrog.com,my1.vpn.goldenfrog.com,it1.vpn.goldenfrog.com,es1.vpn.goldenfrog.com,jp1.vpn.goldenfrog.com,kr1.vpn.goldenfrog.com,no1.vpn.goldenfrog.com,tr1.vpn.goldenfrog.com,fi1.vpn.goldenfrog.com,pl1.vpn.goldenfrog.com,pt1.vpn.goldenfrog.com,cz1.vpn.goldenfrog.com,at1.vpn.goldenfrog.com,be1.vpn.goldenfrog.com,lt1.vpn.goldenfrog.com,bg1.vpn.goldenfrog.com,li1.vpn.goldenfrog.com,au1.vpn.goldenfrog.com,au2.vpn.goldenfrog.com,au3.vpn.goldenfrog.com,id1.vpn.goldenfrog.com,nz1.vpn.goldenfrog.com,vn1.vpn.goldenfrog.com,th1.vpn.goldenfrog.com,is1.vpn.goldenfrog.com,ph1.vpn.goldenfrog.com,br1.vpn.goldenfrog.com,mx1.vpn.goldenfrog.com,ar1.vpn.goldenfrog.com,co1.vpn.goldenfrog.com,cr1.vpn.goldenfrog.com,pa1.vpn.goldenfrog.com,tw1.vpn.goldenfrog.com,bh1.vpn.goldenfrog.com,sa1.vpn.goldenfrog.com,qa1.vpn.goldenfrog.com,in1.vpn.goldenfrog.com,ae1.vpn.goldenfrog.com,il1.vpn.goldenfrog.com,li1.vpn.goldenfrog.com,sk1.vpn.goldenfrog.com,ua1.vpn.goldenfrog.com,si1.vpn.goldenfrog.com,lv1.vpn.goldenfrog.com,sv1.vpn.goldenfrog.com,uy1.vpn.goldenfrog.com,mo1.vpn.goldenfrog.com,mh1.vpn.goldenfrog.com,mv1.vpn.goldenfrog.com,dz1.vpn.goldenfrog.com,eg1.vpn.goldenfrog.com,gr1.vpn.goldenfrog.com,pk1.vpn.goldenfrog.com";
		String[] hosts = hostString.split(",");
		for (final String host : hosts) {
			new Thread(){
				public void run() {
					test(host);
				};
			}.start();
		}
	}
	
	public static void test(String host){
		try {
			InetAddress[] addresses = InetAddress.getAllByName(host);
			for (InetAddress inetAddress : addresses) {
				String ip = inetAddress.getHostAddress();
				long time = System.currentTimeMillis();
				Socket socket = new Socket();
				try {
					socket.connect(new InetSocketAddress(ip, 1701), 3000);
					System.out.println(host + " : " + ip + "：" + (System.currentTimeMillis() - time) + "ms");
		        } catch (Exception e) {
					//System.out.println(host + " : " + ip + "：" + e);
					continue;
		        } finally {
		            try {
		                socket.close();
		            } catch (Exception e) {}
		        }
			}
		} catch (Exception e) {
		}
	}
}
