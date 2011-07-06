package org.springhispano.roo.addon.tdb;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.*;


/**
 * Implementacion de los comandos del addon Test Data Builder.
 *
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 */
@Component
@Service
public class TestDataBuilderOperationsImpl implements TestDataBuilderOperations {

    private static final String BUILDER_POSTFIX = "Builder";

    private static final String ORG_SPRINGHISPANO_ROO_ADDON_GROUP_ID = "org.springhispano.roo.addon";

    private static final String ORG_SPRINGHISPANO_ROO_ADDON_TDB_ARTIFACT_ID =
            "org.springhispano.roo.addon.tdb.annotations";

    private static final String TDB_ANNOTATIONS_VERSION = "1.0.0.RELEASE";

    /**
     * Provee de metodos para obtener los metadata
     */
    @Reference
    private MetadataService metadataService;

    /**
     * Provee de metodos con operaciones sobre el pom.xml del proyecto
     */
    @Reference
    private ProjectOperations projectOperations;

    /**
     * Provee de metodos para generar archivos Java
     */
    @Reference
    private TypeManagementService typeManagementService;
    private ComponentContext context;

    protected void activate(ComponentContext context) {
        this.context = context;
    }

    public boolean isProjectAvailable() {
        return getPathResolver() != null;
    }

    /**
     * @return the path resolver or null if there is no user project
     */
    private PathResolver getPathResolver() {
        ProjectMetadata projectMetadata = (ProjectMetadata) metadataService.get(ProjectMetadata.getProjectIdentifier());
        if (projectMetadata == null) {
            return null;
        }
        return projectMetadata.getPathResolver();
    }

    /*
      * (non-Javadoc)
      * @see org.springhispano.roo.addon.tdb.TestDataBuilderOperations#createTestDataBuilder(org.springframework.roo.model.JavaType)
      */
    public void createTestDataBuilder(JavaType clazz) {
        JavaType tdbJavaType = createTestDataBuilderJavaTypeInSamePackageOfClass(clazz);
        String identifier = PhysicalTypeIdentifier.createIdentifier(tdbJavaType, Path.SRC_MAIN_JAVA);

        ClassOrInterfaceTypeDetailsBuilder typeBuilder =
                new ClassOrInterfaceTypeDetailsBuilder(
                        identifier, Modifier.PUBLIC, tdbJavaType, PhysicalTypeCategory.CLASS);

        AnnotationMetadataBuilder amBuilder =
                new AnnotationMetadataBuilder(new JavaType("org.springhispano.roo.addon.tdb.RooTestDataBuilder"));
        amBuilder.addClassAttribute("clazz", clazz);

        List<AnnotationMetadataBuilder> annotationBuilders = new ArrayList<AnnotationMetadataBuilder>();
        annotationBuilders.add(amBuilder);

        typeBuilder.setAnnotations(annotationBuilders);

        this.typeManagementService.generateClassFile(typeBuilder.build());
    }

    private JavaType createTestDataBuilderJavaTypeInSamePackageOfClass(JavaType clazz) {
        JavaPackage javaPackage = clazz.getPackage();
        String className = clazz.getSimpleTypeName();
        JavaType tdbJavaType = new JavaType(javaPackage.getFullyQualifiedPackageName() +
                '.' + className + BUILDER_POSTFIX);
        return tdbJavaType;
    }

    public void addRooTestDataBuilderDependencyIfNoPresent() {

        this.projectOperations.addRepository(new Repository(
                "org.springhispano.roo.addon",
                "SpringHispano.org - Spring Roo Addon Repository",
                "http://repo.springhispano-addons.googlecode.com/hg/repo/releases"));

        // No es necesario verificar si existe, esta operacion no hace nada si ya esta en el pom
        this.projectOperations.addDependency(
                new Dependency(ORG_SPRINGHISPANO_ROO_ADDON_GROUP_ID,
                        ORG_SPRINGHISPANO_ROO_ADDON_TDB_ARTIFACT_ID, TDB_ANNOTATIONS_VERSION));
    }
}