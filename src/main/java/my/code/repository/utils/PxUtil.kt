@file:JvmName("PxUtil")

package my.code.repository.utils

import android.util.TypedValue
import android.view.ViewConfiguration

/**
 * @author djh on  2018/10/5 14:36
 * @E-Mail 1544579459@qq.com
 */
private val DISPLAY_METRICS = MyApplication.getContext().resources.displayMetrics

fun mmToPx(value: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, value, DISPLAY_METRICS)

fun dpToPx(value: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, DISPLAY_METRICS)

fun spToPx(value: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, DISPLAY_METRICS)

fun getTouchSlop() =
        ViewConfiguration.get(MyApplication.getContext()).scaledTouchSlop
        
