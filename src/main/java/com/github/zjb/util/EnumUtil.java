package com.github.zjb.util;

import com.github.zjb.enums.AnnotationEnum;
import com.google.common.collect.Maps;
import com.intellij.psi.PsiAnnotation;

import java.util.Map;

/**
 * @author zhaojingbo(zjbhnay @ 163.com)
 * 2020/12/28
 */
public class EnumUtil {

    private static final Map<String, AnnotationEnum> ANNOTATION_ENUM_MAP = Maps.newHashMap();

    static {
        for (AnnotationEnum value : AnnotationEnum.values()) {
            ANNOTATION_ENUM_MAP.put(value.getQualifiedName(), value);
        }
    }

    public static Boolean containAnnotation(PsiAnnotation psiAnnotation) {
        return ANNOTATION_ENUM_MAP.containsKey(psiAnnotation.getQualifiedName());
    }


}
