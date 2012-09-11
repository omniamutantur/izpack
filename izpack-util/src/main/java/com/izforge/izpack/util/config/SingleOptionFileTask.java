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

	/**
	 * {@inheritDoc}
	 * 
	 * <p>The {@code Entry.section} property is not applicable to property files.</p>
	 * 
	 * @throws ClassCastException if {@code config} is not a type of {@link OptionMap}
	 */
	@Override
	protected void deleteEntry(MultiMapConfigEntry entry, MultiMap<String, String> config) throws Exception {
        for (int i = 0; i < config.length(entry.getKey()); i++)
        {
            if (entry.getValue() == null)
            {
                String origValue = getValueFromOptionMap((OptionMap) config, entry.getKey(), i);

                if (origValue != null)
                {
                    switch (entry.getLookupType())
                    {
                        case REGEXP:
                            if (origValue.matches(entry.getValue()))
                            {
                                logger.fine("Remove option key \"" + entry.getKey() + "\"");
                                config.remove(entry.getKey(), i);
                                i--;
                            }
                            break;

                        default:
                            if (origValue.equals(entry.getValue()))
                            {
                                logger.fine("Remove option key \"" + entry.getKey() + "\"");
                                config.remove(entry.getKey(), i);
                                i--;
                            }
                            break;
                    }
                }
            }
            else
            {
                logger.fine("Remove option key \"" + entry.getKey() + "\"");
                config.remove(entry.getKey());
                i--;
            }
        }
	}

	@Override
	protected void keepEntry(MultiMapConfigEntry entry, MultiMap<String, String> src, MultiMap<String, String> target) throws Exception {
        for (int i = 0; i < src.length(entry.getKey()); i++)
        {
            String fromValue = getValueFromOptionMap((OptionMap) src, entry.getKey(), i);
            if (fromValue != null)
            {
                if (entry.getValue() != null)
                {
                    switch (entry.getLookupType())
                    {
                        case REGEXP:
                            if (fromValue.matches(entry.getValue()))
                            {
                                setOptions(entry, fromValue, (Options)target);
                            }
                            break;

                        default:
                            if (!fromValue.equals(entry.getValue()))
                            {
                                setOptions(entry, fromValue, (Options)target);
                            }
                            break;
                    }
                }
                else
                {
                    setOptions(entry, fromValue, (Options)target);
                }
            }
        }
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ClassCastException if config is not of type {@link Options}
	 */
	@Override
	protected void insertEntry(MultiMapConfigEntry entry, MultiMap<String, String> config) throws Exception 
	{
		setOptions(entry, null, (Options)config);
	}
	
	/**
	 * Sets the value of the specified {@code key} to the given {@code value}.
	 * Where {@code lookupValue} is given, only keys with matching values will
	 * be updated.
	 *  
	 * @param entry the data to set
	 * @param lookupValue the old value to update, or null to just insert a
	 * new entry
	 * @param config the Options configuration to change
	 */
    protected void setOptions(MultiMapConfigEntry entry, String lookupValue, Options config) throws Exception
    {
        int found = 0;

        for (int i = 0; i < config.length(entry.getKey()); i++)
        {
            if (lookupValue != null)
            {
                String origValue = getValueFromOptionMap(config, entry.getKey(), i);
                String newValue = entry.calculateValue(origValue);

                if (origValue != null)
                {
                    switch (entry.getLookupType())
                    {
                        case REGEXP:
                            if (origValue.matches(lookupValue))
                            {
                                // found in patch target and in patch using reqexp value lookup;
                                // overwrite in each case at the original position
                                logger.fine("Preserve option file entry \"" + entry.getKey() + "\"");
                                config.put(entry.getKey(), newValue, i);
                                found++;
                            }
                            break;

                        default:
                            if (origValue.equals(lookupValue))
                            {
                                // found in patch target and in patch using plain value lookup;
                                // overwrite in each case at the original position
                            	config.put(entry.getKey(), newValue, i);
                                found++;
                            }
                            break;
                    }
                }
            }
            else
            {
                // found in patch target and in patch;
                // not looked up by value - overwrite in each case at the original position
            	config.put(entry.getKey(), entry.getValue(), i);
                found++;
            }
        }

        logger.fine("Patched " + found + " option file entries for key \"" + entry.getKey() + "\" found in original: " + entry.getValue());

        if (found == 0)
        {
            // nothing existing to patch found in patch target
            // but force preserving of patch entry
            logger.fine("Add option file entry for \"" + entry.getKey() + "\": " + entry.getValue());
            config.add(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Resolves the value of the {@code key} in {@code map} with the specified
     * index. The value of {@code key} may contain ini4j variables if 
     * {@link #patchResolveVariables} is {@code true}.
     * 
     * @param map the map to search
     * @param key the key to resolve
     * @param index the index of the given key
     * @return the resolved value
     */
    protected String getValueFromOptionMap(OptionMap map, String key, int index)
    {
        return patchResolveVariables ?
                map.fetch(key, index)
                : map.get(key, index);
    }
}
