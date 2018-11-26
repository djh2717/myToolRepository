package my.code.repository.mvvm.activity.fragment

import androidx.appcompat.app.AppCompatActivity
import my.demo.one.R

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_log_out.*
import my.code.repository.mvvm.view.model.LogOutViewModel
import my.code.repository.utils.showToast

/**
 * @author djh on 2018-10-30 16:19:47
 * @E-Mail 1544579459@qq.com
 */
class LogOutActivity : AppCompatActivity() {
    
    private lateinit var logOutViewModel: LogOutViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_out)
        
        logOutViewModel = ViewModelProviders.of(this).get(LogOutViewModel::class.java)
        logOutViewModel.logInfoLiveData.observe(this, Observer { it: String? ->
            logInfoText.text = it
            it?.let { showToast(it) }
        })
        
        
        logOutBtn.setOnClickListener {
            logOutViewModel.logOut()
        }
    }
}
