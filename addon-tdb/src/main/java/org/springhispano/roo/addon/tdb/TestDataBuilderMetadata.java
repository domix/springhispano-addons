package org.springhispano.roo.addon.tdb;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultFieldMetadata;
import org.springframework.roo.classpath.details.DefaultMethodMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
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
            this.builder.addField(createField(field));
        }

        for (FieldMetadata field : cid.getDeclaredFields()) {
            this.builder.addMethod(createWithFieldMethod(field));
        }

        // Por ultimo crear el metodo build
        builder.addMethod(createBuildMethod(originalClassPhysicalTypeMetadata));

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    private FieldMetadata createField(FieldMetadata field) {
        List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>();
        return new DefaultFieldMetadata(getId(), Modifier.PRIVATE, field.getFieldName(),
                field.getFieldType(), null, annotations);
    }

    /**
     * Crea la definicion del metodo build.
     * 
     * @param originalClassPhysicalTypeMetadata
     * @return
     */
    private MethodMetadata createBuildMethod(PhysicalTypeMetadata originalClassPhysicalTypeMetadata) {
        JavaSymbolName methodName = new JavaSymbolName("build");

        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        body.appendFormalLine("System.out.println(\"Hello World\");");

        List<AnnotatedJavaType> parameters = new ArrayList<AnnotatedJavaType>();
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>();
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        return new DefaultMethodMetadata(getId(), Modifier.PUBLIC, methodName,
                JavaType.VOID_PRIMITIVE, parameters, parameterNames, annotations, throwsTypes,
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
        body.appendFormalLine("System.out.println(\"Hello World\");");
        body.appendFormalLine("return this;");

        List<AnnotatedJavaType> parameters = new ArrayList<AnnotatedJavaType>();
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>();
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        return new DefaultMethodMetadata(getId(), Modifier.PUBLIC, methodName,
                this.governorTypeDetails.getName(), // regresa un tipo de si
                                                    // mismo (this)
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