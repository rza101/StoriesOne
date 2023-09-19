package com.rhezarijaya.storiesone.ui.customview

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import com.rhezarijaya.storiesone.R
import com.rhezarijaya.storiesone.util.Helpers

// saran dari submission 1: email dan password bisa digabung menjadi 1 custom edit text
class CustomEditText : AppCompatEditText {
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
        // saat dilakukan cek dengan logcat, input type yang diharapkan untuk tipe email dan password
        // seakan ditambah 1 secara binary, sehingga bisa memakai operasi bitwise or
        // untuk menambah 1 (TYPE_CLASS_TEXT) ke tipe tersebut
        when (inputType) {
            InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) -> {
                addTextChangedListener(
                    onTextChanged = { charSequence, _, _, _ ->
                        charSequence?.let {
                            error = if (it.isNotEmpty() && !Helpers.isEmailValid(it.toString())) {
                                context.getString(R.string.invalid_email)
                            } else {
                                null
                            }
                        }
                    }
                )
            }

            InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_PASSWORD) -> {
                addTextChangedListener(
                    onTextChanged = { charSequence, _, _, _ ->
                        charSequence?.let {
                            error =
                                if (it.isNotEmpty() && !Helpers.isPasswordValid(it.toString())) {
                                    context.getString(R.string.invalid_password)
                                } else {
                                    null
                                }
                        }
                    }
                )
            }
        }
    }
}