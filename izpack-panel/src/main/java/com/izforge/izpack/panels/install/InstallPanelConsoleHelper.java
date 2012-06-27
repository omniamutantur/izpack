/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
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

package com.izforge.izpack.panels.install;

import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.installer.console.PanelConsole;
import com.izforge.izpack.installer.console.PanelConsoleHelper;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.util.Console;

/**
 * Install Panel console helper
 *
 * @author Mounir el hajj
 */
public class InstallPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole, ProgressListener
{
    /**
     * The unpacker.
     */
    private final IUnpacker unpacker;

    private int noOfPacks = 0;

    public InstallPanelConsoleHelper(IUnpacker unpacker)
    {
        this.unpacker = unpacker;
    }

    public boolean runConsoleFromProperties(InstallData installData, Properties properties)
    {
        return runConsole(installData);
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    @Override
    public boolean runConsole(InstallData installData, Console console)
    {
        unpacker.setProgressListener(this);
        unpacker.run();
        return unpacker.getResult();
    }

    @Override
    public void startAction(String name, int no_of_steps)
    {
        System.out.println("[ Starting to unpack ]");
        this.noOfPacks = no_of_steps;
    }

    @Override
    public void stopAction()
    {
        System.out.println("[ Unpacking finished ]");
    }

    @Override
    public void progress(int val, String msg)
    {

    }

    @Override
    public void nextStep(String packName, int stepno, int stepsize)
    {
        System.out.print("[ Processing package: " + packName + " (");
        System.out.print(stepno);
        System.out.print('/');
        System.out.print(this.noOfPacks);
        System.out.println(") ]");
    }

    @Override
    public void setSubStepNo(int no_of_substeps)
    {

    }

    /**
     * Invoked to notify progress.
     * <p/>
     * This increments the current step.
     *
     * @param message a message describing the step
     */
    @Override
    public void progress(String message)
    {
        // no-op
    }

    /**
     * Invoked when an action restarts.
     *
     * @param name           the name of the action
     * @param overallMessage a message describing the overall progress
     * @param tip            a tip describing the current progress
     * @param steps          the number of steps the action consists of
     */
    @Override
    public void restartAction(String name, String overallMessage, String tip, int steps)
    {
        // no-op
    }
}
