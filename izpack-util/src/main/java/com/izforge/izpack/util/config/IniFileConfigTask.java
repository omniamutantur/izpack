/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2012 Rene Krell
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

import com.izforge.izpack.util.config.base.Ini;
import com.izforge.izpack.util.config.base.MultiMap;
import com.izforge.izpack.util.config.base.Profile;
import com.izforge.izpack.util.config.base.Profile.Section;

public class IniFileConfigTask extends MultiMapConfigFileTask<String, Profile.Section> 
{
    private static final Logger logger = Logger.getLogger(IniFileConfigTask.class.getName());
    

	@Override
	protected MultiMap<String, Section> readFromFile(File configFile) throws IOException
	{
    	MultiMap<String, Section> config;
        if (configFile != null && configFile.exists())
        {
            logger.fine("Loading INI file: " + configFile.getAbsolutePath());
            config = new Ini(configFile);
        }
        else
        {
        	config = new Ini();
        }
        return config;
    }

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ClassCastException if {@code config} cannot be cast to
	 * {@link Ini}
	 */
	@Override
	protected void writeToFile(MultiMap<String, Section> config, File destFile) throws IOException
	{
        Ini ini = (Ini) config;
        ini.setFile(destFile);
        ini.setComment(headerComment);
        ini.store();		
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ClassCastException if {@code patchConfig} cannot be cast to
	 * {@link Profile}
	 */
	@Override
	protected void patch(MultiMap<String, Section> srcConfig, MultiMap<String, Section> patchConfig)
	{
        Set<String> toKeySet;
        Set<String> fromKeySet;
        Set<String> sectionKeySet = patchConfig.keySet();
        Set<String> fromSectionKeySet = srcConfig.keySet();
        for (String fromSectionKey : fromSectionKeySet)
        {
            if (sectionKeySet.contains(fromSectionKey))
            {
                Ini.Section fromSection = srcConfig.get(fromSectionKey);
                Ini.Section toSection = patchConfig.get(fromSectionKey);
                fromKeySet = fromSection.keySet();
                toKeySet = null;
                if (toSection != null) toKeySet = toSection.keySet();
                for (String fromKey : fromKeySet)
                {
                    if (toSection == null)
                    {
                        logger.fine("Adding new section [" + fromSectionKey + "]");
                        toSection = ((Profile)patchConfig).add(fromSectionKey);
                    }
                    String fromValue = (patchResolveVariables ? fromSection.fetch(fromKey) : fromSection.get(fromKey));
                    if (patchPreserveEntries && !toKeySet.contains(fromKey))
                    {
                        logger.fine("Preserve key \"" + fromKey
                                + "\" in section [" + fromSectionKey + "]: " + fromValue);
                        toSection.add(fromKey, fromValue);
                    }
                    else if (patchPreserveValues && toKeySet.contains(fromKey))
                    {
                        logger.fine("Preserve value for key \"" + fromKey
                                + "\" in section [" + fromSectionKey + "]: " + fromValue);
                        toSection.put(fromKey, fromValue);
                    }
                }
            }
        }		
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The {@code lookupValue} and {@loookupType} arguments are not applicable.
	 * 
	 * @throws ClassCastException if {@config} is not a type of {@link Profile}
	 * 
	 */
	@Override
	protected void deleteEntry(MultiMapConfigEntry entry, MultiMap<String, Section> config) throws Exception {
		((Profile)config).remove(entry.getSection(), entry.getKey());
	}

	/**
	 * {@inheritDoc}
	 * 
	 *  @throws	ClassCastException if either {@code src} or {@code target} is
	 *  not of type {@link Profile}
	 */
	@Override
	protected void keepEntry(MultiMapConfigEntry entry, MultiMap<String, Section> src, MultiMap<String, Section> target) throws Exception {
        Profile.Section fromSection = ((Profile) src).get(entry.getSection());
        Profile.Section toSection = ((Profile) target).get(entry.getSection());
        if (fromSection != null)
        {
            if (toSection == null)
            {
                logger.fine("Adding new section [" + entry.getSection() + "]");
                toSection = ((Profile) target).add(entry.getSection());
            }
            if (toSection != null)
            {
                String fromValue = (patchResolveVariables ? fromSection
                        .fetch(entry.getKey()) : fromSection.get(entry.getKey()));
                if (!toSection.containsKey(entry.getKey()))
                {
                    logger.fine("Preserve file entry \"" + entry.getKey()
                            + "\" in section [" + entry.getSection() + "]: " + fromValue);
                    toSection.add(entry.getKey(), fromValue);
                }
                else
                {
                    logger.fine("Preserve file entry value for key \"" + entry.getKey()
                            + "\" in section [" + entry.getSection() + "]: " + fromValue);
                    toSection.put(entry.getKey(), fromValue);
                }
            }
        }
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ClassCastException if {@code config} is not a type of 
	 * {@link Profile}
	 */
	@Override
	protected void insertEntry(MultiMapConfigEntry entry, MultiMap<String, Section> config)
			throws Exception {
        String oldValue = getValueFromProfile((Profile)config, entry.getSection(), entry.getKey());
		((Profile)config).put(entry.getSection(), entry.getKey(), entry.calculateValue(oldValue));
	}
	
	
    /**
     * Resolves the value of the {@code key} in the specified {@code section}
     * in {@code profile}. The value of {@code key} may contain ini4j 
     * variables if {@link #patchResolveVariables} is {@code true}.
     * 
     * @param profile the profile to search
     * @param section the location of the given key
     * @param key the key to resolve
     * @return the resolved value
     */
    protected String getValueFromProfile(Profile profile, String section, String key)
    {
        return patchResolveVariables ? 
        		profile.fetch(section, key) 
        		: profile.get(section, key);
    }
}
