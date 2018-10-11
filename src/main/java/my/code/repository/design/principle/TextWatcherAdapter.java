package my.code.repository.design.principle;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * This use to adapter the text watcher interface.
 * NOTICE : This is a typical of interface adapter design mode.
 *
 * @author djh on  2018/9/10 21:53
 * @E-Mail 1544579459@qq.com
 */
public class TextWatcherAdapter implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
