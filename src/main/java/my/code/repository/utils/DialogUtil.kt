package my.code.repository.utils

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import my.demo.one.R
import kotlin.math.roundToInt

/**
 * Use to show custom layout dialog.
 *
 * @author djh on  2018/11/12 14:01
 * @E-Mail 1544579459@qq.com
 */
@JvmOverloads
fun showCustomDialog(
        context: Context, contentView: View, width: Float = -1f,
        height: Float = -1f, backgroundTransparent: Boolean = true,
        cancelable: Boolean = true, cancelableTouchOutSide: Boolean = true,
        @StyleRes animation: Int = R.style.DialogAnimation): AlertDialog {
    
    val alertDialog = AlertDialog.Builder(context)
            .setCancelable(cancelable)
            .create()
            .apply {
                setCanceledOnTouchOutside(cancelableTouchOutSide)
                show()
            }
    
    
    alertDialog.window?.apply {
        // Remove the content view from any view group if need.
        (contentView.parent as? ViewGroup)?.removeView(contentView)
        
        setContentView(contentView)
        setWindowAnimations(animation)
        
        if (width != -1f && height != -1f) {
            attributes.apply {
                this.width = width.roundToInt()
                this.height = height.roundToInt()
            }
        }
        
        if (backgroundTransparent) {
            decorView.setBackgroundColor(Color.TRANSPARENT)
        }
    }
    
    return alertDialog
}