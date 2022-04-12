package in.digistorm.aksharam;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import java.util.Locale;

public class LettersTabViewModel extends ViewModel {
    private String targetLanguage;
    private String language;
    private Transliterator transliterator;
    private LabelledArrayAdapter<String> adapter;

    public Transliterator getTransliterator(Context context) {
        if(transliterator == null)
            transliterator = Transliterator.getDefaultTransliterator(context);

        return transliterator;
    }

    public Transliterator getTransliterator(String language, Context context) {
        if(transliterator != null) {
            if (transliterator.getCurrentLang().toLowerCase(Locale.ROOT)
                    .equals(language.toLowerCase(Locale.ROOT)))
                return transliterator;
        }
        transliterator = new Transliterator(language, context);
        return transliterator;
    }

    public Transliterator getTransliterator() {
        return transliterator;
    }

    public void setTargetLanguage(String lang) {
        targetLanguage = lang;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setLanguage(String lang) {
        language = lang;
    }

    public String getLanguage() {
        return language;
    }

    public LabelledArrayAdapter<String> getAdapter() {
        return adapter;
    }

    public void setAdapter(LabelledArrayAdapter<String> adapter) {
        this.adapter = adapter;
    }
}
