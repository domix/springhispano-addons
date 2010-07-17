package org.springhispano.roo.addon.tdb;

import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.model.JavaType;

/**
 * Clase a la cual se le asignan los valores de la anotacion RooTestDataBuilder.
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols.
 *
 */
public class TestDataBuilderAnnotationValues extends AbstractAnnotationValues {
    
    @AutoPopulate JavaType clazz = null;

    public TestDataBuilderAnnotationValues(PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(governorPhysicalTypeMetadata, new JavaType(RooTestDataBuilder.class.getName()));
        AutoPopulationUtils.populate(this, annotationMetadata); 
    }

    public JavaType getClazz() {
        return this.clazz;
    }
}
