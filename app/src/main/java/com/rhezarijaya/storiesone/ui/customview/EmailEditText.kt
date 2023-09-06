package com.rhezarijaya.storiesone.ui.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.rhezarijaya.storiesone.R
import com.rhezarijaya.storiesone.util.Helpers

class EmailEditText : AppCompatEditText {
    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize()
    }

    private fun initialize() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    error = if (it.isNotEmpty() && !Helpers.isEmailValid(it.toString())) {
                        context.getString(R.string.invalid_email)
                    } else {
                        null
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}