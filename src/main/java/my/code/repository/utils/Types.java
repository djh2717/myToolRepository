package my.code.repository.utils;

/**
 * A media type util, provide the common media type as a constant.
 *
 * @author djh on  2018/9/8 19:19
 * @E-Mail 1544579459@qq.com
 */
public class Types {
    /**
     * text/plain          ：纯文本格式 .txt
     * text/xml            ： XML格式 .xml
     * image/gif           ： gif图片格式 .gif
     * image/jpeg          ： jpg图片格式 .jpg
     * image/png           ： png图片格式 .png
     * audio/mp3           : 音频mp3格式 .mp3
     * audio/rn-mpeg       : 音频mpga格式 .mpga
     * video/mpeg4         : 视频mp4格式 .mp4
     * video/x-mpg         : 视频mpa格式 .mpg
     * video/x-mpeg        : 视频mpeg格式 .mpeg
     * video/mpg           : 视频mpg格式 .mpg
     *
     * 以application开头的媒体格式类型：
     *
     * application/xhtml+xml      ：XHTML格式
     * application/xml            ：XML数据格式
     * application/atom+xml       ：Atom XML聚合格式
     * application/json           ：JSON数据格式
     * application/pdf            ：pdf格式
     * application/msword         ：Word文档格式
     * application/octet-stream   ：二进制流数据
     */

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
}
