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
import java.io.IOException;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.util.config.base.Options;

public class SingleOptionFileTask extends ConfigFileTask
{
    private static final Logger logger = Logger.getLogger(SingleOptionFileTask.class.getName());

    @Override
    protected void readSourceConfigurable() throws Exception
    {
        if (fromFile != null)
        {
            try
            {
                if (!fromFile.exists())
                {
                    logger.warning("Options file " + fromFile.getAbsolutePath()
                            + " to patch from could not be found, no patches will be applied");
                    return;
                }
                logger.fine("Loading options file: " + fromFile.getAbsolutePath());
                // Configuration file type must be the same as the target type
                fromConfigurable = new Options(this.fromFile);
            }
            catch (IOException ioe)
            {
                throw new Exception(ioe.toString());
            }
        }
    }

    @Override
    protected void readConfigurable() throws Exception
    {
        if (toFile.exists())
        {
            try
            {
                logger.fine("Loading original configuration file: " + toFile.getAbsolutePath());
                configurable = new Options(toFile);
            }
            catch (IOException ioe)
            {
                throw new Exception("Error opening original configuration file: " + ioe.toString());
            }
        }
        else
        {
            configurable = new Options();
        }
    }

    @Override
    protected void writeConfigurable() throws Exception
    {
    	File targetFile = getOutputFile();
        try
        {
	        if (targetFile.exists())
	        {
	        	if (!overwrite)
	        	{
	        		logger.warning("Options file " + targetFile.getAbsolutePath()
	                        + " exists and is not allowed to be overwritten");
	                return;
	        	}
	        }
	        else
            {
                if (createConfigurable)
                {
                    File parent = targetFile.getParentFile();
                    if (parent != null && !parent.exists())
                    {
                        if (!parent.mkdirs())
                        {
                        	throw new IOException("Failed to create options file target location " + parent.getPath());
                        }
                    }
                    logger.fine("Creating empty options file: " + targetFile.getAbsolutePath());
                    targetFile.createNewFile();
                }
                else
                {
                    logger.warning("Options file " + targetFile.getAbsolutePath()
                            + " did not exist and is not allowed to be created");
                    return;
                }
            }
            Options opts = (Options) configurable;
            opts.setFile(targetFile);
            opts.setComment(getComment());
            opts.store();
        }
        catch (IOException ioe)
        {
            throw new Exception(ioe);
        }

        if (cleanup && fromFile.exists())
        {
            if (!fromFile.delete())
            {
                logger.warning("File " + fromFile + " could not be cleant up");
            }
        }
    }

    @Override
    protected Entry filterEntryFromXML(IXMLElement parent, Entry entry)
    {
        entry.setKey(parent.getAttribute("key"));
        entry.setValue(parent.getAttribute("value"));
        return entry;
    }
}
