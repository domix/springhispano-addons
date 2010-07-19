package org.springhispano.roo.addon.tdb;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultFieldMetadata;
import org.springframework.roo.classpath.details.DefaultMethodMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.style.ToStringCreator;
import org.springframework.roo.support.util.Assert;

/**
 * Metadata
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 * 
 */
public class TestDataBuilderMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String PROVIDES_TYPE_STRING = TestDataBuilderMetadata.class.getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    /**
     * Constructor
     * 
     * @param identifier
     * @param aspectName
     * @param governorPhysicalTypeMetadata
     * @param originalClassPhysicalTypeMetadata
     */
    public TestDataBuilderMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            PhysicalTypeMetadata originalClassPhysicalTypeMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Assert.isTrue(isValid(identifier), "Metadata identification string '" + identifier
                + "' does not appear to be a valid");

        if (!isValid()) {
            return;
        }

        ClassOrInterfaceTypeDetails cid = (ClassOrInterfaceTypeDetails) originalClassPhysicalTypeMetadata
                .getPhysicalTypeDetails();

        // Por cada atributo de la clase se agrega uno igual al TDB
        for (FieldMetadata field : cid.getDeclaredFields()) {
            // Aquellos campos Id o Version no tiene caso inclurlos
            if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.persistence.Id")) != null || 
                    MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.persistence.Version")) != null) {
                continue;
            }

            // Aquellos campos Transient tampoco tiene caso inclurlos
            if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.persistence.Transient")) != null) {
                continue;
            }

            this.builder.addField(createField(field));
            this.builder.addMethod(createWithFieldMethod(field));
            this.builder.addMethod(createWithNoFieldMethod(field));
        }

        // Por ultimo crear el metodo build
        builder.addMethod(createBuildMethod(cid));

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Crea el metodo withNoXXXX para dejar el campo en null.
     * @param field Campo
     * @return Metadata del metodo
     */
    private MethodMetadata createWithNoFieldMethod(FieldMetadata field) {
        String fieldName = field.getFieldName().getSymbolName();

        // TODO: Verificar esto si el nombre del atributo es de un solo caracter
        JavaSymbolName methodName = new JavaSymbolName("withNo"
                + Character.toUpperCase(fieldName.charAt(0))
                + fieldName.substring(1, fieldName.length()));

        // Verificar si el metodo ya existe declarado
        MethodMetadata method = methodExists(methodName, new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            return method;
        }

        // Para crear el cuerpo del metodo
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        body.appendFormalLine("this." + fieldName + " = null;");
        body.appendFormalLine("return this;");

        List<AnnotatedJavaType> parameters = new ArrayList<AnnotatedJavaType>();
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>();
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        return new DefaultMethodMetadata(getId(), Modifier.PUBLIC, methodName,
                this.governorTypeDetails.getName(), // regresa un tipo de si mismo (this)
                parameters, parameterNames, annotations, throwsTypes, body.getOutput());
    }

    /**
     * Crea el campo
     * @param field Metadata del campo de la clase a la que se le va a crear el TDB
     * @return Metadata del campo de la clase TDB
     */
    private FieldMetadata createField(FieldMetadata field) {
        List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>();
        return new DefaultFieldMetadata(getId(), Modifier.PRIVATE, field.getFieldName(),
                field.getFieldType(), getDefaultValue(field), annotations);
    }

    private String getDefaultValue(FieldMetadata field) {
        /*
         * La gran mayoria del codigo siguiente es una copia de la logica en el codigo de Addon
         * de Data On Demand ya que este entre todo lo demas que hace es crear valores default
         * a los campos del objeto que esta generando sus pruebas
         * 
         */
        
        // Check for @ManyToOne annotation with 'optional = false' attribute (ROO-1075)
        boolean hasManyToOne = false;
        AnnotationMetadata manyToOneAnnotation = MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.persistence.ManyToOne"));
        if (manyToOneAnnotation != null) {
            AnnotationAttributeValue<?> optionalAttribute = manyToOneAnnotation.getAttribute(new JavaSymbolName("optional"));
            hasManyToOne = optionalAttribute != null && !((Boolean) optionalAttribute.getValue());
        }
        
        String initializer = "null"; 
        if (field.getFieldType().equals(new JavaType(Date.class.getName()))) {
            if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Past")) != null) {
                initializer = "new java.util.Date(new java.util.Date().getTime() - 10000000L)";
            } else if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Future")) != null) {
                initializer = "new java.util.Date(new java.util.Date().getTime() + 10000000L)";
            } else {
                initializer = "new java.util.Date()";
            }
        } else if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.NotNull")) != null || MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Size")) != null || MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Min")) != null || MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Max")) != null || hasManyToOne || field.getAnnotations().size() == 0) {
            // Only include the field if it's really required (ie marked with JSR 303 NotNull) or it has no annotations and is therefore probably simple to invoke
            if (field.getFieldType().equals(new JavaType(String.class.getName()))) {
                initializer = field.getFieldName().getSymbolName();
                
                // Check for @Size
                AnnotationMetadata sizeAnnotationMetadata = MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Size"));
                if (sizeAnnotationMetadata != null) {
                    AnnotationAttributeValue<?> maxAttributeValue =  sizeAnnotationMetadata.getAttribute(new JavaSymbolName("max"));
                    if (maxAttributeValue != null && (initializer.length() + 2) > (Integer) maxAttributeValue.getValue()) {
                        initializer = initializer.substring(0, (Integer) maxAttributeValue.getValue() - 2); 
                    }
                    AnnotationAttributeValue<?> minAttributeValue =  sizeAnnotationMetadata.getAttribute(new JavaSymbolName("min"));
                    if (minAttributeValue != null && (initializer.length() + 2) < (Integer) minAttributeValue.getValue()) {
                        initializer = String.format("%1$-" + ((Integer) minAttributeValue.getValue() - 2) + "s", initializer).replace(' ', 'x'); 
                    }
                }
                
                // Check for @Max
                AnnotationMetadata maxAnnotationMetadata = MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Max"));
                if (maxAnnotationMetadata != null) {
                    AnnotationAttributeValue<?> valueAttributeValue =  maxAnnotationMetadata.getAttribute(new JavaSymbolName("value"));
                    if ((initializer.length() + 2) > (Integer) valueAttributeValue.getValue()) {
                        initializer = initializer.substring(0, (Integer) valueAttributeValue.getValue() - 2); 
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
            } else if (field.getFieldType().equals(new JavaType(Calendar.class.getName()))) {
                if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Past")) != null) {
                    initializer = "new java.util.GregorianCalendar(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR), java.util.Calendar.getInstance().get(java.util.Calendar.MONTH), java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH) - 1)";
                } else if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Future")) != null) {
                    initializer = "new java.util.GregorianCalendar(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR), java.util.Calendar.getInstance().get(java.util.Calendar.MONTH), java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH) + 1)";
                } else {
                    initializer = "java.util.Calendar.getInstance()";
                }
            } else if (field.getFieldType().equals(JavaType.BOOLEAN_OBJECT)) {
                initializer = "new Boolean(true)";
            } else if (field.getFieldType().equals(JavaType.BOOLEAN_PRIMITIVE)) {
                initializer = "true";
            } else if (field.getFieldType().equals(JavaType.INT_OBJECT)) {
                initializer = "new Integer(index)";
            } else if (field.getFieldType().equals(JavaType.INT_PRIMITIVE)) {
                initializer = "new Integer(index)"; // Auto-boxed
            } else if (field.getFieldType().equals(JavaType.DOUBLE_OBJECT)) {
                initializer = "new Integer(index).doubleValue()"; // Auto-boxed
            } else if (field.getFieldType().equals(JavaType.DOUBLE_PRIMITIVE)) {
                initializer = "new Integer(index).doubleValue()";
            } else if (field.getFieldType().equals(JavaType.FLOAT_OBJECT)) {
                initializer = "new Integer(index).floatValue()"; // Auto-boxed
            } else if (field.getFieldType().equals(JavaType.FLOAT_PRIMITIVE)) {
                initializer = "new Integer(index).floatValue()";
            } else if (field.getFieldType().equals(JavaType.LONG_OBJECT)) {
                initializer = "new Integer(index).longValue()"; // Auto-boxed
            } else if (field.getFieldType().equals(JavaType.LONG_PRIMITIVE)) {
                initializer = "new Integer(index).longValue()";
            } else if (field.getFieldType().equals(JavaType.SHORT_OBJECT)) {
                initializer = "new Integer(index).shortValue()"; // Auto-boxed
            } else if (field.getFieldType().equals(JavaType.SHORT_PRIMITIVE)) {
                initializer = "new Integer(index).shortValue()";
            }
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
                    if (otherMd == null || !otherMd.isValid()) {
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
        return initializer;
    }

    /**
     * Crea la definicion del metodo build.
     * 
     * @param cid
     * @return
     */
    private MethodMetadata createBuildMethod(ClassOrInterfaceTypeDetails cid) {
        JavaSymbolName methodName = new JavaSymbolName("build");

        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        
        // TODO: Que pasa si la clase no tiene gets y sets y solo tiene un constructor??
        String objectName = "clazz";
        body.appendFormalLine(cid.getName().getFullyQualifiedTypeName() + 
                ' ' + objectName + " = new " + cid.getName() + "();");
        String fieldName = null;
        for (FieldMetadata field : cid.getDeclaredFields()) {
            fieldName = field.getFieldName().getSymbolName();
            body.appendFormalLine(objectName + ".set" + 
                    Character.toUpperCase(fieldName.charAt(0)) + 
                    fieldName.substring(1, fieldName.length()) +
                    "(this." + fieldName + ");");
        }
        body.appendFormalLine("return " + objectName + ';');;

        List<AnnotatedJavaType> parameters = new ArrayList<AnnotatedJavaType>();
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>();
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        return new DefaultMethodMetadata(getId(), Modifier.PUBLIC, methodName,
                cid.getName(), parameters, parameterNames, annotations, throwsTypes,
                body.getOutput());
    }

    /**
     * Crea la definicion de un metodo withXXX.
     * 
     * @param field
     *            Metadatos del campo.
     * @return Metadata del metodo.
     */
    private MethodMetadata createWithFieldMethod(FieldMetadata field) {
        String fieldName = field.getFieldName().getSymbolName();

        // TODO: Verificar esto si el nombre del atributo es de un solo caracter
        JavaSymbolName methodName = new JavaSymbolName("with"
                + Character.toUpperCase(fieldName.charAt(0))
                + fieldName.substring(1, fieldName.length()));

        // Verificar si el metodo ya existe declarado
        MethodMetadata method = methodExists(methodName, new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            return method;
        }

        // Para crear el cuerpo del metodo
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        body.appendFormalLine("this." + fieldName + " = " + fieldName + ';');
        body.appendFormalLine("return this;");

        List<AnnotatedJavaType> parameters = new ArrayList<AnnotatedJavaType>();
        parameters.add(
                new AnnotatedJavaType(field.getFieldType(), new ArrayList<AnnotationMetadata>()));
        
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(field.getFieldName());
        
        List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>();
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        return new DefaultMethodMetadata(getId(), Modifier.PUBLIC, methodName,
                this.governorTypeDetails.getName(), // regresa un tipo de si mismo (this)
                parameters, parameterNames, annotations, throwsTypes, body.getOutput());
    }

    /**
     * Verifica si un metodo existe ya definido en la clase.
     * 
     * @param methodName
     *            Nombre del metodo
     * @param paramTypes
     *            Lista de tipos de los parametros del metodo
     * @return Los metadatos del metodo si existe, null de lo contrario
     */
    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        // We have no access to method parameter information, so we scan by name
        // alone and treat any match as authoritative
        // We do not scan the superclass, as the caller is expected to know
        // we'll only scan the current class
        for (MethodMetadata method : governorTypeDetails.getDeclaredMethods()) {
            if (method.getMethodName().equals(methodName)
                    && method.getParameterTypes().equals(paramTypes)) {
                // Found a method of the expected name; we won't check method
                // parameters though
                return method;
            }
        }
        return null;
    }

    public String toString() {
        ToStringCreator tsc = new ToStringCreator(this);
        tsc.append("identifier", getId());
        tsc.append("valid", valid);
        tsc.append("aspectName", aspectName);
        tsc.append("destinationType", destination);
        tsc.append("governor", governorPhysicalTypeMetadata.getId());
        tsc.append("itdTypeDetails", itdTypeDetails);
        return tsc.toString();
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType, Path path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, javaType,
                path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static final Path getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

}
