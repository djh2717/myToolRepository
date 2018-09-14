package my.code.repository.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Djh on 2018/7/26 14:54
 * E-Mail ：1544579459@qq.com
 */
public class DataBaseUtil extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;

    public static final String TABLE_MY_TABLE = "myTable";

    private static final String DB_FILE_NAME = "myDataBase.db";

    public DataBaseUtil(Context context, int version) {
        super(context, DB_FILE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "create table myTable(tableName varchar(20), tableAge varchar(20))";
        sqLiteDatabase.execSQL(sql);
        Log.d("DataBaseUtil--->onCreate======", "---");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
