@file:JvmName("ToastUtil")

package my.code.repository.utils

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import com.google.android.material.snackbar.Snackbar
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

/**
 * Toast skill, avoid multiple triggers lead to long time display a toast.
 *
 * @author djh on  2018/10/5 11:42
 * @E-Mail 1544579459@qq.com
 */
private var sToast: Toast? = null
@SuppressLint("StaticFieldLeak")
private var sSnackbar: Snackbar? = null

@JvmOverloads
@SuppressLint("ShowToast")
fun showToast(content: String, duration: Int = Toast.LENGTH_LONG, gravity: Int = -1) {
    if (sToast == null) {
        sToast = Toast.makeText(MyApplication.getContext(), content, duration)
    } else {
        sToast?.setText(content)
    }
    if (gravity != -1) {
        sToast?.setGravity(gravity, 0, 0)
    }
    sToast?.show()
}

@JvmOverloads
fun showSnackbar(content: String, viewGroup: ViewGroup,
                 duration: Int = Snackbar.LENGTH_LONG,
                 actionContent: String = "",
                 onClickListener: View.OnClickListener? = null) {
    
    if (sSnackbar == null) {
        sSnackbar = Snackbar.make(viewGroup, content, duration)
                .setActionTextColor(Color.WHITE)
    } else {
        sSnackbar?.setActionTextColor(Color.WHITE)
        sSnackbar?.setText(content)
    }
    
    if (onClickListener != null) {
        sSnackbar?.setAction(actionContent, onClickListener)
    }
    
    sSnackbar?.show()
}