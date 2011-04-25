package org.springhispano.roo.addon.auditablefields;

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
 * Provee los metadatos
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 */
@Component
@Service
public final class AuditableFieldsMetadataProvider extends AbstractItdMetadataProvider {

    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(new JavaType(RooAuditableFields.class.getName()));
    }

    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        removeMetadataTrigger(new JavaType(RooAuditableFields.class.getName()));
    }

    protected ItdTypeDetailsProvidingMetadataItem getMetadata(String metadataIdentificationString,
            JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {
        return new AuditableFieldsMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata);
    }

    public String getItdUniquenessFilenameSuffix() {
        return "AuditableFields";
    }

    protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
        JavaType javaType = AuditableFieldsMetadata.getJavaType(metadataIdentificationString);
        Path path = AuditableFieldsMetadata.getPath(metadataIdentificationString);
        String physicalTypeIdentifier = PhysicalTypeIdentifier.createIdentifier(javaType, path);
        return physicalTypeIdentifier;
    }

    protected String createLocalIdentifier(JavaType javaType, Path path) {
        return AuditableFieldsMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return AuditableFieldsMetadata.getMetadataIdentiferType();
    }
}