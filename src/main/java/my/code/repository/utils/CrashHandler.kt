package my.code.repository.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.lang.Exception

/**
 * This is a custom crash handler, use to handler no catch exception, and save the
 * info, also can load the info to services.
 *
 * @author djh on  2018/10/7 13:18
 * @E-Mail 1544579459@qq.com
 */
@SuppressLint("StaticFieldLeak")
object CrashHandler : Thread.UncaughtExceptionHandler {
    
    private var mDefaultCrashHandler: Thread.UncaughtExceptionHandler? = null
    private lateinit var mContext: Context
    
    /**
     * Use this method at application to init the crash handler.
     */
    fun install(context: Context) {
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        this.mContext = context.applicationContext
    }
    
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        e?.let { saveInfo(e) }
        
        // Kill the application.
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler?.uncaughtException(t, e)
        } else {
            Process.killProcess(Process.myPid())
        }
    }
    
    private fun saveInfo(throwable: Throwable) {
        val logDir = File(mContext.filesDir, "log")
        logDir.mkdir()
        
        val nowTime = DateUtil.getNowDate(DateUtil.DATE_ALL) + "  " +
                DateUtil.getNowTime(DateUtil.TIME_ALL)
        val crashFileName = "crashInfo :$nowTime.txt"
        val crashFile = File(logDir, crashFileName)
        crashFile.createNewFile()
        
        val printWriter = PrintWriter(BufferedWriter(FileWriter(crashFile)))
        printWriter.use {
            printWriter.println(nowTime)
            printPhoneInfo(printWriter)
            
            printWriter.println()
            printWriter.println()
            
            throwable.printStackTrace(printWriter)
        }
    }
    
    private fun printPhoneInfo(printWriter: PrintWriter) {
        val packageManager = mContext.packageManager
        val packageInfo = packageManager.getPackageInfo(mContext.packageName, PackageManager.GET_ACTIVITIES)
        
        // App version.
        printWriter.print("APP Version: ")
        printWriter.print(packageInfo.versionName)
        printWriter.print("_")
        printWriter.println(packageInfo.versionCode)
        
        // Android version.
        printWriter.print("Android Version: ")
        printWriter.print(Build.VERSION.RELEASE)
        printWriter.print("_")
        printWriter.println(Build.VERSION.SDK_INT)
        
        // Phone manufacturer.
        printWriter.print("Brand: ")
        printWriter.println(Build.MANUFACTURER)
        
        // Phone model.
        printWriter.print("Model: ")
        printWriter.println(Build.MODEL)
        
        // CPU architecture.
        printWriter.print("CPU: ")
        printWriter.println(Build.SUPPORTED_ABIS)
    }
}