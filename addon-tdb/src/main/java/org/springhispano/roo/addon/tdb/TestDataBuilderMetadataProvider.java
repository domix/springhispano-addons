package org.springhispano.roo.addon.tdb;

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
 * Proveedor de metadatos.
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 * 
 */
@Component
@Service
public class TestDataBuilderMetadataProvider extends AbstractItdMetadataProvider {

    /**
     * Llamado al activar el componente
     * 
     * @param context
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(new JavaType(RooTestDataBuilder.class.getName()));
    }

    /**
     * Llamado al desactivar el componente
     * 
     * @param context
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        removeMetadataTrigger(new JavaType(RooTestDataBuilder.class.getName()));
    }

    /**
     * return El Sufijo del ITD
     */
    public String getItdUniquenessFilenameSuffix() {
        return "TestDataBuilder";
    }

    public String getProvidesType() {
        return TestDataBuilderMetadata.getMetadataIdentiferType();
    }

    @Override
    protected String createLocalIdentifier(JavaType javaType, Path path) {
        return TestDataBuilderMetadata.createIdentifier(javaType, path);
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
        JavaType javaType = TestDataBuilderMetadata.getJavaType(metadataIdentificationString);
        Path path = TestDataBuilderMetadata.getPath(metadataIdentificationString);
        String physicalTypeIdentifier = PhysicalTypeIdentifier.createIdentifier(javaType, path);
        return physicalTypeIdentifier;
    }

    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(String metadataIdentificationString,
            JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        // Verificar que la clase a la cual se le intenta crear el
        // TestDataBuilder tenga la anotacion
        // RooTestDataBuilder y obtener los valores de sus atributos
        TestDataBuilderAnnotationValues annotationValues = new TestDataBuilderAnnotationValues(
                governorPhysicalTypeMetadata);
        // Si no tiene la anotacion o la clase a la que aplica, mejor salir
        if (!annotationValues.isAnnotationFound() || annotationValues.getClass() == null) {
            return null;
        }

        // Obtener info de la clase a la cual se le va a crear el TDB
        JavaType classToTdb = annotationValues.getClazz();
        Path path = Path.SRC_MAIN_JAVA;
        String physicalTypeIdentifier = PhysicalTypeIdentifier.createIdentifier(classToTdb, path);
        // Con el identificador ya creado, se puede obtener los metadatos
        PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(physicalTypeIdentifier);

        // Si no fue posible obtener informacion de la clase, mejor salir
        if (physicalTypeMetadata == null) {
            return null;
        }

        return new TestDataBuilderMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, physicalTypeMetadata);
    }

}
