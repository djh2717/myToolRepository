package my.code.repository.mvvm.view.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import my.code.repository.mvvm.model.LogOutModel

/**
 * @author djh on  2018/10/30 16:21
 * @E-Mail 1544579459@qq.com
 */
class LogOutViewModel : ViewModel() {
    
    val logInfoLiveData by lazy {
        MutableLiveData<String>()
    }
    
    private val logOutModel: LogOutModel = LogOutModel()
    
    
    fun logOut() {
        logOutModel.startLogOut(logInfoLiveData)
    }
    
}