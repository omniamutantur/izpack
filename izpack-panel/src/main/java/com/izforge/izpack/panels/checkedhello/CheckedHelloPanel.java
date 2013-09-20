/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2005 Klaus Bartz
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.panels.checkedhello;

import static com.izforge.izpack.util.helper.SpecHelper.getXSBoolean;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.hello.HelloPanel;
import com.izforge.izpack.util.ProcessHelper;

/**
 * An extended hello panel class which detects whether the product was already installed or not.
 * This class should be only used if the RegistryInstallerListener will be also used. Currently the
 * check will be only performed on Windows operating system. This class can be used also as example
 * how to use the registry stuff to get informations from the current system.
 *
 * @author Klaus Bartz
 */
public class CheckedHelloPanel extends HelloPanel
{

    /**
     * Required (serializable)
     */
    private static final long serialVersionUID = 1737042770727953387L;
    private static final int STDERR_BUFF_SIZE = 8192;

    /**
     * Flag to break installation or not.
     */
    protected boolean abortInstallation;
    /**
     * Path to existing installation, if present.
     */
    protected String installationPath;
    /**
     * Command to run uninstaller for existing installation, if present.
     */
    protected String uninstallCommand;
    
    /**
     * Flag to kick off the uninstaller if an entity already exists.
     */
    protected boolean runUninstaller = false;
    /**
     * Flag to use the install path of an existing entity.
     */
    protected boolean replaceInstallPath = true;

    /**
     * The registry helper.
     */
    private transient final RegistryHelper registryHelper;

    /**
     * The logger.
     */
    private static Logger logger = Logger.getLogger(CheckedHelloPanel.class.getName());
    
