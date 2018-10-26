/*
 * @(#)ElementUtils.java     7 Jun 2009
 * 
 * Copyright © 2009 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.xunkutech.base.model.util;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject.Kind;
import java.lang.annotation.*;
import java.util.*;

/**
 * Utility methods for {@link Element Elements}.
 *
 * @author aphillips
 * @see Elements
 * @since 7 Jun 2009
 */
public abstract class ElementUtils {
    /**
     * The {@link Target documented} default value is &quot;all element types&quot;.
     */
    private static final Set<ElementType> DEFAULT_TARGET = asUnmodifiableSet(ElementType.values());

    /**
     * The {@link Retention documented} default value is CLASS.
     */
    private static final RetentionPolicy DEFAULT_RETENTION = RetentionPolicy.CLASS;

    @SafeVarargs
    private static <E> Set<E> asUnmodifiableSet(E... objs) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(objs)));
    }

    /**
     * Checks if an {@link Element} is of a certain {@link Kind}.
     *
     * @param element the element to be checked
     * @param kind    the kind to be checked for
     * @return {@code true} iff the element is of the given kind
     * @see #isAnnotation(Element)
     * @see #isConstructor(Element)
     */
    public static boolean isOfKind(Element element, ElementKind kind) {
        return ((element != null) && (kind != null) && element.getKind().equals(kind));
    }

    /**
     * Checks if an {@link Element} is an {@link ElementKind#ANNOTATION_TYPE annotation}
     *
     * @param element the element to be checked
     * @return {@code true} iff the element is an annotation
     * @see #isConstructor(Element)
     * @see #isMethod(Element)
     * @see #isOfKind(Element, ElementKind)
     */
    public static boolean isAnnotation(Element element) {
        return isOfKind(element, ElementKind.ANNOTATION_TYPE);
    }

    /**
     * Checks if an {@link Element} is a {@link ElementKind#CONSTRUCTOR constructor}
     *
     * @param element the element to be checked
     * @return {@code true} iff the element is a constructor
     * @see #isAnnotation(Element)
     * @see #isMethod(Element)
     * @see #isOfKind(Element, ElementKind)
     */
    public static boolean isConstructor(Element element) {
        return isOfKind(element, ElementKind.CONSTRUCTOR);
    }

    /**
     * Checks if an {@link Element} is a {@link ElementKind#METHOD method}
     *
     * @param element the element to be checked
     * @return {@code true} iff the element is a method
     * @see #isAnnotation(Element)
     * @see #isConstructor(Element)
     * @see #isOfKind(Element, ElementKind)
     */
    public static boolean isMethod(Element element) {
        return isOfKind(element, ElementKind.METHOD);
    }

    /**
     * @param element the element whose target is required
     * @return the value of the {@link Target @Target} annotation on the given element, if present,
     * or the documented default value; for a {@code null} element or an element that
     * is not an {@link Annotation} subtype, returns {@code null}
     */
    public static Set<ElementType> getTarget(Element element) {

        if ((element == null) || !isAnnotation(element)) {
            return null;
        }

        Target targetAnnotation = element.getAnnotation(Target.class);
        return ((targetAnnotation != null) ? asUnmodifiableSet(targetAnnotation.value())
                : DEFAULT_TARGET);
    }

    /**
     * @param element the element whose retention policy is required
     * @return the value of the {@link Retention @Retention} annotation on the given class, if
     * present, or the documented default value; for a {@code null} element or an element that
     * is not an {@link Annotation} subtype, returns {@code null}
     */
    public static RetentionPolicy getRetention(Element element) {

        if ((element == null) || !isAnnotation(element)) {
            return null;
        }

        Retention retentionAnnotation = element.getAnnotation(Retention.class);
        return ((retentionAnnotation != null) ? retentionAnnotation.value() : DEFAULT_RETENTION);
    }

    /**
     * @param element the element to be checked
     * @return {@code true} iff the element has a public, no-argument constructor
     */
    public static boolean hasPublicNoargConstructor(Element element) {

        if (element == null) {
            return false;
        }

        for (Element enclosedElement : element.getEnclosedElements()) {
            // an element of type CONSTRUCTOR is an ExecutableElement
            if (isConstructor(enclosedElement)
                    && enclosedElement.getModifiers().contains(Modifier.PUBLIC)
                    && ((ExecutableElement) enclosedElement).getParameters().isEmpty()) {
                return true;
            }

        }

        return false;
    }

    /**
     * Gets the {@link AnnotationMirror} corresponding to the given annotation type, if present.
     *
     * @param element           the annotated element
     * @param annotationElement the (element corresponding to the) type of annotation to be returned
     * @return the annotation mirror for the element's annotation of the requested type, or
     * {@code null} if none is found
     */
    public static AnnotationMirror getAnnotationMirror(Element element,
                                                       TypeElement annotationElement) {

        if ((element == null) || (annotationElement == null)) {
            return null;
        }

        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {

            if (annotationMirror.getAnnotationType().asElement().equals(annotationElement)) {
                return annotationMirror;
            }

        }

        return null;
    }

    /**
     * @param element             the element
     * @param enclosedElementName the name of the enclosed element required
     * @return the enclosed element of the given name, or {@code null} if not found
     */
    public static Element getEnclosedElement(Element element, String enclosedElementName) {

        if ((element == null) || (enclosedElementName == null)) {
            return null;
        }

        for (Element enclosedElement : element.getEnclosedElements()) {

            if (enclosedElement.getSimpleName().contentEquals(enclosedElementName)) {
                return enclosedElement;
            }

        }

        return null;
    }

    /**
     * @param element        the element
     * @param annotationType the type (class) of the annotation to be searched for
     * @return the enclosed elements of the given element that are annotated with the requested
     * annotation
     */
    public static List<Element> getAnnotatedEnclosedElements(Element element,
                                                             Class<? extends Annotation> annotationType) {

        if ((element == null) || (annotationType == null)) {
            return new ArrayList<Element>(0);
        }

        List<Element> annotatedEnclosedElements = new ArrayList<Element>();

        for (Element enclosedElement : element.getEnclosedElements()) {

            if (enclosedElement.getAnnotation(annotationType) != null) {
                annotatedEnclosedElements.add(enclosedElement);
            }

        }

        return annotatedEnclosedElements;
    }

    /**
     * @param element the element
     * @return the declared types of any annotations on the element, in the same order as
     * the annotations returned by {@link Element#getAnnotationMirrors()}
     */
    public static List<DeclaredType> getAnnotationTypes(Element element) {

        if (element == null) {
            return new ArrayList<DeclaredType>(0);
        }

        List<DeclaredType> annotationTypes = new ArrayList<DeclaredType>();

        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            annotationTypes.add(annotationMirror.getAnnotationType());
        }

        return annotationTypes;
    }

}
