package org.springhispano.roo.addon.auditablefields;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotacion que dispara la generacion de los campos auditables.
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RooAuditableFields {
}

