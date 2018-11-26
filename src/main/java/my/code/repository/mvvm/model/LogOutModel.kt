package my.code.repository.mvvm.model

import androidx.lifecycle.MutableLiveData
import java.util.concurrent.Executors

/**
 * @author djh on  2018/10/30 16:23
 * @E-Mail 1544579459@qq.com
 */
class LogOutModel : BaseModel() {
    var i = 0
    
    fun startLogOut(liveData: MutableLiveData<String>) {
        i++
        val cachePool = Executors.newCachedThreadPool()
        cachePool.execute {
            Thread.sleep(5000)
            liveData.postValue("第 $i 次退出登录成功!")
        }
        cachePool.shutdown()
    }
}