package org.springhispano.roo.addon.tdb;

import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Builder de default values para los atributos.
 *
 * La gran mayoria del codigo siguiente es una copia de la logica en el codigo de Addon
 * de Data On Demand ya que este entre todo lo demas que hace es crear valores default
 * a los campos del objeto que esta generando sus pruebas
 *
 * NOTA: Esta clase deberia ser codigo comun para todos los addons
 *
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 */
public class FieldDefaultValueCodeBuilder {
    private FieldMetadata field;

    public FieldDefaultValueCodeBuilder(FieldMetadata field) {
        this.field = field;
    }

    public String getDefaultValue() {
        String initializer = "null";

        if (isNullable()) {
            initializer = isPrimitiveType() ? null : "null";
        }
        else {
            if (isPrimitiveType()) {
                initializer = buildDefaultValueForPrimitiveTypes();
            }
            else if (isDateType()) {
                initializer = buildDefaultValueForDateTypes();
            }
            else if (isStringType()) {
                initializer = buildDefaultValueForStringTypes();
            }
            if (field.getFieldType().equals(JavaType.BOOLEAN_OBJECT)) {
                    initializer = "new Boolean(true)";
            }
            else if (field.getFieldType().equals(JavaType.INT_OBJECT)) {
                initializer = "new Integer(1)";
            }
            else if (field.getFieldType().equals(JavaType.DOUBLE_OBJECT)) {
                initializer = "new Integer(1).doubleValue()"; // Auto-boxed
            }
            else if (field.getFieldType().equals(JavaType.FLOAT_OBJECT)) {
                initializer = "new Integer(1).floatValue()"; // Auto-boxed
            }
            else if (field.getFieldType().equals(JavaType.LONG_OBJECT)) {
                initializer = "new Integer(1).longValue()"; // Auto-boxed
            }
            else if (field.getFieldType().equals(JavaType.SHORT_OBJECT)) {
                initializer = "new Integer(1).shortValue()"; // Auto-boxed
            }
            else if (field.getFieldType().equals(JavaType.BYTE_OBJECT)) {
                initializer = "new Byte(0)";                
            }
            else if (field.getFieldType().equals(JavaType.CHAR_OBJECT)) {
                initializer = "new Character('\u0000')";                
            }
            else if (isCalendarType()) {
                initializer = buildDefaultValueForCalendarTypes();
            }           
            else if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Size")) != null ||
                MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Min")) != null ||
                MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Max")) != null ||
                // Check for @ManyToOne annotation with 'optional = false' attribute (ROO-1075)
                isFieldWithManyToOneAnnotationWithOptionalInfalse() ||
                field.getAnnotations().size() == 0) {
                // TODO: Todavia tengo que entender que esta haciendo aqui
                /*
                else if (manyToOneAnnotation != null || MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.persistence.OneToOne")) != null) {
                    if (field.getFieldType().equals(this.getAnnotationValues().getEntity())) {
                        // Avoid circular references (ROO-562)
                        initializer = "obj";
                    } else {
                        requiredDataOnDemandCollaborators.add(field.getFieldType());
                        String collaboratingFieldName = getCollaboratingFieldName(field.getFieldType()).getSymbolName();

                        // Look up the metadata we are relying on
                        String otherProvider = DataOnDemandMetadata.createIdentifier(new JavaType(field.getFieldType() + "DataOnDemand"), Path.SRC_TEST_JAVA);

                        // Decide if we're dealing with a one-to-one and therefore should _try_ to keep the same id (ROO-568)
                        boolean oneToOne = MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.persistence.OneToOne")) != null;

                        metadataDependencyRegistry.registerDependency(otherProvider, getId());
                        DataOnDemandMetadata otherMd = (DataOnDemandMetadata) metadataService.get(otherProvider);
                        if (MaxAnnotationDataotherMd == null || !otherMd.isValid()) {
                            // There is no metadata around, so we'll just make some basic assumptions
                            if (oneToOne) {
                                initializer = collaboratingFieldName + ".getSpecific" + field.getFieldType().getSimpleTypeName() + "(index)";
                            } else {
                                initializer = collaboratingFieldName + ".getRandom" + field.getFieldType().getSimpleTypeName() + "()";
                            }
                        } else {
                            // We can use the correct name
                            if (oneToOne) {
                                initializer = collaboratingFieldName + "." + otherMd.getSpecificPersistentEntityMethod().getMethodName().getSymbolName() + "(index)";
                            } else {
                                initializer = collaboratingFieldName + "." + otherMd.getRandomPersistentEntityMethod().getMethodName().getSymbolName() + "()";
                            }
                        }
                    }
                } else if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.persistence.Enumerated")) != null) {
                    initializer = field.getFieldType().getFullyQualifiedTypeName() + ".class.getEnumConstants()[0]";
                }*/
            }
        }
        return initializer;
    }

    private boolean isCalendarType() {
        return field.getFieldType().equals(new JavaType(Calendar.class.getName()));
    }

    private String buildDefaultValueForCalendarTypes() {
        String initializer;
        if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Past")) != null) {
            initializer = "new java.util.GregorianCalendar(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR), java.util.Calendar.getInstance().get(java.util.Calendar.MONTH), java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH) - 1)";
        }
        else if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Future")) != null) {
            initializer = "new java.util.GregorianCalendar(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR), java.util.Calendar.getInstance().get(java.util.Calendar.MONTH), java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH) + 1)";
        }
        else {
            initializer = "java.util.Calendar.getInstance()";
        }
        return initializer;
    }

    private String buildDefaultValueForStringTypes() {
        String initializer;
        initializer = field.getFieldName().getSymbolName();

        // Check for @Size
        SizeAnnotationData size = getSizeAnnotationData();
        if (size != null) {
            initializer = buildDefaultValueForStringTypeApplyingSize(initializer, size);
        }

        // Check for @Max
        MaxAnnotationData max = getMaxAnnotationData();
        if (max != null) {
            if ((initializer.length() + 2) > max.getValue()) {
                initializer = initializer.substring(0, (int) (max.getValue() - 2));
            }
        }

        // Check for @Min
        AnnotationMetadata minAnnotationMetadata = MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Min"));
        if (minAnnotationMetadata != null) {
            AnnotationAttributeValue<?> valueAttributeValue =  minAnnotationMetadata.getAttribute(new JavaSymbolName("value"));
            if ((initializer.length() + 2) < (Integer) valueAttributeValue.getValue()) {
                initializer = String.format("%1$-" + ((Integer) valueAttributeValue.getValue() - 2) + "s", initializer).replace(' ', 'x');
            }
        }

        // Check for @Column
        AnnotationMetadata columnAnnotationMetadata = MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.persistence.Column"));
        if (columnAnnotationMetadata != null) {
            AnnotationAttributeValue<?> lengthAttributeValue =  columnAnnotationMetadata.getAttribute(new JavaSymbolName("length"));
            if (lengthAttributeValue != null && (initializer.length() + 2) > (Integer) lengthAttributeValue.getValue()) {
                initializer = initializer.substring(0, (Integer) lengthAttributeValue.getValue() - 2);
            }
        }

        initializer = "\"" + initializer + "_\" + 1";
        return initializer;
    }

    private MaxAnnotationData getMaxAnnotationData() {
        MaxAnnotationData max = null;
        AnnotationMetadata maxAnnotationMetadata = MemberFindingUtils.getAnnotationOfType(
                field.getAnnotations(), new JavaType("javax.validation.constraints.Max"));
        AnnotationAttributeValue<?> valueAttributeValue = null;
        if (maxAnnotationMetadata != null) {
            valueAttributeValue =  maxAnnotationMetadata.getAttribute(new JavaSymbolName("value"));
            max = new MaxAnnotationData(new Long((Integer) valueAttributeValue.getValue()));
        }
        return max;
    }

    private String buildDefaultValueForStringTypeApplyingSize(String initializer, SizeAnnotationData size) {
        if ((initializer.length() + 2) > size.getMax()) {
            initializer = initializer.substring(0, (int) (size.getMax() - 2));
        }
        if ((initializer.length() + 2) < size.getMin()) {
            initializer = String.format("%1$-" + (size.getMin() - 2) + "s", initializer).replace(' ', 'x');
        }
        return initializer;
    }

    private SizeAnnotationData getSizeAnnotationData() {
        SizeAnnotationData size = null;
        AnnotationMetadata sizeAnnotationMetadata =
                MemberFindingUtils.getAnnotationOfType(
                        field.getAnnotations(), new JavaType("javax.validation.constraints.Size"));
        AnnotationAttributeValue<?> minAttributeValue = null;
        AnnotationAttributeValue<?> maxAttributeValue = null;
        if (sizeAnnotationMetadata != null) {
            maxAttributeValue =  sizeAnnotationMetadata.getAttribute(new JavaSymbolName("max"));
            minAttributeValue =  sizeAnnotationMetadata.getAttribute(new JavaSymbolName("min"));
            size = new SizeAnnotationData(
                new Long((Integer) minAttributeValue.getValue()), new Long((Integer) maxAttributeValue.getValue()));
        }
        return size;
    }

    private boolean isStringType() {
        return field.getFieldType().equals(new JavaType(String.class.getName()));
    }

    private String buildDefaultValueForPrimitiveTypes() {
        String initializer = null;
        if (field.getFieldType().equals(JavaType.BOOLEAN_PRIMITIVE)) {
            initializer = "true";
        }
        else if (field.getFieldType().equals(JavaType.INT_PRIMITIVE)) {
            initializer = "new Integer(1)"; // Auto-boxed
        }
        else if (field.getFieldType().equals(JavaType.DOUBLE_PRIMITIVE)) {
            initializer = "new Integer(1).doubleValue()";
        }
        else if (field.getFieldType().equals(JavaType.FLOAT_PRIMITIVE)) {
            initializer = "new Integer(1).floatValue()";
        }
        else if (field.getFieldType().equals(JavaType.LONG_PRIMITIVE)) {
            initializer = "new Integer(1).longValue()";
        }
        else if (field.getFieldType().equals(JavaType.SHORT_PRIMITIVE)) {
            initializer = "new Integer(1).shortValue()";
        }
        else if (field.getFieldType().equals(JavaType.BYTE_PRIMITIVE)) {
            initializer = "new Byte(0).byteValue()";
        }
        else if (field.getFieldType().equals(JavaType.CHAR_PRIMITIVE)) {
            initializer = "new Character('\u0000').charValue()";
        }
        return initializer;
    }

    private boolean isPrimitiveType() {
        List<JavaType> primitives = new ArrayList<JavaType>();
        primitives.add(JavaType.BOOLEAN_PRIMITIVE);
        primitives.add(JavaType.BYTE_PRIMITIVE);
        primitives.add(JavaType.CHAR_PRIMITIVE);
        primitives.add(JavaType.DOUBLE_PRIMITIVE);
        primitives.add(JavaType.FLOAT_PRIMITIVE);
        primitives.add(JavaType.INT_PRIMITIVE);
        primitives.add(JavaType.LONG_PRIMITIVE);
        primitives.add(JavaType.SHORT_PRIMITIVE);
        primitives.add(JavaType.VOID_PRIMITIVE);
        return primitives.contains(field.getFieldType());
    }

    private boolean isNullable() {
        AnnotationMetadata annotations = MemberFindingUtils.getAnnotationOfType(
                field.getAnnotations(), new JavaType("javax.validation.constraints.NotNull"));
        if (annotations != null) {
            return false;
        }
        return true;
    }

    private boolean isFieldWithManyToOneAnnotationWithOptionalInfalse() {
        boolean hasManyToOne = false;
        AnnotationMetadata manyToOneAnnotation = MemberFindingUtils.getAnnotationOfType(
                field.getAnnotations(), new JavaType("javax.persistence.ManyToOne"));
        if (manyToOneAnnotation != null) {
            AnnotationAttributeValue<?> optionalAttribute = manyToOneAnnotation.getAttribute(
                    new JavaSymbolName("optional"));
            hasManyToOne = optionalAttribute != null && !((Boolean) optionalAttribute.getValue());
        }
        return hasManyToOne;
    }

    private String buildDefaultValueForDateTypes() {
        String initializer;
        String pack = field.getFieldType().equals(new JavaType(Date.class.getName())) ? "java.util." : "java.sql.";
        if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Past")) != null) {
            initializer = "new " + pack + "Date(new java.util.Date().getTime() - 10000000L)";
        }
        else if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Future")) != null) {
            initializer = "new " + pack + "Date(new java.util.Date().getTime() + 10000000L)";
        }
        else {
            initializer = "new " + pack + "Date()";
        }
        return initializer;
    }

    private boolean isDateType() {
        return field.getFieldType().equals(new JavaType(Date.class.getName())) ||
            field.getFieldType().equals(new JavaType(java.sql.Date.class.getName()));
    }

    private class SizeAnnotationData {
        private Long min;
        private Long max;

        public SizeAnnotationData(Long min, Long max) {
            this.min = min == null ? new Long(0) : min;
            this.max = max == null ? new Long(0) : max;
        }

        public Long getMin() {
            return min;
        }

        public Long getMax() {
            return max;
        }
    }

    private class MaxAnnotationData {
        private Long value;

        public MaxAnnotationData(Long value) {
            this.value = value == null ? new Long(0) : value;
        }

        public Long getValue() {
            return value;
        }
    }
}
