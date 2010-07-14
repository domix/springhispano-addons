package org.springhispano.roo.addon.tdb;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;

/**
 * Implementacion de los comandos del addon Test Data Builder.
 * 
 * @author Rafael Antonio Guti&eacute;rrez Turullols
 */
@Component
@Service
public class TestDataBuilderOperationsImpl implements TestDataBuilderOperations {

	private static Logger logger = Logger.getLogger(TestDataBuilderOperationsImpl.class.getName());

	@Reference private FileManager fileManager;
	@Reference private MetadataService metadataService;

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
}