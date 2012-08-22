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
import java.util.Enumeration;
import java.util.logging.Logger;

import com.izforge.izpack.util.file.FileCopyTask;

public abstract class ConfigurableFileCopyTask extends FileCopyTask implements ConfigurableTask
{
    private static final Logger logger = Logger.getLogger(ConfigurableFileCopyTask.class.getName());

    protected boolean patchPreserveEntries = true;

    protected boolean patchPreserveValues = true;

    protected boolean patchResolveVariables = false;

    protected boolean cleanup;


    /**
     * Whether to preserve equal entries but not necessarily their values from an old configuration,
     * if they can be found (default: true).
     *
     * @param preserveEntries - true to preserve equal entries from an old configuration
     */
    public void setPatchPreserveEntries(boolean preserveEntries)
    {
        this.patchPreserveEntries = preserveEntries;
    }

    /**
     * Whether to preserve the values of equal entries from an old configuration, if they can be
     * found (default: true). Set false to overwrite old configuration values by default with the
     * new ones, regardless whether they have been already set in an old configuration. Values from
     * an old configuration can only be preserved, if the appropriate entries exist in an old
     * configuration.
     *
     * @param preserveValues - true to preserve the values of equal entries from an old
     * configuration
     */
    public void setPatchPreserveValues(boolean preserveValues)
    {
        patchPreserveValues = preserveValues;
    }

    /**
     * Whether variables should be resolved during patching.
     *
     * @param resolve - true to resolve in-value variables
     */
    public void setPatchResolveExpressions(boolean resolve)
    {
        patchResolveVariables = resolve;
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
     * Do a patch operation.
     *
     * @param fromFile original file to patch from
     * @param toFile newer reference file to patch certain values or entries to
     * @param targetFile output file of the patched result
     */
    protected abstract void doFileOperation(File fromFile, File toFile, File targetFile)
            throws Exception;

    @Override
    protected void doFileOperations() throws Exception
    {
        if (fileCopyMap.size() > 0)
        {
            logger.fine("Merge/copy " + fileCopyMap.size() + " file"
                    + (fileCopyMap.size() == 1 ? "" : "s") + " in " + destDir.getAbsolutePath());

            Enumeration<String> e = fileCopyMap.keys();
            while (e.hasMoreElements())
            {
                String fromFile = e.nextElement();
                String[] toFiles = fileCopyMap.get(fromFile);

                for (String toFile : toFiles)
                {
                    if (fromFile.equals(toFile))
                    {
                        logger.warning("Skipping self-merge/copy of " + fromFile);
                        continue;
                    }

                    logger.fine("Merge/copy " + fromFile + " into " + toFile);

                    // The target file to copy to is the original (old) file to
                    // take preservations of old entries and values from.
                    // The source file to copy from is the new file which contains
                    // the reference entries and values which might be patched from
                    // the original ones.
                    // The temp file is a transitional file to store the initial merge
                    // before replacing the original file.
                    File to = new File(toFile);
                    File from = new File(fromFile);
                    /* TODO: change this so that a copy of target is maintained in temp
                     * for possible rollback, and just write the merge straight to
                     * the target file? */
                    File toTmp = File.createTempFile("tmp-", null, to.getParentFile());

                    try
                    {
                        doFileOperation(from, to, toTmp);

                        getFileUtils().copyFile(toTmp, to, forceOverwrite, preserveLastModified);
                        if (cleanup && from.exists())
                        {
                            if (!from.delete())
                            {
                                logger.warning("File " + from + " could not be cleaned up");
                            }
                        }
                    }
                    catch (IOException be)
                    {
                        String msg = "Failed to merge/copy " + fromFile + " into " + toFile
                                + " due to " + be.getMessage();
                        // TODO: should there be proper rollback here to restore the original state of the file? See 'todo' above.
                        if (failonerror)
                        {
                        	throw new Exception(msg, be);
                        }
                        else
                        {
                        	logger.warning(msg);
                        }
                    }
                    finally
                    {
                        toTmp.delete();
                    }
                }
            }
        }
    }

}
