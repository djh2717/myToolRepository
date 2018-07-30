package my.utils;

import java.util.regex.Pattern;

/**
 * A regex util, use to matcher characters.
 *
 * @author Djh on 2018/7/25 10:50
 * E-Mail ：1544579459@qq.com
 */
public class RegexUtil {

    /**
     * Regular expression: Matcher userName (Does not contain Chinese and special characters).
     * If the username uses a mobile number or email combined with mobile phone
     * number verification and mailbox verification.
     */
    private static final String REGEX_USERNAME = "^[a-zA-Z]\\w{5,17}$";


    /**
     * Regular expression: Matcher passWord (Does not contain special characters).
     */
    private static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,16}$";


    /**
     * Regular expression: Verify phone number.
     */
    private static final String REGEX_MOBILE = "^(0|86|17951)?(13[0-9]|15[0-9]|17[678]|18[0-9]|14[57])[0-9]{8}$";


    /**
     * Regular expression: Verify email.
     */
    private static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";


    /**
     * Regular expression: Verify Chinese characters(1-9个汉字)  {1,9} custom interval.
     */
    private static final String REGEX_CHINESE = "^[\u4e00-\u9fa5]{1,9}$";


    /**
     * Regular expression: Verify ID card.
     */
    private static final String REGEX_ID_CARD = "(^\\d{15}$)|(^\\d{17}([0-9]|X)$)";


    /**
     * Regular expression: Verify URL.
     */
    private static final String REGEX_URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";


    /**
     * Regular expression: Verify IP address.
     */
    private static final String REGEX_IP_ADDRESS = "(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})";


    public static boolean isUserName(String username) {
        return Pattern.matches(REGEX_USERNAME, username);
    }


    public static boolean isPassword(String password) {
        return Pattern.matches(REGEX_PASSWORD, password);
    }


    public static boolean isMobile(String mobile) {
        return Pattern.matches(REGEX_MOBILE, mobile);
    }


    public static boolean isEmail(String email) {
        return Pattern.matches(REGEX_EMAIL, email);
    }


    public static boolean isChinese(String chinese) {
        return Pattern.matches(REGEX_CHINESE, chinese);
    }


    public static boolean isIDCard(String idCard) {
        return Pattern.matches(REGEX_ID_CARD, idCard);
    }


    public static boolean isUrl(String url) {
        return Pattern.matches(REGEX_URL, url);
    }


    public static boolean isIPAddress(String ipAddress) {
        return Pattern.matches(REGEX_IP_ADDRESS, ipAddress);
    }

}
