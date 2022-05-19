package in.digistorm.aksharam;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import in.digistorm.aksharam.util.Transliterator;

public class TransliterateTabTestData {

    public static Map<String, List<Map<String, String>>> getData(Context context) throws IOException {
        InputStream is = context.getResources().openRawResource(
                in.digistorm.aksharam.test.R.raw.transliterate_tab_test_data);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, new TypeReference<Map<String, List<Map<String, String>>>>() { });
    }
}
