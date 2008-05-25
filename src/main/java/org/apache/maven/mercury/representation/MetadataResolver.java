package org.apache.maven.mercury.representation;

/**
 * entry point into metadata resolution component
 * 
 * @author Jason van Zyl
 * @author Oleg Gusakov
 */
public interface MetadataResolver
{
    String ROLE = MetadataResolver.class.getName();

    /**
     * collect all dependency metadata into one "dirty" tree
     * 
     * @param request
     * @return
     * @throws MetadataResolutionException
     */
    MetadataResolutionResult resolveMetadata( MetadataResolutionRequest request )
        throws MetadataResolutionException;
}
