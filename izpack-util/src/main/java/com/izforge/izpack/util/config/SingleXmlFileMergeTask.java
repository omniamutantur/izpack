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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.util.file.DirectoryScanner;
import com.izforge.izpack.util.file.types.FileSet;
import com.izforge.izpack.util.xmlmerge.AbstractXmlMergeException;
import com.izforge.izpack.util.xmlmerge.ConfigurationException;
import com.izforge.izpack.util.xmlmerge.XmlMerge;
import com.izforge.izpack.util.xmlmerge.config.ConfigurableXmlMerge;
import com.izforge.izpack.util.xmlmerge.config.PropertyXPathConfigurer;

public class SingleXmlFileMergeTask implements ConfigurableTask
{
    private static final Logger logger = Logger.getLogger(SingleXmlFileMergeTask.class.getName());

    protected File fromFile;
    protected File toFile;
    protected File targetFile;
    protected File conffile;
    protected boolean cleanup;

    protected Properties confProps = new Properties();


    public void setFromFile(File fromFile)
    {
        this.fromFile = fromFile;
    }


    public void setToFile(File toFile)
    {
        this.toFile = toFile;
    }


    public void setTargetFile(File targetFile)
    {
        this.targetFile = targetFile;
    }
    
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

    public void setConfigFile(File confFile)
    {
        this.conffile = confFile;
    }

    /**
     * Whether to delete the patchfiles after the operation
     * @param cleanup True, if the patchfiles should be deleted after the operation
     */
    public void setCleanup(boolean cleanup)
    {
        this.cleanup = cleanup;
    }

    /**
    * List of file sets.
    */
    List<FileSet> filesets = new ArrayList<FileSet>();

    /**
    * Adds a file set.
    * @param fileset The file set to add
    */
    public void addFileSet(FileSet fileset) {
        filesets.add(fileset);
    }

    /**
     * Adds a XML merge configuration property (XPath)
     * @param key The property key
     * @param value The property value
     */
    public void addProperty(String key, String value) {
        confProps.setProperty(key, value);
    }

    /**
    * Validates the configuration and destination files and the file sets.
    */
    public void validate() throws Exception {
        if (filesets.isEmpty() && fromFile == null) 
        {
            throw new Exception("No XML merge patch files given at all");
        }
        if (!confProps.isEmpty() && conffile != null) 
        {
            throw new Exception("Using both XML merge configuration file and explicit merge properties not allowed");
        }
    }

    @Override
    public void execute() throws Exception {
        validate();

        // Get the files to merge
        LinkedList<File> filesToMerge = new LinkedList<File>();

        if (fromFile != null)
        {
            if (fromFile.exists())
            {
                filesToMerge.add(fromFile);
            }
            else
            {
                logger.warning("XML merge skipped, target file "+fromFile+" not found");
                return;
            }
        }
        else
        {
            logger.warning("XML merge skipped, target file not defined");
            return;
        }

        if (toFile != null && toFile.exists())
            filesToMerge.add(toFile);

        for (FileSet fs : filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner();
            String[] includedFiles = ds.getIncludedFiles();
            for (String includedFile : includedFiles)
            {
                filesToMerge.add(new File(ds.getBasedir(), includedFile));
            }
        }

        if (filesToMerge.size() < 2)
        {
            logger.warning("XML merge skipped, not enough XML input files to merge");
            return;
        }

        if (conffile != null) {
            InputStream configIn = null;
            try {
                configIn = new FileInputStream(conffile);
                confProps.load(configIn);
            } catch (IOException e) {
                throw new Exception(e);
            } finally {
                if (configIn != null) {
                    try {
                        configIn.close();
                    } catch (IOException e) {
                        logger.log(Level.WARNING,
                                "Error closing file '" + conffile + "': " + e.getMessage(),
                                e);
                    }
                }
            }
        }

        // Create the XmlMerge instance and execute the merge
        XmlMerge xmlMerge;
        try {
            xmlMerge = new ConfigurableXmlMerge(new PropertyXPathConfigurer(confProps));
        } catch (ConfigurationException e) {
            throw new Exception(e);
        }

        File targetFile = getOutputFile();
        try {
            xmlMerge.merge(filesToMerge.toArray(new File[filesToMerge.size()]), targetFile);
        } catch (AbstractXmlMergeException e) {
            throw new Exception(e);
        }

        if (cleanup)
        {
            for (File file : filesToMerge)
            {
                if (file.exists() && !file.equals(targetFile))
                {
                    if (!file.delete())
                    {
                        logger.warning("File " + file + " could not be cleaned up");
                    }
                }
            }
        }
    }
}
