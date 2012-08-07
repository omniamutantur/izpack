/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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
package com.izforge.izpack.panels.test;

import java.util.Properties;

import org.mockito.Mockito;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.injectors.ProviderAdapter;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.PlatformProvider;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.container.provider.RulesProvider;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Platforms;

/**
 * Container for testing panels.
 *
 * @author Tim Anderson
 */
public abstract class AbstractTestPanelContainer extends AbstractContainer
{

    /**
     * Returns the underlying container.
     *
     * @return the underlying container
     */
    @Override
    public MutablePicoContainer getContainer()
    {
        return super.getContainer();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     * @throws PicoException      for any PicoContainer error
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        Properties properties = System.getProperties();
        addComponent(properties, properties);
        addComponent(Variables.class, DefaultVariables.class);
        addComponent(ResourceManager.class);
        addComponent(UninstallData.class);
        addComponent(ConditionContainer.class);
        addComponent(UninstallDataWriter.class, Mockito.mock(UninstallDataWriter.class));
        addComponent(AutomatedInstaller.class);

        container.addComponent(new DefaultObjectFactory(this));
        addComponent(IUnpacker.class, Mockito.mock(IUnpacker.class));
        addComponent(Housekeeper.class, Mockito.mock(Housekeeper.class));
        addComponent(Platforms.class);
        addComponent(Container.class, this);

        container.addAdapter(new ProviderAdapter(new RulesProvider()));
        container.addAdapter(new ProviderAdapter(new PlatformProvider()));
    }
}
