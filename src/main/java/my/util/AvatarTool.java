package my.util;

import advanced.demo.R;

/**
 * @author Djh on 2018/7/16 17:03
 * E-Mail ：1544579459@qq.com
 */
public class AvatarTool {

    public static int[] avatars = new int[]{R.drawable.avatar1, R.drawable.avatar2,
            R.drawable.avatar3, R.drawable.avatar4, R.drawable.avatar5, R.drawable.avatar6,
            R.drawable.avatar7, R.drawable.avatar8, R.drawable.avatar9, R.drawable.avatar10};

    public static String[] avatarNames = new String[]{"风车", "小华", "漫画小子", "大拇指", "透明人", "经理"
            , "假小子", "小坏蛋", "小红帽", "联系人"};

    public static class AvatarEntity {
        private int avatarIndex;
        private String avatarName;

        public AvatarEntity(int avatarIndex, String avatarName) {
            this.avatarIndex = avatarIndex;
            this.avatarName = avatarName;
        }

        public void setAvatarName(String avatarName) {
            this.avatarName = avatarName;
        }

        public int getAvatarIndex() {
            return avatarIndex;
        }

        public String getAvatarName() {
            return avatarName;
        }
    }

}
