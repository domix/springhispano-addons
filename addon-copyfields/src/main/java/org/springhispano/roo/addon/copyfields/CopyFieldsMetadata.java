package org.springhispano.roo.addon.copyfields;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.DefaultMethodMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.style.ToStringCreator;
import org.springframework.roo.support.util.Assert;

/**
 * Metadatos que son generados por la anotacion para copiar campos.
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 * 
 */
public class CopyFieldsMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String PROVIDES_TYPE_STRING = CopyFieldsMetadata.class.getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    /**
     * Constructor que crea el ITD del metadato
     * @param identifier
     * @param aspectName
     * @param governorPhysicalTypeMetadata
     * @param methodName
     */
    public CopyFieldsMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata, String methodName) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Assert.isTrue(isValid(identifier), "Metadata identification string '" + identifier
                + "' does not appear to be a valid");

        if (!isValid()) {
            return;
        }
        
        if (methodName == null) {
            methodName = "copyFields";
        }

        // Si no hay campos, no se genera nada
        if (this.governorTypeDetails.getDeclaredFields().size() > 0) {
            builder.addMethod(createCopyFieldsMethod(methodName));
        }
        // NOTE: no sera bueno tambien generar el metodo clone e implementar la interfaz?

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Genera el metodo para copiar los campos
     * @param name Nombre que va a tener el metodo
     * @return Los metadatos del metodo
     */
    private MethodMetadata createCopyFieldsMethod(String name) {
        JavaSymbolName methodName = new JavaSymbolName(name);

        // Verificar si el metodo ya existe declarado con ese nombre y sin
        // recibir argumentos
        MethodMetadata method = methodExists(methodName, new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            return method;
        }

        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();

        // TODO: No dar por hecho que existen los getters y setters
        String auxMethodName = null;
        for (FieldMetadata field : this.governorTypeDetails.getDeclaredFields()) {
            auxMethodName = buildMethodName(field.getFieldName().getSymbolName());
            body.appendFormalLine("this.set" + auxMethodName + 
                    "(object.get" + auxMethodName + "());");
        }

        List<AnnotatedJavaType> parameters = new ArrayList<AnnotatedJavaType>();
        parameters.add(new AnnotatedJavaType(
                this.governorTypeDetails.getName(), new ArrayList<AnnotationMetadata>()));

        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("object"));

        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
                  
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC, methodName,
                JavaType.VOID_PRIMITIVE,
                parameters, parameterNames, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);
        return methodBuilder.build();
    }

    /**
     * Construye el nombre del metodo a partir del nombre del campo
     * @param fieldName Nombre del campo
     * @return El nombre del metodo
     */
    private String buildMethodName(String fieldName) {
        return Character.toUpperCase(fieldName.charAt(0))
                + fieldName.substring(1, fieldName.length());
    }

    /**
     * Verifica si el metodo existe con ese nombre y con los parametros en la clase a la cual
     * se le esta generando el ITD
     * @param methodName Nombre del metodo
     * @param paramTypes Lista de tipos que recibe como parametros el metodo
     * @return null si no existe el metodo, los metadatos del metadato de lo contrario
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
