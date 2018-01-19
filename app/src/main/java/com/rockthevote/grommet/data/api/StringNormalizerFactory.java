package com.rockthevote.grommet.data.api;

import com.rockthevote.grommet.util.MoshiUtils;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.Normalizer;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Created by Aaron Huttner on 9/23/16. Grommet
 */

public class StringNormalizerFactory implements JsonAdapter.Factory {

    public static String stripDiacritics(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return str;
    }

    @Override
    public JsonAdapter<?> create(
            Type type, Set<? extends Annotation> annotations, Moshi moshi) {
        if (!type.equals(String.class)) return null;
        if (!MoshiUtils.isAnnotationPresent(annotations, Normalize.class)) return null;

        final JsonAdapter<String> stringAdapter
                = moshi.nextAdapter(this, String.class, MoshiUtils.NO_ANNOTATIONS);
        return new JsonAdapter<String>() {
            @Override
            public String fromJson(JsonReader reader) throws IOException {
                String s = stringAdapter.fromJson(reader);
                return stripDiacritics(s);
            }

            @Override
            public void toJson(JsonWriter writer, String value) throws IOException {
                stringAdapter.toJson(writer, stripDiacritics(value));
            }
        };
    }


}
