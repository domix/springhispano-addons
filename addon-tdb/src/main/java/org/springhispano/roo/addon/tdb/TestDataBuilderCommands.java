package org.springhispano.roo.addon.tdb;

import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;


/**
 * Comandos del addon Test Data Builder.
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 */
@Component
@Service
public class TestDataBuilderCommands implements CommandMarker {
	
	private static final Logger LOGGER = Logger.getLogger(TestDataBuilderCommands.class.getName());

	@Reference
	private TestDataBuilderOperations operations;
	
	// NOTE: Esta inyeccion no deberia estar aqui
	@Reference
	private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;
	
	/**
	 * Verifica si el comando 'sh tdb' tiene todo lo necesario para poder correr.
	 * @return true si lo tiene, false de lo contrario
	 */
	@CliAvailabilityIndicator("sh tdb")
	public boolean isTestDataBuilderCommandAvailable() {
		return this.operations.isProjectAvailable();
	}
	
	/**
	 * Atiende la ejecucion del comando para generar el Test Data Builder.
	 * 
	 * TODO: Poder especificar la ruta donde se quiere generar el codigo.
	 * @param javaType Clase de la cual se va a generar el Test Data Builder
	 */
	@CliCommand(value="sh tdb", help="Genera un Test Data Builder del pojo que se especifique")
	public void createTestDataBuilder(
	        @CliOption(key = "class", mandatory = true, 
	            help = "Clase de la cual se va a generar el Test Data Builder")
	        JavaType javaType) {
	    // Verificar la existencia fisica de la clase a la cual se le va a crear el TDB
	    String id = this.physicalTypeMetadataProvider.findIdentifier(javaType);
        if (id == null) {
            throw new IllegalArgumentException(
                    "Cannot locate source for [" + javaType.getFullyQualifiedTypeName() + ']');
        }
        // Agregar la dependencia a los addons de SpringHispano al pom.xml
	    this.operations.addRooTestDataBuilderDependencyIfNoPresent();
	    // Crear el TDB
	    this.operations.createTestDataBuilder(javaType);	
	}
}