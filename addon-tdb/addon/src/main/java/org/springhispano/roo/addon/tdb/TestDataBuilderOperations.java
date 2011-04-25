package org.springhispano.roo.addon.tdb;

import org.springframework.roo.model.JavaType;

/**
 * Interface con los comandos del addon Test Data Builder.
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 */
public interface TestDataBuilderOperations {

	boolean isProjectAvailable();

	/**
	 * Crea el archivo Java del Test Data Builder de la clase especificada.
	 * @param clazz Clase de la cual se crea el TDB
	 */
    void createTestDataBuilder(JavaType clazz);

    /**
     * Agrega la dependencia a el jar de anotaciones de SpringHispano.org en caso de que no
     * exista.
     */
    void addRooTestDataBuilderDependencyIfNoPresent();
}