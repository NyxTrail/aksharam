package in.digistorm.aksharam.util;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

public class AutoAdjustingTextView extends AppCompatTextView {
    String logTag = "AutoAdjustingTextView";

    public AutoAdjustingTextView(@NonNull Context context) {
        super(context);
    }

    public AutoAdjustingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoAdjustingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String text = getText().toString();

        float difference = getMeasuredWidth() - getPaint().measureText(text), size;
        if(difference < 9)
            Log.d(logTag, "Resizing " + text + " container text view");
        while(difference < 9){
            size = getTextSize() - 1.0f;
            difference = getMeasuredWidth() - getPaint().measureText(text);
            setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }
}
