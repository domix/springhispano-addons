package org.springhispano.roo.addon.tdb;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.DefaultFieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * TODO: descripcion
 *
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 *         Date: Jul 10, 2011
 *         Time: 3:59:06 PM
 */
public class FieldDefaultValueCodeBuilderTest {

    private static final Logger LOGGER = Logger.getLogger(FieldDefaultValueCodeBuilderTest.class);

    @Test
    public void shouldCreateDefaultValueForDoubleObject() {
        validateDefaultValueFor("Double Object", JavaType.DOUBLE_OBJECT,
                new AnnotationMetadataBuilder[]{
                        new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.NotNull"))
                },
                "new Integer(1).doubleValue()");
    }

    @Test
    public void shouldCreateDefaultValueForDoublePrimitive() {
        validateDefaultValueFor("Double primitive", JavaType.DOUBLE_PRIMITIVE,
                new AnnotationMetadataBuilder[]{
                        new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.NotNull"))
                },
                "new Integer(1).doubleValue()");
    }

    @Test
    public void shouldCreateNullValueForDoubleObject() {
        validateDefaultValueFor("Null Double Object", JavaType.DOUBLE_OBJECT, "null");
    }

    @Test
    public void shouldCreateDefaultValueForBooleanObject() {
        validateDefaultValueFor("Boolean Object", JavaType.BOOLEAN_OBJECT,
                new AnnotationMetadataBuilder[]{
                        new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.NotNull"))
                },
                "new Boolean(true)");
    }

    @Test
    public void shouldCreateDefaultValueForBooleanPrimitive() {
        validateDefaultValueFor("Boolean primitive", JavaType.BOOLEAN_PRIMITIVE,
                new AnnotationMetadataBuilder[]{
                        new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.NotNull"))
                },
                "true");
    }

    @Test
    public void shouldCreateNullValueForBooleanObject() {
        validateDefaultValueFor("Null Boolean Object", JavaType.BOOLEAN_OBJECT, "null");
    }

    @Test
    public void shouldCreateDefaultValueForByteObject() {
        validateDefaultValueFor("Byte Object", JavaType.BYTE_OBJECT,
                new AnnotationMetadataBuilder[]{
                    new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.NotNull"))
                },
                "new Byte(0)");
    }

    @Test
    public void shouldCreateDefaultValueForBytePrimitive() {
        validateDefaultValueFor("Byte primitive", JavaType.BYTE_PRIMITIVE,
                new AnnotationMetadataBuilder[]{
                    new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.NotNull"))
                },
                "new Byte(0).byteValue()");
    }

    @Test
    public void shouldCreateNullValueForByteObject() {
        validateDefaultValueFor("Null Byte Object", JavaType.BYTE_OBJECT, "null");
    }

    @Test
    public void shouldCreateDefaultValueForCharObject() {
        validateDefaultValueFor("Char Object", JavaType.CHAR_OBJECT,
                new AnnotationMetadataBuilder[]{
                    new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.NotNull"))
                },
                "new Character('\u0000')");
    }

    @Test
    public void shouldCreateDefaultValueForCharPrimitive() {
        validateDefaultValueFor("Char primitive", JavaType.CHAR_PRIMITIVE,
                new AnnotationMetadataBuilder[]{
                    new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.NotNull"))
                },
                "new Character('\u0000').charValue()");
    }

    @Test
    public void shouldCreateNullValueForCharObject() {
        validateDefaultValueFor("Null Char Object", JavaType.CHAR_OBJECT, "null");
    }

    private void validateDefaultValueFor(String desc, JavaType jt, String expectedDefaultValue) {
        FieldMetadata fm = createFieldMetadataFor(jt);
        FieldDefaultValueCodeBuilder builder = new FieldDefaultValueCodeBuilder(fm);
        String defaultValue = builder.getDefaultValue();
        LOGGER.debug("[" + desc + "] = [" + defaultValue + ']');
        assertThat(defaultValue, is(expectedDefaultValue));
    }

    private void validateDefaultValueFor(String desc, JavaType jt,
                                         AnnotationMetadataBuilder[] annotationMetadataBuilders,
                                         String expectedDefaultValue) {
        FieldMetadata fm = createFieldMetadataFor(jt, Arrays.asList(annotationMetadataBuilders));
        FieldDefaultValueCodeBuilder builder = new FieldDefaultValueCodeBuilder(fm);
        String defaultValue = builder.getDefaultValue();
        LOGGER.debug("[" + desc + "], E[" + expectedDefaultValue + "], A[" + defaultValue + ']');
        assertThat(defaultValue, is(expectedDefaultValue));
    }

    private FieldMetadata createFieldMetadataFor(JavaType javaType) {
        String physicalTypeIdentifier = createPhysicalTypeIdentifierFor(this.getClass());
        FieldMetadata fm = new FieldMetadataBuilder(
                physicalTypeIdentifier,
                Modifier.PUBLIC,
                new JavaSymbolName("campo"),
                javaType,
                ""
        ).build();
        return fm;
    }

    private FieldMetadata createFieldMetadataFor(JavaType javaType,
                                                 List<AnnotationMetadataBuilder> annotationMetadataBuilders) {
        String physicalTypeIdentifier = createPhysicalTypeIdentifierFor(this.getClass());
        FieldMetadata fm = new FieldMetadataBuilder(
                physicalTypeIdentifier,
                Modifier.PUBLIC,
                annotationMetadataBuilders,
                new JavaSymbolName("campo"),
                javaType
        ).build();
        return fm;
    }

    private String createPhysicalTypeIdentifierFor(Class clazz) {
        JavaType type = new JavaType(clazz.getName());
        Path path = Path.SRC_TEST_JAVA;
        String physicalTypeIdentifier = PhysicalTypeIdentifier.createIdentifier(type, path);
        //LOGGER.debug("type = [" + physicalTypeIdentifier + ']');
        return physicalTypeIdentifier;
    }
}
