package com.rockthevote.grommet.util;

import com.squareup.moshi.JsonQualifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Created by Aaron Huttner on 9/23/16. Grommet
 */

public final class MoshiUtils {
    public static final Set<Annotation> NO_ANNOTATIONS = Collections.emptySet();

    public static boolean typesMatch(Type pattern, Type candidate) {
        // TODO: permit raw types (like Set.class) to match non-raw candidates (like Set<Long>).
        return pattern.equals(candidate);
    }

    public static Set<? extends Annotation> jsonAnnotations(AnnotatedElement annotatedElement) {
        return jsonAnnotations(annotatedElement.getAnnotations());
    }

    public static Set<? extends Annotation> jsonAnnotations(Annotation[] annotations) {
        Set<Annotation> result = null;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(JsonQualifier.class)) {
                if (result == null) result = new LinkedHashSet<>();
                result.add(annotation);
            }
        }
        return result != null ? Collections.unmodifiableSet(result) : MoshiUtils.NO_ANNOTATIONS;
    }

    public static boolean isAnnotationPresent(
            Set<? extends Annotation> annotations, Class<? extends Annotation> annotationClass) {
        if (annotations.isEmpty()) return false; // Save an iterator in the common case.
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == annotationClass) return true;
        }
        return false;
    }

    /**
     * Returns true if {@code annotations} has any annotation whose simple name is Nullable.
     */
    public static boolean hasNullable(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().getSimpleName().equals("Nullable")) {
                return true;
            }
        }
        return false;
    }

}
