package com.zywl.app.base.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ByteUtil {

	protected static final Charset CHRST = Charset.forName("UTF-8");

	public static ByteBuffer ToByte(String data){
//		return CHRST.encode(data);
		return ToByte(data.getBytes(StandardCharsets.UTF_8));
	}

	public static ByteBuffer ToByte(byte[] data){
		return ByteBuffer.wrap(data);
	}

	public static String ToString(ByteBuffer buffer) {
//		return CHRST.decode(buffer).toString();
		return ToString(buffer.array());
	}

	public static String ToString(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}


}
