package org.springhispano.roo.addon.tdb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotacion que dispara la generacion del Test Data Builder.
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RooTestDataBuilder {

    /**
     * Clase de la cual se va a generar el TestDataBuilder
     */
    Class<?> clazz();
}
