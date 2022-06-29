package in.digistorm.aksharam;

import android.content.Context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class TransliterateTabTestData {

    public static Map<String, List<Map<String, String>>> getData(Context context) throws IOException {
        InputStream is = context.getResources().openRawResource(
                in.digistorm.aksharam.test.R.raw.transliterate_tab_test_data);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, new TypeReference<Map<String, List<Map<String, String>>>>() { });
    }
}
