/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.panels.packs;

import java.io.PrintWriter;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.resource.Messages;

import com.izforge.izpack.installer.console.PanelConsole;
import com.izforge.izpack.installer.console.AbstractPanelConsole;

import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.api.handler.Prompt.Type;
import com.izforge.izpack.api.handler.Prompt.Options;
import com.izforge.izpack.api.handler.Prompt.Option;

import com.izforge.izpack.util.Console;

/**
 * Console implementation for the TreePacksPanel.
 *
 * Based on PacksPanelConsoleHelper
 *
 * @author Sergiy Shyrkov
 * @author Dustin Kut Moy Cheung
 */
public class PacksPanelConsole extends AbstractPanelConsole implements PanelConsole
{

    private Messages messages;

    private String REQUIRED = "required";
    private String NOT_SELECTED = "Not Selected";
    private String ALREADY_SELECTED = "Already Selected";
    private String CONTINUE = "Continue?";
    private String NO_PACKS = "No packs selected.";
    private String DONE = "Done!";

    private String SPACE = " ";

    private HashMap<String, Pack> names;

    private ConsolePrompt consolePrompt;

    /** Are there dependencies in the packs */
    private boolean dependenciesExist = false;

    private void loadLangpack(InstallData installData)
    {
        messages = installData.getMessages();
    }

    /**
     * Generates a properties file for each input field or variable.
     *
     * @param installData the installation data
     * @param printWriter the properties file to write to
     * @return <tt>true</tt> if the generation is successful, otherwise <tt>false</tt>
     */
    public boolean runGeneratePropertiesFile(InstallData installData, PrintWriter printWriter)
    {
        return true;
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return <tt>true</tt> if the installation is successful, otherwise <tt>false</tt>
     */
    public boolean runConsoleFromProperties(InstallData installData, Properties properties)
    {
        return true;
    }

    /**
     * Runs the panel in interactive console mode.
     *
     * @param installData the installation data
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    public boolean runConsole(InstallData installData)
    {
        return true;
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    public boolean runConsole(InstallData installData, Console console)
    {
        consolePrompt = new ConsolePrompt(console);
        out(Type.INFORMATION, "");
        List<String> kids;
        List<Pack> selectedPacks = new LinkedList<Pack>();
        loadLangpack(installData);
        computePacks(installData.getAvailablePacks());

        // private HashMap<String, Pack> names;
        for (String key: names.keySet())
        {
            drawHelper(key, selectedPacks, installData);
        }
        out(Type.INFORMATION, DONE);

        installData.setSelectedPacks(selectedPacks);

        if (selectedPacks.size() == 0)
        {
            out(Type.WARNING, "You have not selected any packs!");
            out(Type.WARNING, "Are you sure you want to continue?");
        }
        return promptEndPanel(installData, console);
    }

    private void out(Type type, String message)
    {
        consolePrompt.message(type, message);
    }


    private String getTranslation(String id)
    {
        return messages.get(id);
    }

    /**
     * It is used to "draw" the appropriate tree-like structure of the packs and ask if you want to install
     * the pack. The pack will automatically be selected if it is required; otherwise you will be prompted if
     * you want to install that pack. If a pack is not selected, then their child packs won't be installed as
     * well and you won't be prompted to install them.
     *
     * @param pack              - the pack to install
     * @param selectedPacks     - the packs that are selected by the user are added there
     * @param installData       - Database of izpack
     *
     * @return void
     */
    private void drawHelper(final String pack, final List<Pack> selectedPacks, final InstallData installData)
    {
        Pack p                      = names.get(pack);
        Boolean conditionSatisfied  = checkCondition(installData, p);
        Boolean conditionExists     = !(conditionSatisfied == null);
        String packName             = p.getName();
        String id                   = p.getLangPackId();

        // If a condition is set to that pack
        if (conditionExists) {
            if (conditionSatisfied) {

                out(Type.INFORMATION, packName + SPACE + ALREADY_SELECTED);
                selectedPacks.add(p);

            } else {
                // condition says don't install!
                out(Type.INFORMATION, packName + SPACE + NOT_SELECTED);
            }
            // If no condition specified
        } else if (p.isRequired()) {
            out(Type.INFORMATION, packName + SPACE + REQUIRED);

            selectedPacks.add(p);
            // Prompt the user
        } else if (askUser(packName)) {
            selectedPacks.add(p);
        }
    }

    /**
     * helper method to know if the condition assigned to the pack is satisfied
     *
     * @param installData       - the data of izpack
     * @param pack              - the pack whose condition needs to be checked·
     * @return true             - if the condition is satisfied
     *         false            - if condition not satisfied
     *         null             - if no condition assigned
     */
    private Boolean checkCondition(InstallData installData, Pack pack)
    {
        if (pack.hasCondition()) {
            return installData.getRules().isConditionTrue(pack.getCondition());
        } else {
            return null;
        }
    }

    /**
     * Helper method to read the input of user
     * Method returns true if user types "y", "yes" or <Enter>·
     *
     * @return boolean  - true if condition above satisfied. Otherwise false
     */
    private boolean askUser(String message)
    {
        return Option.YES == consolePrompt.confirm(Type.QUESTION, message, Options.YES_NO);
    }


    /**
     * Computes pack related installDataGUI like the names or the dependencies state.
     *
     * @param packs The list of packs.
     */
    private void computePacks(List<Pack> packs)
    {
        names = new HashMap<String, Pack>();
        dependenciesExist = false;
        for (Pack pack : packs)
        {
            names.put(pack.getName(), pack);
            if (pack.getDependencies() != null || pack.getExcludeGroup() != null)
            {
                dependenciesExist = true;
            }
        }
    }
}
