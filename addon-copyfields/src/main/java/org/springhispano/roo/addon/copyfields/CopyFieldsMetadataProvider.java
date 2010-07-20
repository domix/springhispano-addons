package org.springhispano.roo.addon.copyfields;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;

/**
 * Provee los metadatos {@link CopyfieldsMetadata}.
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 * 
 */
@Component
@Service
public final class CopyFieldsMetadataProvider extends AbstractItdMetadataProvider {

    /**
     * Activa el componente
     * @param context Contexto
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(new JavaType(RooCopyFields.class.getName()));
    }

    /**
     * Desactiva el componente
     * @param context Contexto
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        removeMetadataTrigger(new JavaType(RooCopyFields.class.getName()));
    }

    /**
     * Provee el metadata.
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(String metadataIdentificationString,
            JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {
        // Obtener los valores dentro de la anotacion
        CopyFieldsAnnotationValues annotationValues = new CopyFieldsAnnotationValues(
                governorPhysicalTypeMetadata);
        if (!annotationValues.isAnnotationFound()) {
            return null;
        }
        return new CopyFieldsMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, annotationValues.getMethodName());
    }

    /**
     * @return El Sufijo unico para el ITD
     */
    public String getItdUniquenessFilenameSuffix() {
        return "CopyFields";
    }

    /**
     * @return El identificador de los metadatos fisicos de la clase a la cual 
     * se le esta generando el ITD
     */
    protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
        JavaType javaType = CopyFieldsMetadata.getJavaType(metadataIdentificationString);
        Path path = CopyFieldsMetadata.getPath(metadataIdentificationString);
        String physicalTypeIdentifier = PhysicalTypeIdentifier.createIdentifier(javaType, path);
        return physicalTypeIdentifier;
    }

    /**
     * @param javaType
     * @param path
     * @returnEl Identificador del tipo que recibe
     */
    protected String createLocalIdentifier(JavaType javaType, Path path) {
        return CopyFieldsMetadata.createIdentifier(javaType, path);
    }

    /**
     * El identificador de los metadatos de este ITD
     */
    public String getProvidesType() {
        return CopyFieldsMetadata.getMetadataIdentiferType();
    }
}