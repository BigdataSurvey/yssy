package com.zywl.app.base.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则工具类
 * @author DOE
 *
 */
public class RegexUtil {

    // 获取img标签正则  
    private static final String IMGURL_REG = "<\\s*img\\s+([^>]*)\\s*";  
    
    // 获取src路径的正则  
    private static final String IMGSRC_REG = "(http|https):\"?(.*?)(\"|>|\\s+)";

	private static List<Pattern> lianghaoPatterns = new ArrayList<Pattern>();

	static {
		// AAABBB
		lianghaoPatterns.add(Pattern.compile("^\\d*(\\d)\\1\\1(\\d)\\2\\2\\d*$"));
		// ABABAB
		lianghaoPatterns.add(Pattern.compile("^(\\d)(\\d)\\1\\2\\1\\2\\1\\2$"));
		// ABCABC
		lianghaoPatterns.add(Pattern.compile("^(\\d)(\\d)(\\d)\\1\\2\\3$"));
		// ABBABB
		lianghaoPatterns.add(Pattern.compile("^(\\d)(\\d)\\2\\1\\2\\2$"));
		// 4-8 位置重复
		lianghaoPatterns.add(Pattern.compile("^\\d*(\\d)\\1{2,}\\d*$"));
		// 4位以上 位递增或者递减（7890也是递增）
		lianghaoPatterns.add(Pattern.compile("( :( :0( =1)|1( =2)|2( =3)|3( =4)|4( =5)|5( =6)|6( =7)|7( =8)|8( =9)|9( =0)){2,}|( :0( =9)|9( =8)|8( =7)|7( =6)|6( =5)|5( =4)|4( =3)|3( =2)|2( =1)|1( =0)){2,})\\d"));
	}

	/**
	 * 获取&lt;img>标签 
	 * @return
	 */
    public static List<String> getHTMLImageTag(String HTML) {  
        Matcher matcher = Pattern.compile(IMGURL_REG).matcher(HTML);  
        List<String> listImgUrl = new ArrayList<String>();  
        while (matcher.find()) {  
            listImgUrl.add(matcher.group());  
        }  
        return listImgUrl;  
    }  
  
    /**
     * 获取src地址
     * @return
     */
    public static List<String> getHTMLImageSrc(String HTML) {
    	List<String> listImageUrl = getHTMLImageTag(HTML);
        List<String> listImgSrc = new ArrayList<String>();
        for (String image : listImageUrl) {
            Matcher matcher = Pattern.compile(IMGSRC_REG).matcher(image);
            while (matcher.find()) {  
                listImgSrc.add(matcher.group().substring(0, matcher.group().length() - 1).replaceAll("'", ""));
            }  
        }  
        return listImgSrc;
    }
    
    public static String getHost(String url){
        String pattern = "[^://]*?\\.(com|cn|net|org|biz|info|cc|tv)/" ;
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(url);
        matcher.find();
        return matcher.group().replaceAll("/", "");
    }
    
    public static String hidePhone(String phone){
    	return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
    }

	public static boolean isLiangHao(String input) {
		for (Pattern pattern : lianghaoPatterns) {
			if(pattern.matcher(input).matches()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 字符串是否是中文、字母、英文
	 * @param str
	 * @return
	 */
	public static boolean isLetterDigitOrChinese(String str) {
		String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";// 其他需要，直接修改正则表达式就好
		return str.matches(regex);
	}
	
	public static void main(String[] args) {
		/*long now = System.currentTimeMillis();
		String HTML = "<li> <div class='one'><a href='http://m.jb51.net' target='_blank'>手机版</a></div><div class='two'><img src='https://files.jb51.net/images/m.jb51.net.png' data-baiduimageplus-ignore /></div></li><li class='watch'><div class='one'><a href='https://www.jb51.net/about.htm' target='_blank' rel='nofollow' ><i class='icon'></i>关注微信</a></div><div class='two'><img src='http://files.jb51.net/images/weixin_200.gif' data-baiduimageplus-ignore /></div></li>";
        System.out.println(JSON.toJSONString(RegexUtil.getHTMLImageSrc(HTML)));
        System.out.println(System.currentTimeMillis() - now);
        System.out.println(getHost("https://wallstreetcn.com/articles/3040803"));*/
		int j = 0 ;
		for (int i = 0; i < 999999; i++) {
			String num = RandomNumberUtils.create(5)+"";
			if(isLiangHao(num)) {
				System.out.println(++j+" "+num);
			}
		}
		
	}
}
