package org.springhispano.roo.addon.copyfields;

import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.model.JavaType;

/**
 * Asigna los valores de la anotacion RooCopyFields
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 *
 */
public class CopyFieldsAnnotationValues extends AbstractAnnotationValues {
    
    @AutoPopulate private String methodName = null;

    public CopyFieldsAnnotationValues(PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(governorPhysicalTypeMetadata, new JavaType(RooCopyFields.class.getName()));
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String getMethodName() {
        return this.methodName;
    }
}
