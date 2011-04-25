package org.springhispano.roo.addon.tdb;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger; 
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
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;


/**
 * Implementacion de los comandos del addon Test Data Builder.
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 */
@Component
@Service
public class TestDataBuilderOperationsImpl implements TestDataBuilderOperations {

	private static final String BUILDER_POSTFIX = "Builder";

    private static Logger logger = Logger.getLogger(TestDataBuilderOperationsImpl.class.getName());

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
        // El paquete del TDB y la clase sera el mismo
        JavaPackage javaPackage = clazz.getPackage();
        
        String className = clazz.getSimpleTypeName();
               
        JavaType tdbJavaType = new JavaType(javaPackage.getFullyQualifiedPackageName() + 
                '.' + className + BUILDER_POSTFIX);
        
        // Crear un identificador PhysicalTypeMetadata del tipo del Test Data Builder
        String identifier = PhysicalTypeIdentifier.createIdentifier(tdbJavaType, Path.SRC_MAIN_JAVA);

        AnnotationMetadataBuilder amBuilder =
                new AnnotationMetadataBuilder(new JavaType("org.springhispano.roo.addon.tdb.RooTestDataBuilder"));
        amBuilder.addClassAttribute("clazz", clazz);

        ClassOrInterfaceTypeDetailsBuilder typeBuilder =
                new ClassOrInterfaceTypeDetailsBuilder(
                        identifier, Modifier.PUBLIC, tdbJavaType, PhysicalTypeCategory.CLASS);

        List<AnnotationMetadataBuilder> annotationBuilders = new ArrayList<AnnotationMetadataBuilder>();
        annotationBuilders.add(amBuilder);
        
        typeBuilder.setAnnotations(annotationBuilders);
        
        this.typeManagementService.generateClassFile(typeBuilder.build());
    }

    /*
     * (non-Javadoc)
     * @see org.springhispano.roo.addon.tdb.TestDataBuilderOperations#addRooTestDataBuilderDependencyIfNoPresent()
     */
    public void addRooTestDataBuilderDependencyIfNoPresent() {
        // NOTA: Que va a pasar cuando la version cambie?
        // NOTA: Esta operacion potencialmente puede llegar a ser usada por todos los addons
        // ¿como hacer para que esta sea una especie de utileria?
        
        // No es necesario verificar si existe, esta operacion no hace nada si ya esta en el pom
        this.projectOperations.addDependency(
                new Dependency("org.springhispano.roo.addon",
                        "org.springhispano.roo.addon.anotaciones", "0.1.0-SNAPSHOT"));        
    }
}