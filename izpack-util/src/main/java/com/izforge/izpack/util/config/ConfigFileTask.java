/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.util.config;

import java.io.File;

public abstract class ConfigFileTask extends SingleConfigurableTask
{
    /*
     * Instance variables.
     */

    protected File fromFile;

    protected File toFile;

    protected File targetFile;

    protected boolean cleanup;


    /**
     * Use this to prepend a comment to the configuration file's header
     */
    private String comment;

    /**
     * Location of the configuration file to be written; required. May be an existing file to
     * be patched, or a new file (implying that configuration values will be specified e.g. by 
     * &lt;entry&gt; elements to ConfigurationInstallerListener).
     * 
     * @see #setTargetFile(File)
     */
    public void setToFile(File file)
    {
        this.toFile = file;
    }

    /**
     * Location of the configuration file to be patched from; optional. If not set, attributes
     * defining preservations of entries and values are ignored.
     */
    public void setFromFile(File file)
    {
        this.fromFile = file;
    }

    /**
     * Alternative location for the resulting configuration output; optional. Allows an 
     * original config file (defined by {@link #setToFile(File) setToFile}) to be preserved,
     * and the new, merged configuration written to a separate file.
     */
    public void setTargetFile(File file)
    {
        this.targetFile = file;
    }
    
    /**
     * Returns the location to which config should be written. Always {@code targetFile}, if
     * defined (i.e. old configuration file preserved; otherwise, {@code toFile} (i.e. old 
     * configuration file overwritten).
     * 
     * @returns the location to which config should be written
     */
    protected File getOutputFile(){
    	if(targetFile == null) 
    	{
    		return toFile;
    	}
    	else 
    	{ 
    		return targetFile;
    	}
    }
    
    /**
     * Whether to delete the toFile after the operation
     * @param cleanup True, if the toFile should be deleted after the operation
     */
    public void setCleanup(boolean cleanup)
    {
        this.cleanup = cleanup;
    }

    /**
     * optional header comment for the file
     */
    public void setComment(String hdr)
    {
        comment = hdr;
    }

    protected String getComment()
    {
        return this.comment;
    }

    @Override
    protected void validate() throws Exception
    {
        // No additional validation required.
    }

}
