/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
 * Copyright 2013 Daniel Abson
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

package com.izforge.izpack.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.event.ConfigurationInstallerListener;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.config.ConfigFileTask;
import com.izforge.izpack.util.config.PropertyFileConfigTask;
import com.izforge.izpack.util.config.base.MultiMap;


/**
 * Tests the {@link ConfigurationInstallerListener} and {@link ConfigFileTask} framework.
 *
 * @author Tim Anderson
 * @author Daniel Abson
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class ConfigurationInstallerListenerTest extends AbstractInstallationTest
{

    /**
     * The listeners.
     */
    private final InstallerListeners listeners;

    /**
     * The installer frame.
     */
    private final InstallerFrame frame;

    /**
     * The installer controller.
     */
    private final InstallerController controller;

    /**
     * Frame fixture.
     */
    private FrameFixture frameFixture;


    /**
     * Constructs a {@code ConfigurationInstallerListenerTest}.
     *
     * @param listeners   the installer listeners
     * @param installData the install data
     * @param frame       the installer frame
     * @param controller  the installer controller
     */
    public ConfigurationInstallerListenerTest(InstallerListeners listeners, AutomatedInstallData installData, InstallerFrame frame,
                                 InstallerController controller)
    {
        super(installData);
        this.listeners = listeners;
        this.frame = frame;
        this.controller = controller;
    }

    /**
     * Tears down the test case.
     */
    @After
    public void tearDown()
    {
        if (frameFixture != null)
        {
            frameFixture.cleanUp();
        }
    }

    /**
     * Verifies that {@link ConfigurationInstallerListener} patches files as expected.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/event/configinstaller.xml")
    public void testConfigurationInstallerListener() throws Exception
    {
        frameFixture = HelperTestMethod.prepareFrameFixture(frame, controller);
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        frameFixture.requireVisible();

        HelperTestMethod.waitAndCheckInstallation(getInstallData());

        assertEquals(1, listeners.size());
        String installPath = getInstallData().getInstallPath();
        TestPropertyFileConfigTask task = new TestPropertyFileConfigTask();
        MultiMap<String, String> patchConfig1 = task.readFromFile(new File(installPath + "/configPatch/test1.properties"));
        verifyPropertyValue(patchConfig1, "1property1", "1value1");
        verifyPropertyIsNull(patchConfig1, "1propertyX");
        verifyPropertyValue(patchConfig1, "1propertyZ", "1valueZ");
        MultiMap<String, String> patchConfig2 = task.readFromFile(new File(installPath + "/configPatch/test2.properties"));
        verifyPropertyValue(patchConfig2, "2property1", "2value1");
        verifyPropertyIsNull(patchConfig2, "2propertyY");
        verifyPropertyValue(patchConfig2, "2propertyZ", "2valueZ");        
        MultiMap<String, String> patchConfig3 = task.readFromFile(new File(installPath + "/configPatch/test3.properties"));
        verifyPropertyValue(patchConfig3, "3property1", "3value1a");
        verifyPropertyValue(patchConfig3, "3property2", "3value2");
        verifyPropertyIsNull(patchConfig3, "3propertyY");
        verifyPropertyValue(patchConfig3, "3propertyZ", "3valueZ");      
    }
    
    private void verifyPropertyValue(MultiMap<String, String> config, String name, String value)
    {
        assertNotNull("Property " + name + " should exist!", config.get(name));
        assertEquals(value, config.get(name));
    }
    
    private void verifyPropertyIsNull(MultiMap<String, String> config, String name)
    {
        assertNull("Property " + name + " should not exist!", config.get(name));
    }
    
    /**
     * Stub class to make {@link PropertyFileConfigTask#readFromFile} visible to this test. 
     */    
    private class TestPropertyFileConfigTask extends PropertyFileConfigTask
    {
        public MultiMap<String, String> readFromFile(File configFile) throws IOException 
        {
            return super.readFromFile(configFile);
        }
    }

}
