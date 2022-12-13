package `in`.digistorm.aksharam.activities.main.letters

import `in`.digistorm.aksharam.util.AutoAdjustingTextView
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager

class LetterView: AutoAdjustingTextView {
    private var logTag: String = javaClass.simpleName
    private var letter: String = ""

    constructor(context: Context): super(context)
    constructor(context: Context?, attrs: AttributeSet?): super(
        context!!, attrs
    )
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): super(
        context!!, attrs, defStyleAttr
    )
    constructor(letter: String, context: Context): this(context) {
        this.letter = letter
        text = letter
        /*
        We use the tag to uniquely identify the textview that contains a letter. This is used in
        testing to click the letter accurately even after the actual text contained in the textview
        changes. This should work as long as the language has unique letters in each category.
         */
        tag = letter
        gravity = Gravity.CENTER
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private fun getHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val height = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.height()
        } else {
            windowManager.defaultDisplay.height
        }
        return height
    }

    public fun getScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Log.d(logTag, "SDK VERSION: ${Build.VERSION.SDK_INT}")
        Log.d(logTag, "SDK VERSION R: ${Build.VERSION_CODES.R}")
        val width = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insets = windowManager.currentWindowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowManager.currentWindowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
        return width
    }

}