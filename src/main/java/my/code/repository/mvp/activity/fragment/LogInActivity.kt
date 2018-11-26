package my.code.repository.mvp.activity.fragment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_log_in.*
import my.code.repository.mvp.presenter.LogInPresenter
import my.code.repository.mvp.view.LogInView
import my.code.repository.utils.showToast
import my.demo.one.R

/**
 * @author djh on 2018-10-16 14:12:40
 * @E-Mail 1544579459@qq.com
 */
class LogInActivity : AppCompatActivity(), LogInView {
    
    override val mPresenter: LogInPresenter = LogInPresenter(lifecycle, this)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        
        logInButton.setOnClickListener {
            mPresenter.login(useNameEditText.text.toString(), passWordEdiText.text.toString())
        }
    }
    
    override fun logSuccess(message: String?) {
        message?.let {
            showToast(message)
            logInfoText.text = message
        }
    }
    
    override fun logFailure(message: String?) {
        message?.let {
            showToast(message)
            logInfoText.text = message
        }
    }
}
