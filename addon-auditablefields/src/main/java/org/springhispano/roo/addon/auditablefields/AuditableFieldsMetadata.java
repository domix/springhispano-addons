package org.springhispano.roo.addon.auditablefields;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.DefaultFieldMetadata;
import org.springframework.roo.classpath.details.DefaultMethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.DefaultAnnotationMetadata;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.operations.DateTime;
import org.springframework.roo.classpath.operations.jsr303.DateField;
import org.springframework.roo.classpath.operations.jsr303.DateFieldPersistenceType;
import org.springframework.roo.classpath.operations.jsr303.StringField;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.style.ToStringCreator;
import org.springframework.roo.support.util.Assert;

/**
 * Metadata que es disparada por la anotacion
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 */
public class AuditableFieldsMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String PROVIDES_TYPE_STRING = AuditableFieldsMetadata.class.getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public AuditableFieldsMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Assert.isTrue(isValid(identifier), "Metadata identification string '" + identifier
                + "' does not appear to be a valid");

        if (!isValid()) {
            return;
        }

        builder.addField(createDateField("fechaCreacion", "fecha_creacion", true));
        builder.addField(createUserField("usuarioCreacion", "usuario_creacion", true));
        builder.addField(createDateField("fechaModificacion", "fecha_modificacion", false));
        builder.addField(createUserField("usuarioModificacion", "usuario_modificacion", false));
        
        builder.addMethod(createSetterMethod("fechaCreacion", "java.util.Date"));
        builder.addMethod(createSetterMethod("fechaModificacion", "java.util.Date"));
        builder.addMethod(createSetterMethod("usuarioCreacion", "java.lang.String"));
        builder.addMethod(createSetterMethod("usuarioModificacion", "java.lang.String"));
        builder.addMethod(createGetterMethod("fechaCreacion", "java.util.Date"));
        builder.addMethod(createGetterMethod("fechaModificacion", "java.util.Date"));
        builder.addMethod(createGetterMethod("usuarioCreacion", "java.lang.String"));
        builder.addMethod(createGetterMethod("usuarioModificacion", "java.lang.String"));

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    private MethodMetadata createGetterMethod(String fieldName, String type) {
        JavaSymbolName methodName = new JavaSymbolName("get" + 
                Character.toUpperCase(fieldName.charAt(0)) + 
                fieldName.substring(1, fieldName.length()));
        
        // Verificar si el metodo ya existe declarado con ese nombre y sin recibir argumentos
        MethodMetadata method = methodExists(methodName, new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            return method;
        }
        
        // Para crear el cuerpo del metodo
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        body.appendFormalLine("return this." + fieldName + ';');

        List<AnnotatedJavaType> parameters = new ArrayList<AnnotatedJavaType>();
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
                                        
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName,
                new JavaType(type),
                parameters, parameterNames, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);
        return methodBuilder.build();
    }

    private MethodMetadata createSetterMethod(String fieldName, String type) {
        JavaSymbolName methodName = new JavaSymbolName("set" + 
                Character.toUpperCase(fieldName.charAt(0)) + 
                fieldName.substring(1, fieldName.length()));
        
        List<AnnotatedJavaType> parameters = new ArrayList<AnnotatedJavaType>();
        parameters.add(new AnnotatedJavaType(new JavaType(type), new ArrayList<AnnotationMetadata>()));
        
        // Verificar si el metodo ya existe declarado con ese nombre y con los arguementos
        MethodMetadata method = methodExists(methodName, parameters);
        if (method != null) {
            return method;
        }
        
        // Para crear el cuerpo del metodo
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        body.appendFormalLine("this." + fieldName + " = " + fieldName + ';');

        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName(fieldName));
        
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
                   
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC, methodName,
                JavaType.VOID_PRIMITIVE,
                parameters, parameterNames, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);
        return methodBuilder.build();
    }

    private FieldMetadata createDateField(String fieldName, String columnName, boolean notNull) {
        String physicalTypeIdentifier = PhysicalTypeIdentifier.createIdentifier(
                this.governorTypeDetails.getName(), Path.SRC_MAIN_JAVA);

        // Crear un DateField para a partir de los datos que se definan en este
        // se genere
        // el FieldMetadata
        DateField fieldDetails = new DateField(physicalTypeIdentifier, new JavaType(
                "java.util.Date"), new JavaSymbolName(fieldName));
        fieldDetails.setPersistenceType(DateFieldPersistenceType.JPA_TIMESTAMP);
        fieldDetails.setNotNull(notNull);
        fieldDetails.setColumn(columnName);
        fieldDetails.setDateFormat(DateTime.SHORT);
        fieldDetails.setTimeFormat(DateTime.NONE);

        // Esta es la ventaja de crear un DateField ya que con este podemos
        // generar las anotaciones adecuadas
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        fieldDetails.decorateAnnotationsList(annotations);
                                                                                             
        FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(getId(), Modifier.PRIVATE, fieldDetails.getFieldName(),
                fieldDetails.getFieldType(), null);
        fieldBuilder.setAnnotations(annotations);
        return fieldBuilder.build();
    }

    private FieldMetadata createUserField(String fieldName, String columnName, boolean notNull) {
        String physicalTypeIdentifier = PhysicalTypeIdentifier.createIdentifier(
                this.governorTypeDetails.getName(), Path.SRC_MAIN_JAVA);
        StringField fieldDetails = new StringField(physicalTypeIdentifier, new JavaType(
                "java.lang.String"), new JavaSymbolName(fieldName));
        fieldDetails.setNotNull(notNull);
        fieldDetails.setColumn(columnName);
        fieldDetails.setSizeMax(10); // No debe estar en duro

        // Esta es la ventaja de crear un StringField ya que con este podemos
        // generar las anotaciones adecuadas
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        fieldDetails.decorateAnnotationsList(annotations);

        FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(getId(), Modifier.PRIVATE, fieldDetails.getFieldName(),
                fieldDetails.getFieldType(), null);
        fieldBuilder.setAnnotations(annotations);
        return fieldBuilder.build();
    }

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
