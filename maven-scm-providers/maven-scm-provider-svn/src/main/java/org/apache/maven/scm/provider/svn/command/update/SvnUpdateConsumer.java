package org.apache.maven.scm.provider.svn.command.update;

/*
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.List;
import java.util.ArrayList;
import java.io.File;

import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmFile;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SvnUpdateConsumer
    implements StreamConsumer
{
    private final static String UPDATED_TO_REVISION_TOKEN = "Updated to revision";

    private final static String AT_REVISION_TOKEN = "At revision";

    private Logger logger;

    private File workingDirectory;

    private List updatedFiles = new ArrayList();

    private int revision;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public SvnUpdateConsumer( Logger logger, File workingDirectory )
    {
        this.logger = logger;

        this.workingDirectory = workingDirectory;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    public void consumeLine( String line )
    {
        if ( line.length() <= 3 )
        {
            logger.warn( "Unexpected input, the line must be at least three characters long. Line: '" + line + "'." );

            return;
        }

        String statusString = line.substring( 0, 1 );

        String file = line.substring( 3 );

        ScmFileStatus status;

        if ( line.startsWith( UPDATED_TO_REVISION_TOKEN ) )
        {
            String revisionString = line.substring( UPDATED_TO_REVISION_TOKEN.length() + 1, line.length() - 1 );

            revision = parseInt( revisionString );

            return;
        }
        else if ( line.startsWith( AT_REVISION_TOKEN ) )
        {
            String revisionString = line.substring( AT_REVISION_TOKEN.length() + 1, line.length() - 1 );

            revision = parseInt( revisionString );

            return;
        }
        else if ( statusString.equals( "A" ) )
        {
            status = ScmFileStatus.ADDED;
        }
        else if ( statusString.equals( "U" ) )
        {
            status = ScmFileStatus.UPDATED;
        }
        else if ( statusString.equals( "D" ) )
        {
            status = ScmFileStatus.DELETED;
        }
        else
        {
            logger.info( "Unknown file status: '" + statusString + "'." );

            return;
        }

        // If the file isn't a file; don't add it.
        if ( !new File( workingDirectory, file ).isFile() )
        {
            return;
        }

        updatedFiles.add( new ScmFile( file, status ) );
    }

    public List getUpdatedFiles()
    {
        return updatedFiles;
    }

    public int getRevision()
    {
        return revision;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private int parseInt( String revisionString )
    {
        try
        {
            return Integer.parseInt( revisionString );
        }
        catch ( NumberFormatException ex )
        {
            return 0;
        }
    }
}