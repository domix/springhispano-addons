package org.springhispano.roo.addon.tdb;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CommandMarker;

/**
 * Comandos del addon Test Data Builder.
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 */
@Component
@Service
public class TestDataBuilderCommands implements CommandMarker {
	
	private static Logger logger = Logger.getLogger(TestDataBuilderCommands.class.getName());

	@Reference private TestDataBuilderOperations operations;
	
	/**
	 * Verifica si el comando 'sh tdb' tiene todo lo necesario para poder correr.
	 * @return true si lo tiene, false de lo contrario
	 */
	@CliAvailabilityIndicator("sh tdb")
	public boolean isPropertyAvailable() {
		return true;
	}
	
	@CliCommand(value="sh tdb", help="Genera un Test Data Builder del pojo que se especifique")
	public String property() {
		return "En desarrollo!";
	}
}