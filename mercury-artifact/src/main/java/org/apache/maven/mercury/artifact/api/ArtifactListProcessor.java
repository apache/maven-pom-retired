package org.apache.maven.mercury.artifact.api;

import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;

/**
 * generic interface to be implemented by helper components
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface ArtifactListProcessor
{
  /** named functions - used to help processing in various parts of the system */
  public static final String [] FUNCTIONS = new String [] 
                         { 
                            "tp" // transaction processing function, future use
                         };
  public static final String FUNCTION_TP = FUNCTIONS[0];
  
  /** initialize it if required 
   * @throws ArtifactListProcessorException */
  public void init( Map<String, String> env )
  throws ArtifactListProcessorException;
  
  /** configure it if required */
  public void configure( Object config )
  throws ArtifactListProcessorException;
  
  /** actually do that 
   * @throws ArtifactListProcessorException */
  public List<ArtifactBasicMetadata> process( List<ArtifactBasicMetadata> artifacts )
  throws ArtifactListProcessorException;
}
