package `in`.digistorm.aksharam.activities.main.letters

import `in`.digistorm.aksharam.util.AutoAdjustingTextView
import `in`.digistorm.aksharam.util.logDebug
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View

class LetterView: AutoAdjustingTextView, View.OnClickListener {
    private var logTag: String = javaClass.simpleName
    var letter: String = ""
    var transliteratedLetter: String = ""

    constructor(context: Context): super(context)
    constructor(context: Context?, attrs: AttributeSet?): super(
        context!!, attrs
    )
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): super(
        context!!, attrs, defStyleAttr
    )
    constructor(letter: String,
                transliteratedLetter: String,
                context: Context
    ): this(context) {
        this.letter = letter
        this.transliteratedLetter = transliteratedLetter
        text = letter
        /*
          We use the tag to uniquely identify the textview that contains a letter. This is used in
          testing to click the letter accurately even after the actual text contained in the textview
          changes. This should work as long as the language has unique letters in each category.
         */
        tag = letter
        gravity = Gravity.CENTER
        setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        logDebug(logTag, "$text clicked")
        text = if(text == letter) transliteratedLetter else letter
    }
}