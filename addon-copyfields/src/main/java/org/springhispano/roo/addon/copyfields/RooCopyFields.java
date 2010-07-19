package org.springhispano.roo.addon.copyfields;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotacion que lanza la generacion del metodo de copiar.
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RooCopyFields {
    
    /**
     * Nombre del metodo. 
     */
    String methodName() default "copyFields";
}

