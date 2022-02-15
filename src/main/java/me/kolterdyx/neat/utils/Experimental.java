package me.kolterdyx.neat.utils;

import java.lang.annotation.*;

/**
 *
 * This element has an experimental maturity.  Use with caution.
 *
 *
 * NOTE: The developers of this element is not responsible for the issues created,
 * using it is not suggested for production environment.
 */


@Documented //this annotation maybe helpful for your custom annotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
        ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.PACKAGE,
        ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE, ElementType.TYPE_PARAMETER
})
public @interface Experimental {}