package org.springhispano.roo.addon.tdb;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.roo.addon.javabean.JavaBeanMetadata;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataService;
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
    
    private static final Logger LOGGER = Logger.getLogger(TestDataBuilderMetadata.class.getName());

    private static final String PROVIDES_TYPE_STRING = TestDataBuilderMetadata.class.getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);
    
    private MetadataService metadataService;

    /**
     * Constructor
     * 
     * @param identifier
     * @param aspectName
     * @param tdbClassPhysicalTypeMetadata
     * @param originalClassPhysicalTypeMetadata
     * @param metadataService 
     */
    public TestDataBuilderMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata tdbClassPhysicalTypeMetadata,
            PhysicalTypeMetadata originalClassPhysicalTypeMetadata, MetadataService metadataService) {
        super(identifier, aspectName, tdbClassPhysicalTypeMetadata);
        Assert.isTrue(isValid(identifier), "Metadata identification string '" + identifier
                + "' does not appear to be a valid");
        this.metadataService = metadataService;

        if (!isValid()) {
            return;
        }

        ClassOrInterfaceTypeDetails tdbClassCid =
                getClassOrInterfaceTypeDetailsFromPhysicalTypeMetadataOrNull(tdbClassPhysicalTypeMetadata);

        ClassOrInterfaceTypeDetails originalClassCid =
               getClassOrInterfaceTypeDetailsFromPhysicalTypeMetadataOrNull(originalClassPhysicalTypeMetadata);

        // DUDA: Porque puede venir nulo originalClassCid?
        if (originalClassCid != null && tdbClassCid != null) {
            // Por cada atributo de la clase se agrega uno igual al TDB
            for (FieldMetadata field : originalClassCid.getDeclaredFields()) {
                if (isIgnorableField(field)) {
                    continue;
                }
                
                if (!existsFieldNameInClass(field.getFieldName(), tdbClassCid)) {
                    this.builder.addField(createField(field));
                }

                // addMethod, asi como addField lanzan IllegalArgumentException si el campo o
                // metodo ya existen definidos
                try {
                    this.builder.addMethod(createWithFieldMethod(field));
                }
                catch (IllegalArgumentException ex) {
                }

                if (!isPrimitiveField(field)) {
                    try {
                        this.builder.addMethod(createWithNoFieldMethod(field));
                    }
                    catch (IllegalArgumentException ex) {
                    }
                }
            }

            builder.addMethod(createBuildMethod(originalClassCid));
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    private boolean existsFieldNameInClass(JavaSymbolName fieldName, ClassOrInterfaceTypeDetails cid) {
        if (MemberFindingUtils.getField(cid, fieldName) == null) {
            return false;
        }
        return true;
    }

    private ClassOrInterfaceTypeDetails getClassOrInterfaceTypeDetailsFromPhysicalTypeMetadataOrNull(
            PhysicalTypeMetadata physicalTypeMetadata) {
        PhysicalTypeDetails physicalTypeDetails = physicalTypeMetadata.getMemberHoldingTypeDetails();
        if (physicalTypeDetails != null && physicalTypeDetails instanceof ClassOrInterfaceTypeDetails) {
            return (ClassOrInterfaceTypeDetails) physicalTypeDetails;
        }
        return null;
    }

    /** 
     * @param field Metadatos del campo
     * @return true si el field que se recibe tiene un tipo de Java primitivo, false de lo contrario
     */
    private boolean isPrimitiveField(FieldMetadata field) {
        JavaType fieldJavaType = field.getFieldType();
        JavaType[] primitiveJavaTypes = new JavaType[]{
                JavaType.BOOLEAN_PRIMITIVE, JavaType.BYTE_PRIMITIVE, JavaType.CHAR_PRIMITIVE, 
                JavaType.DOUBLE_PRIMITIVE, JavaType.FLOAT_PRIMITIVE, JavaType.INT_PRIMITIVE,
                JavaType.LONG_PRIMITIVE, JavaType.SHORT_PRIMITIVE,
        };
        for (int index = 0; index < primitiveJavaTypes.length; ++index) {
            if (primitiveJavaTypes[index].equals(fieldJavaType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 
     * @param field
     * @return true si es un cam
     */
    private boolean isIgnorableField(FieldMetadata field) {
        // Aquellos campos Id o Version no tiene caso inclurlos
        if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.persistence.Id")) != null || 
                MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.persistence.Version")) != null) {
            return true;
        }

        // Aquellos campos Transient tampoco tiene caso inclurlos
        if (MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.persistence.Transient")) != null) {
            return true;
        }
        
        // Si son estaticos y finales ignorarlos
        if (Modifier.isFinal(field.getModifier()) && Modifier.isStatic(field.getModifier())) {
            return true;
        }
 
        return false;
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
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, this.governorTypeDetails.getName(),
                parameters, parameterNames, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);
        return methodBuilder.build();
    }

    /**
     * Crea el campo
     * @param field Metadata del campo de la clase a la que se le va a crear el TDB
     * @return Metadata del campo de la clase TDB
     */
    private FieldMetadata createField(FieldMetadata field) {
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(getId(), Modifier.PRIVATE, field.getFieldName(),
                field.getFieldType(), new FieldDefaultValueCodeBuilder(field).getDefaultValue());
        fieldBuilder.setAnnotations(annotations);
        return fieldBuilder.build();
    }

    /**
     * Crea la definicion del metodo build.
     * 
     * @param cid
     * @return
     */
    private MethodMetadata createBuildMethod(ClassOrInterfaceTypeDetails cid) {
        //LOGGER.info("Creando el metodo build");
        JavaSymbolName methodName = new JavaSymbolName("build");

        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        
        // TODO: Que pasa si la clase no tiene gets y sets y solo tiene un constructor??
        String objectName = "clazz";
        body.appendFormalLine(cid.getName().getFullyQualifiedTypeName() + 
                ' ' + objectName + " = new " + cid.getName() + "();");
        String fieldName = null;
        for (FieldMetadata field : cid.getDeclaredFields()) {
            if (isIgnorableField(field)) {
                continue;
            }
            
            fieldName = field.getFieldName().getSymbolName();
            String setMethodStr = "set" + Character.toUpperCase(fieldName.charAt(0)) + 
                fieldName.substring(1, fieldName.length());
            
            // Verificar la existencia del metodo setXXX
            List<JavaType> methodParams = new ArrayList<JavaType>();
            methodParams.add(field.getFieldType());
            MethodMetadata methodMetadata = 
                MemberFindingUtils.getMethod(cid, new JavaSymbolName(setMethodStr), methodParams);
            //LOGGER.info("methodMetadata = [" + methodMetadata + ']');
            
            // Si no existe en el propio archivo .java entonces probablemente en el @RooJavaBean
            if (methodMetadata == null) {
                String metadataIdentifier = 
                    JavaBeanMetadata.createIdentifier(cid.getName(), Path.SRC_MAIN_JAVA);
                JavaBeanMetadata javaBeanMetadata = 
                    (JavaBeanMetadata) this.metadataService.get(metadataIdentifier);
                if (javaBeanMetadata != null) {
                    methodMetadata = javaBeanMetadata.getDeclaredSetter(field);
                }
            }
            
            // Solo si existe el metodo setXXX se agrega al codigo
            if (methodMetadata != null) {
                body.appendFormalLine(objectName + "." + setMethodStr + "(this." + fieldName + ");");
            }
        }
        body.appendFormalLine("return " + objectName + ';');;

        List<AnnotatedJavaType> parameters = new ArrayList<AnnotatedJavaType>();
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC, methodName,
                cid.getName(), parameters, parameterNames, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);
        return methodBuilder.build();
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
        
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC, methodName,
                this.governorTypeDetails.getName(), parameters, parameterNames, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);
        return methodBuilder.build();
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
