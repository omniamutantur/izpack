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
import java.util.Set;
import java.util.logging.Logger;

import com.izforge.izpack.util.config.base.MultiMap;
import com.izforge.izpack.util.config.base.OptionMap;
import com.izforge.izpack.util.config.base.Options;

public class SingleOptionFileTask extends MultiMapConfigFileTask<String, String>
{
    private static final Logger logger = Logger.getLogger(SingleOptionFileTask.class.getName());

    @Override
    protected MultiMap<String, String> readFromFile(File configFile) throws IOException
    {
    	MultiMap<String, String> config;
        if (configFile != null && configFile.exists())
        {
            logger.fine("Loading options file: " + configFile.getAbsolutePath());
            config = new Options(configFile);
        }
        else
        {
        	config = new Options();
        }
        return config;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ClassCastException if {@code config} cannot be cast to 
     * {@link Options} 
     */
    @Override
    protected void writeToFile(MultiMap<String, String> config, File destFile) throws IOException
    {
        Options opts = (Options) config;
        opts.setFile(destFile);
        opts.setComment(headerComment);
        opts.store();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ClassCastException if {@link #patchResolveVariables} is 
     * {@code true}, and {@code srcConfig} cannot be cast to {@link OptionMap}
     */
	@Override
	protected void patch(MultiMap<String, String> srcConfig, MultiMap<String, String> patchConfig)
	{
        Set<String> toKeySet;
        Set<String> fromKeySet;
        toKeySet = patchConfig.keySet();
        fromKeySet = srcConfig.keySet();
        for (String key : fromKeySet)
        {
            String fromValue = (patchResolveVariables ? ((OptionMap)srcConfig).fetch(key) : srcConfig.get(key));
            if (patchPreserveEntries && !toKeySet.contains(key))
            {
                logger.fine("Preserve key \"" + key + "\"");
                patchConfig.add(key, fromValue);
            }
            else if (patchPreserveValues && patchConfig.keySet().contains(key))
            {
                logger.fine("Preserve value for key \"" + key + "\": \"" + fromValue
                        + "\"");
                patchConfig.put(key, fromValue);
            }
        }
	}
}
