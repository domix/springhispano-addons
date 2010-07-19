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

    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(new JavaType(RooCopyFields.class.getName()));
    }

    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        removeMetadataTrigger(new JavaType(RooCopyFields.class.getName()));
    }

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

    public String getItdUniquenessFilenameSuffix() {
        return "CopyFields";
    }

    protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
        JavaType javaType = CopyFieldsMetadata.getJavaType(metadataIdentificationString);
        Path path = CopyFieldsMetadata.getPath(metadataIdentificationString);
        String physicalTypeIdentifier = PhysicalTypeIdentifier.createIdentifier(javaType, path);
        return physicalTypeIdentifier;
    }

    protected String createLocalIdentifier(JavaType javaType, Path path) {
        return CopyFieldsMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return CopyFieldsMetadata.getMetadataIdentiferType();
    }
}