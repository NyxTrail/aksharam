package in.digistorm.aksharam.util;

/*
 * Copyright (c) 2022 Alan M Varghese <alan@digistorm.in>
 *
 * This files is part of Aksharam, a script teaching app for Indic
 * languages.
 *
 * Aksharam is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aksharam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even teh implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

public class AutoAdjustingTextView extends AppCompatTextView {
    String logTag = getClass().getSimpleName();

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
