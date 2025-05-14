package com.zywl.app.base.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcherUtil {
	
	
	
	public static boolean isEmail(String email) {
		String regex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher=  pattern.matcher(email);
        boolean isMatch = matcher.find();
        return isMatch;
	}

}
