package in.digistorm.aksharam.activities.main.practice;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import java.util.Locale;

import in.digistorm.aksharam.util.Log;
import in.digistorm.aksharam.util.Transliterator;

public class PracticeTabViewModel extends ViewModel {
    private String logTag = "PracticeTabViewModel";

    private Transliterator transliterator;
    private String transLang;
    private String practiceType;

    public Transliterator getTransliterator(Context context) {
        if(transliterator == null)
            transliterator = Transliterator.getDefaultTransliterator(context);

        return transliterator;
    }

    public void resetTransliterator(Context context) {
        if(transliterator == null) {
            Log.d(logTag, "Transliterator is null. Initialising...");
            transliterator = Transliterator.getDefaultTransliterator(context);
        }
    }

    public Transliterator getTransliterator(String language, Context context) {
        if(transliterator != null) {
            if(transliterator.getCurrentLang().toLowerCase(Locale.ROOT)
                .equals(language.toLowerCase(Locale.ROOT)))
                return transliterator;
        }
        transliterator = new Transliterator(language, context);
        return transliterator;
    }

    public Transliterator getTransliterator() {
        return transliterator;
    }

    public String getLanguage() {
        return transliterator.getCurrentLang();
    }

    public void setTransLang(String lang) {
        transLang = lang;
    }

    public String getTransLang() {
        return transLang;
    }

    public void setPracticeType(String pType) {
        practiceType = pType;
    }

    public String getPracticeType() {
        return practiceType;
    }
}