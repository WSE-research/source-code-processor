package org.wseresearch.processor;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TypeNameUtils {

    // === COMPILE-TIME === //
    public static String getTypeName(TypeMirror type) {
        if (type.getKind().isPrimitive() || type.getKind() == TypeKind.VOID) {
            return type.toString(); // e.g. "int"
        }

        if (type.getKind() == TypeKind.ARRAY) {
            ArrayType arrayType = (ArrayType) type;
            return getTypeName(arrayType.getComponentType()) + "[]";
        }

        if (type instanceof DeclaredType) {
            DeclaredType declared = (DeclaredType) type;
            String rawType = declared.asElement().toString();
            List<? extends TypeMirror> args = declared.getTypeArguments();

            if (!args.isEmpty()) {
                String generics = args.stream()
                        .map(TypeNameUtils::getTypeName)
                        .collect(Collectors.joining(","));
                return rawType + "<" + generics + ">";
            }

            return rawType;
        }

        return type.toString(); // fallback (wildcards, etc.)
    }

    // === RUNTIME === //
    public static String getTypeName(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            String raw = getTypeName(pt.getRawType());
            String args = Arrays.stream(pt.getActualTypeArguments())
                    .map(TypeNameUtils::getTypeName)
                    .collect(Collectors.joining(","));
            return raw + "<" + args + ">";
        }

        if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            return getTypeName(arrayType.getGenericComponentType()) + "[]";
        }

        if (type instanceof Class) {
            return ((Class<?>) type).getName();
        }

        if (type instanceof TypeVariable<?>) {
            return ((TypeVariable<?>) type).getName(); // fallback: T, E, etc.
        }

        if (type instanceof WildcardType) {
            WildcardType w = (WildcardType) type;
            StringBuilder sb = new StringBuilder("?");
            if (w.getLowerBounds().length > 0) {
                sb.append(" super ").append(getTypeName(w.getLowerBounds()[0]));
            } else if (w.getUpperBounds().length > 0 && !w.getUpperBounds()[0].equals(Object.class)) {
                sb.append(" extends ").append(getTypeName(w.getUpperBounds()[0]));
            }
            return sb.toString();
        }

        return type.getTypeName(); // fallback
    }

    // Optional: Remove all whitespace (for consistency in string matching)
    public static String normalize(String typeString) {
        return typeString.replaceAll("\\s+", "");
    }

    public static List<String> normalize(List<String> types) {
        return types.stream().map(TypeNameUtils::normalize).collect(Collectors.toList());
    }

}