    /**
     * Config parameter to kick off the uninstaller for an existing instance.
     */
    public static final String EXISTING_UNINSTALL = "uninstallexisting";    
    /**
     * Config parameter to use the install path of an existing installation.
     */
    public static final String EXISTING_REPLACEPATH = "keepexistinginstallpath";

    
    /**
     * The constructor.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent frame
     * @param installData the installation data
     * @param resources   the resources
     * @param handler     the registry handler instance
     * @param log         the log
     * @throws Exception if it cannot be determined if the application is registered
     */
    public CheckedHelloPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                             RegistryDefaultHandler handler, Log log) throws Exception
    {
        super(panel, parent, installData, resources, log);
        registryHelper = new RegistryHelper(handler, installData);
        abortInstallation = isRegistered();
        runUninstaller = getXSBoolean(panel.getConfiguration(EXISTING_UNINSTALL));
        replaceInstallPath = getXSBoolean(panel.getConfiguration(EXISTING_REPLACEPATH));
    }

    /**
     * <p>Presents the install path of the first already installed product and asks the user whether 
     * to install twice or not.</p>
     * 
     * <p><b>This method should only be called if this product was already installed.</b></p>
     *
     * @return whether a multiple Install should be performed or not.
     * @throws NativeLibException for any native library error
     */
    protected boolean multipleInstall() throws NativeLibException
    {
        String noLuck = getString("CheckedHelloPanel.productAlreadyExist0") + 
                (installationPath == null ? getString("CheckedHelloPanel.productPathNotFound") : installationPath) + " . " +
                getString("CheckedHelloPanel.productAlreadyExist1");
        return (askQuestion(getString("installer.error"), noLuck,
                            AbstractUIHandler.CHOICES_YES_NO) == AbstractUIHandler.ANSWER_YES);
    }
    
    /**
     * <p>Determines if an existing uninstaller should be run before proceeding. Checks panel
     * configuration, and obtains user confirmation if necessary.</p> 
     * 
     * </p><b>This method should only be called if this product was already installed.</b></p>
     * 
     * @return whether the uninstaller for the existing installation should be run or not
     * @throws NativeLibException for any native library error
     */
    protected boolean shouldRunUninstaller() throws NativeLibException
    {
        if ( runUninstaller )
        {
            String uninstallFirst = getString("CheckedHelloPanel.productAlreadyExist0") + 
                    (installationPath == null ? getString("CheckedHelloPanel.productPathNotFound") : installationPath) + " . " +
                    getString("CheckedHelloPanel.removePreviousVersion");
            return (askQuestion(getString("installer.error"), uninstallFirst,
                    AbstractUIHandler.CHOICES_YES_NO) == AbstractUIHandler.ANSWER_YES);
        }
        else
        {
            return false;
        }
    }

    /**
     * <p>Returns whether the handled application is already registered or not. The validation will be
     * made only on systems which contains a registry (Windows). Where the application is already
     * registered, attempts to set the following fields from the value set in the registry:
     * <ul><li>{@link installationPath}</li>
     * <li>{@link uninstallCommand}</li></ul></p>
     * 
     * <p>This method is invoked by {@link #CheckedHelloPanel(Panel, InstallerFrame, GUIInstallData, Resources, RegistryDefaultHandler, Log) the constructor}.</p>
     *
     * @return <tt>true</tt> if the application is registered
     * @throws Exception if it cannot be determined if the application is registered
     */
    protected boolean isRegistered() throws Exception
    {
        if ( registryHelper.isRegistered() )
        {
            installationPath = registryHelper.getInstallationPath();
            uninstallCommand = registryHelper.getUninstallCommand();
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Indicates whether the panel has been validated or not.
     *
     * @return true if the internal abort flag is not set, else false
     */
    public boolean isValidated()
    {
        return (!abortInstallation);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.installer.IzPanel#panelActivate()
     */

    public void panelActivate()
    {
        if (abortInstallation) //application is already installed?
        {
            parent.lockNextButton();

            if ( replaceInstallPath && installationPath != null) //re-use existing installation path? 
            {
                installData.getVariables().set("INSTALL_PATH", installationPath);
            }

            try
            {                
                if ( shouldRunUninstaller() )
                {        
                    if ( executeUninstaller() )
                    {
                        abortInstallation = false;
                        parent.unlockNextButton();                        
                    }
                    else
                    {
                        emitError("installer.error", getString("uninstaller.fail") + " " + getString("CheckedHelloPanel.manualUninstall"));
                    }
                }
                else if (multipleInstall())
                {
                    setUniqueUninstallKey();
                    abortInstallation = false;
                    parent.unlockNextButton();
                }
                else
                {
                    installData.getInfo().setUninstallerPath(null);
                    installData.getInfo().setUninstallerName(null);
                    installData.getInfo().setUninstallerCondition("uninstaller.nowrite");
                }
            }
            catch (Exception exception)
            {
                logger.log(Level.WARNING, exception.getMessage(), exception);
            }
        }
        installData.setVariable("UNINSTALL_NAME", registryHelper.getUninstallName());
    }
    
    protected boolean executeUninstaller()
    {
        int status = -1;
        Process uninstall = null;
        try
        {
            uninstall = ProcessHelper.exec(uninstallCommand);
            status = uninstall.waitFor();
        }
        catch (Exception e)
        {
            logger.severe("Error running uninstaller: " + e.getMessage());
        }
        
        if (status == 0) //uninstaller completed successfully
        {
            return true;
        }
        else if (status > 0) //uninstaller exited with error - try to log reason for failure from stderr
        {
            
            Reader in = new InputStreamReader(uninstall.getErrorStream());
            String error = "UNKNOWN RUNTIME ERROR";
            try 
            {
                char[] buff = new char[STDERR_BUFF_SIZE];
                int n = in.read(buff);
                error = new String(buff, 0, n).trim();                
            }
            catch (IOException ioe)
            {
                logger.warning("Failed to read from stderr input stream");
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch (IOException ioe)
                {
                    logger.fine("Failed to close stderr input stream");
                }
            }
            logger.severe(getString("uninstaller.fail") + "\n[" + error + "]");
        }
        return false; //uninstaller failed to run or exited with error
    }

    /**
     * Generates an unique uninstall key, displaying it to the user.
     *
     * @throws NativeLibException for any native library error
     */
    private void setUniqueUninstallKey() throws NativeLibException
    {
        String newUninstallName = registryHelper.updateUninstallName();
        emitNotification(getString("CheckedHelloPanel.infoOverUninstallKey")
                                 + newUninstallName);
    }
}
