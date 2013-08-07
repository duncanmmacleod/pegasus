/**
 *  Copyright 2007-2008 University Of Southern California
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.isi.pegasus.planner.transfer.mapper.impl;

import edu.isi.pegasus.common.logging.LogManager;

import edu.isi.pegasus.planner.catalog.site.classes.Directory;
import edu.isi.pegasus.planner.catalog.site.classes.FileServer;
import edu.isi.pegasus.planner.catalog.site.classes.SiteCatalogEntry;
import edu.isi.pegasus.planner.catalog.site.classes.SiteStore;
import edu.isi.pegasus.planner.classes.ADag;
import edu.isi.pegasus.planner.classes.PegasusBag;
import edu.isi.pegasus.planner.classes.PlannerOptions;
import edu.isi.pegasus.planner.transfer.mapper.MapperException;
import edu.isi.pegasus.planner.transfer.mapper.OutputMapper;

import java.io.File;
import java.io.IOException;
import org.griphyn.vdl.euryale.FileFactory;

import org.griphyn.vdl.euryale.VirtualFlatFileFactory;

/**
 *
 * The abstract class that serves as the base class for the File Factory based mappers.
 * 
 * @author vahi
 */
public abstract class AbstractFileFactoryBasedMapper implements OutputMapper {

    

    /**
     * The handle to the logger.
     */
    protected LogManager mLogger;
    
    
    protected FileFactory mFactory;
    
    /**
     * Handle to the Site Catalog contents.
     */
    protected SiteStore mSiteStore;
    
    /**
     * The output site where the data needs to be placed.
     */
    protected String mOutputSite;
    
    /**
     * The stage out directory where the outputs are staged to.
     */
    protected Directory mStageoutDirectory;
    
    /**
     * The default constructor.
     */
    public AbstractFileFactoryBasedMapper(){
        
    }
    
    /**
     * Initializes the mappers.
     *
     * @param bag   the bag of objects that is useful for initialization.
     * @param workflow   the workflow refined so far.
     *
     */
    public void initialize( PegasusBag bag, ADag workflow)  throws MapperException{
        PlannerOptions options = bag.getPlannerOptions();
        String      outputSite = options.getOutputSite();
        mSiteStore    = bag.getHandleToSiteStore();
        mOutputSite   = outputSite;
        boolean stageOut = (( outputSite != null ) && ( outputSite.trim().length() > 0 ));

        if (!stageOut ){
            //no initialization and return
            mLogger.log( "No initialization of StageOut Site Directory Factory",
                         LogManager.DEBUG_MESSAGE_LEVEL );
            return;
        }
        
        mStageoutDirectory = this.lookupStorageDirectory(outputSite);
        
        mFactory = this.instantiateFileFactory(bag, workflow);
    }
    
    /**
     * Method that instantiates the FileFactory
     * 
     * @param bag   the bag of objects that is useful for initialization.
     * @param workflow   the workflow refined so far.
     * 
     * @return the handle to the File Factory to use 
     */
    public abstract FileFactory instantiateFileFactory( PegasusBag bag, ADag workflow );
    
    /**
     * Returns the short name for the implementation class.
     * 
     * @return 
     */
    public abstract String getShortName();
    
    /**
     * 
     * Returns a URL for the lfn on the output site. It randomly selects one of
     * the file servers to use for selection.
     * 
     * @param lfn          the lfn
     * @param site         the output site
     * @param operation    whether we want a GET or a PUT URL
     * @return 
     */
    public String getURL( String lfn , String site , FileServer.OPERATION operation )  throws MapperException{
        Directory directory = null;
        if( mOutputSite != null && mOutputSite.equals( site ) ){
            directory = this.mStageoutDirectory;
        }
        else{
            directory = this.lookupStorageDirectory(site);
        }
        
        FileServer server = directory.selectFileServer(operation);
        if( server == null ){
            this.complainForStorageFileServer( operation, site);
        }
        
        return this.getURL(lfn, server);
        
    }
    
     
    /**
     * Returns the full path on remote output site, where the lfn will reside, 
     * using the FileServer passed.
     *
     * @param lfn     the logical filename of the file.
     * @param server  the file server to use
     * 
     * @return the URL for the LFN
     */
    public String getURL( String lfn , FileServer server ) throws MapperException {
        StringBuilder url =  new StringBuilder( server.getURL() );
        try{
            //the factory will give us the relative
            //add on part
            String addOn = mFactory.createFile( lfn ).toString();
            //check if we need to add file separator
            //do we really need it?
            if( addOn.indexOf( File.separator ) != 0 ){
                url.append( File.separator );
            }
            url.append( addOn );
         }
         catch( IOException e ){
             throw new RuntimeException( "IOException " , e );
         }
         return url.toString();
        
    }

    /**
     * Looks up the site catalog to return the storage directory for a site
     * 
     * @param site  the site 
     * 
     * @return 
     */
    protected Directory lookupStorageDirectory( String site )throws MapperException {
        // create files in the directory, unless anything else is known.
        SiteCatalogEntry entry       = mSiteStore.lookup( site );
        if( entry == null ){
            throw new MapperException( getErrorMessagePrefix()  + "Unable to lookup site catalog for site " + site );
        }        

        Directory directory = entry.getHeadNodeStorageDirectory();
        if( directory == null ){
            throw new MapperException( getErrorMessagePrefix() + "No Storage directory specified for site " + site );
        }
        return directory;
    }
    
 
    /**
     * Complains for a missing head node storage file server on a site for a job
     *
     * @param operation the file server operation
     * @param site     the site
     */
    protected void complainForStorageFileServer( 
                                               FileServer.OPERATION operation,
                                               String site) {
        StringBuilder error = new StringBuilder();
        error.append( getErrorMessagePrefix() );
        
        error.append( " File Server not specified for shared-storage filesystem for site: ").
              append( site );
        throw new MapperException( error.toString() );

    }
    
    /**
     * Returns the prefix message to be attached to an error message
     * 
     * @return 
     */
    protected String getErrorMessagePrefix(){
        StringBuilder error = new StringBuilder();
        error.append( "[" ).append( this.getShortName() ).append( "] ");
        return error.toString();
    }
}
