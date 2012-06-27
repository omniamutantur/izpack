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

package com.izforge.izpack.panels.info;

import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.console.AbstractTextPanelConsole;

/**
 * Console implementation of {@link InfoPanel}.
 *
 * @author Tim Anderson
 */
public class InfoPanelConsole extends AbstractTextPanelConsole
{

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * Constructs an <tt>InfoPanelConsole</tt>.
     *
     * @param resources the resources
     */
    public InfoPanelConsole(Resources resources)
    {
        this.resources = resources;
    }

    /**
     * Returns the text to display.
     *
     * @return the text
     */
    @Override
    protected String getText()
    {
        String defaultValue = "Error : could not load the info text !";
        return resources.getString("InfoPanel.info", defaultValue);
    }
}
