/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.event;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.ProgressNotifiers;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DynamicVariableImpl;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.core.variable.ConfigFileValue;
import com.izforge.izpack.core.variable.EnvironmentValue;
import com.izforge.izpack.core.variable.ExecValue;
import com.izforge.izpack.core.variable.JarEntryConfigValue;
import com.izforge.izpack.core.variable.PlainConfigFileValue;
import com.izforge.izpack.core.variable.PlainValue;
import com.izforge.izpack.core.variable.RegistryValue;
import com.izforge.izpack.core.variable.ZipEntryConfigFileValue;
import com.izforge.izpack.core.variable.filters.LocationFilter;
import com.izforge.izpack.core.variable.filters.RegularExpressionFilter;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.config.ConfigFileTask;
import com.izforge.izpack.util.config.ConfigurableTask;
import com.izforge.izpack.util.config.MultiMapConfigEntry;
import com.izforge.izpack.util.config.RegistryConfigTask;
import com.izforge.izpack.util.config.MultiMapConfigFileTask;
import com.izforge.izpack.util.config.MultiMapConfigEntry.*;
import com.izforge.izpack.util.config.IniFileConfigTask;
import com.izforge.izpack.util.config.PropertyFileConfigTask;
import com.izforge.izpack.util.config.XmlFileConfigTask;
import com.izforge.izpack.util.file.FileNameMapper;
import com.izforge.izpack.util.file.GlobPatternMapper;
import com.izforge.izpack.util.file.types.FileSet;
import com.izforge.izpack.util.file.types.Mapper;
import com.izforge.izpack.util.helper.SpecHelper;


public class ConfigurationInstallerListener extends AbstractProgressInstallerListener
{
    private static final Logger logger = Logger.getLogger(ConfigurationInstallerListener.class.getName());

    /**
     * Name of the specification file
     */
    public static final String SPEC_FILE_NAME = "ConfigurationActionsSpec.xml";
    
    // Config actions spec names
    public static final String SPEC_ACTION = "configurationaction";
    public static final String SPEC_CONFIGURABLES = "configurables";
    public static final String SPEC_VARIABLES = "variables";
    public static final String SPEC_VARIABLE = "variable";
    public static final String SPEC_CONFIG_REG = "registry";
    public static final String SPEC_CONFIG_PROP = "propertyfile";
    public static final String SPEC_CONFIG_INI = "inifile";
    public static final String SPEC_CONIG_XML = "xmlfile";
	public static final String SPEC_ENTRY = "entry";
	public static final String SPEC_FILESET = "fileset";
	public static final String SPEC_MAPPER = "mapper";
	public static final String SPEC_XPATH_PROP = "xpathproperty";
    
	// Common file config spec names
	public static final String SPEC_TOFILE = "tofile";
	public static final String SPEC_FROMFILE = "fromfile";
	public static final String SPEC_TARGETFILE = "targetfile";	
	public static final String SPEC_TODIR = "todir";
    public static final String SPEC_CLEANUP = "cleanup";
	public static final String SPEC_CREATE = "create";
	public static final String SPEC_OVERWRITE = "overwrite";
	public static final String SPEC_FAIL = "failonerror";
	public static final String SPEC_PRESERVEMOD = "preservelastmodified";
	
	// Multi-file operation spec attributes
	public static final String SPEC_EMPTYDIRS = "includeemptydirs";
	public static final String SPEC_MAPPINGS = "enablemultiplemappings";

	// MultiMap-specific spec attributes
	public static final String SPEC_KEEPKEYS = "keepOldKeys";
	public static final String SPEC_KEEPVALUES = "keepOldValues";
	public static final String SPEC_RESOLVE = "resolveExpressions";
	public static final String SPEC_ESCAPE = "escape";
	public static final String SPEC_ESCAPENEWLINE = "escapeNewLine";
	public static final String SPEC_HEADER = "headerComment";
	public static final String SPEC_EMPTYLINES = "emptyLines";
	public static final String SPEC_OPERATOR = "operator";
	
	// Implementation-specific spec attributes
	public static final String SPEC_REG_FROM = "fromkey";
	public static final String SPEC_REG_TO = "tokey";
	public static final String SPEC_XML_CONFIGFILE = "configfile";
	
	// Entry spec common attributes
	public static final String SPEC_DATATYPE = "dataType"; 
	public static final String SPEC_LOOKUPTYPE = "lookupType"; 
	public static final String SPEC_OPERATION = "operation"; 
	public static final String SPEC_UNIT = "unit"; 
	public static final String SPEC_DEFAULT = "default"; 
	public static final String SPEC_PATTERN = "pattern"; 
	public static final String SPEC_CONDITION = "condition";
	
	// Implementation-specific entry attributes
	public static final String SPEC_SECTION = "section";
	public static final String SPEC_KEY = "key";
	public static final String SPEC_VALUE = "value";
	public static final String SPEC_CASESENSITIVE = "casesensitive";
	public static final String SPEC_FS_DIR = "dir";
	public static final String SPEC_FS_FILE = "file";
	public static final String SPEC_FS_INCLUDES = "includes";
	public static final String SPEC_FS_EXCLUDES = "excludes";
	public static final String SPEC_FS_INCLUDE = "include";
	public static final String SPEC_FS_EXCLUDE = "exclude";
	public static final String SPEC_FS_NAME = "name";
	public static final String SPEC_FS_DEFEXCLUDES = "defaultexcludes";
	public static final String SPEC_FS_FOLLOWSLINKS = "followsymlinks";
	public static final String SPEC_MAPPER_TYPE = "type";
	public static final String SPEC_MAPPER_FROM = "from";
	public static final String SPEC_MAPPER_TO = "to";
	public static final String SPEC_REG_KEY = "subkey";
	public static final String SPEC_REG_VALUE = "value";
	public static final String SPEC_REG_DATA = "data";
	public static final String SPEC_XPATH_KEY = "key";
	public static final String SPEC_XPATH_VALUE = "value";	
	
    private static final String ERRMSG_CONFIGACTION_BADATTR = "Bad attribute value in configuration action: {0}=\"{1}\" not allowed";

    /**
     * The configuration actions.
     */
    private final Map<String, Map<Object, List<ConfigurationAction>>> actions
            = new HashMap<String, Map<Object, List<ConfigurationAction>>>();

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The variable replacer.
     */
    private final VariableSubstitutor replacer;

    /**
     * The specification helper.
     */
    private SpecHelper spec;

    private VariableSubstitutor substlocal;
    
    private UninstallData uninstallData;


    /**
     * Constructs a <tt>ConfigurationInstallerListener</tt>.
     *
     * @param installData the installation data
     * @param resources   the resources
     * @param replacer    the variable replacer
     * @param notifiers   the progress notifiers
     */
    public ConfigurationInstallerListener(InstallData installData, UninstallData uninstallData, Resources resources,
                                          VariableSubstitutor replacer, ProgressNotifiers notifiers)
    {
        super(installData, notifiers);
        this.resources = resources;
        this.replacer = replacer;
        this.uninstallData = uninstallData;
    }

    /**
     * Returns the actions map.
     *
     * @return the actions map
     */
    public Map<String, Map<Object, List<ConfigurationAction>>> getActions()
    {
        return actions;
    }

    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    @Override
    public void initialise()
    {
        spec = new SpecHelper(resources);
        try
        {
            spec.readSpec(SPEC_FILE_NAME, replacer);
        }
        catch (Exception exception)
        {
            throw new IzPackException("Failed to read: " + SPEC_FILE_NAME, exception);
        }
    }

    /**
     * Invoked before packs are installed.
     *
     * @param packs the packs to be installed
     * @throws IzPackException for any error
     */
    @Override
    public void beforePacks(List<Pack> packs)
    {
        if (spec.getSpec() == null)
        {
            return;
        }

        for (Pack p : packs)
        {
            logger.fine("Entering beforepacks configuration action for pack " + p.getName());

            // Resolve data for current pack.
            IXMLElement pack = spec.getPackForName(p.getName());
            if (pack == null)
            {
                continue;
            }

            logger.fine("Found configuration action descriptor for pack " + p.getName());
            // Prepare the action cache
            Map<Object, List<ConfigurationAction>> packActions = new HashMap<Object, List<ConfigurationAction>>();
            packActions.put(ActionBase.BEFOREPACK, new ArrayList<ConfigurationAction>());
            packActions.put(ActionBase.AFTERPACK, new ArrayList<ConfigurationAction>());
            packActions.put(ActionBase.BEFOREPACKS, new ArrayList<ConfigurationAction>());
            packActions.put(ActionBase.AFTERPACKS, new ArrayList<ConfigurationAction>());

            // Get all entries for antcalls.
            List<IXMLElement> configActionEntries = pack.getChildrenNamed(SPEC_ACTION);
            if (configActionEntries != null)
            {
                logger.fine("Found " + configActionEntries.size() + " configuration actions");
                if (configActionEntries.size() >= 1)
                {
                    Iterator<IXMLElement> entriesIter = configActionEntries.iterator();
                    while (entriesIter != null && entriesIter.hasNext())
                    {
                        ConfigurationAction act = readConfigAction(entriesIter.next());
                        if (act != null)
                        {
                            logger.fine("Adding " + act.getOrder() + "configuration action with "
                                                + act.getActionTasks().size() + " tasks");
                            (packActions.get(act.getOrder())).add(act);
                        }
                    }
                    // Set for progress bar interaction.
                    if ((packActions.get(ActionBase.AFTERPACKS)).size() > 0)
                    {
                        setProgressNotifier();
                    }
                }
                // Set for progress bar interaction.
                if ((packActions.get(ActionBase.AFTERPACKS)).size() > 0)
                {
                    this.setProgressNotifier();
                }
            }

            actions.put(p.getName(), packActions);
        }
        for (Pack p : packs)
        {
            String currentPack = p.getName();
            performAllActions(currentPack, ActionBase.BEFOREPACKS, null);
        }
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack the pack
     * @param i    the pack number
     * @throws IzPackException for any error
     */
    @Override
    public void beforePack(Pack pack, int i)
    {
        performAllActions(pack.getName(), ActionBase.BEFOREPACK, null);
    }


    /**
     * Invoked after a pack is installed.
     *
     * @param pack the pack
     * @param i    the pack number
     * @throws IzPackException for any error
     */
    @Override
    public void afterPack(Pack pack, int i)
    {
        performAllActions(pack.getName(), ActionBase.AFTERPACK, null);
    }

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void afterPacks(List<Pack> packs, ProgressListener listener)
    {
        if (notifyProgress())
        {
            listener.nextStep(getMessage("ConfigurationAction.pack"), getProgressNotifierId(),
                              getActionCount(packs, ActionBase.AFTERPACKS));
        }
        for (Pack pack : packs)
        {
            String currentPack = pack.getName();
            performAllActions(currentPack, ActionBase.AFTERPACKS, listener);
        }
    }

    private int getActionCount(List<Pack> packs, String order)
    {
        int retval = 0;
        for (Pack pack : packs)
        {
            String currentPack = pack.getName();
            List<ConfigurationAction> actList = getActions(currentPack, order);
            if (actList != null)
            {
                retval += actList.size();
            }
        }
        return (retval);
    }

    /**
     * Returns the defined actions for the given pack in the requested order.
     *
     * @param packName name of the pack for which the actions should be returned
     * @param order    order to be used; valid are <i>beforepack</i> and <i>afterpack</i>
     * @return a list which contains all defined actions for the given pack and order
     */
    // -------------------------------------------------------
    protected List<ConfigurationAction> getActions(String packName, String order)
    {
        if (actions == null)
        {
            return null;
        }

        Map<Object, List<ConfigurationAction>> packActions = actions.get(packName);
        if (packActions == null || packActions.size() == 0)
        {
            return null;
        }

        return packActions.get(order);
    }

    /**
     * Performs all actions which are defined for the given pack and order.
     *
     * @param packName name of the pack for which the actions should be performed
     * @param order    order to be used; valid are <i>beforepack</i> and <i>afterpack</i>
     * @throws InstallerException
     */
    private void performAllActions(String packName, String order, ProgressListener listener)
            throws InstallerException
    {
        List<ConfigurationAction> actList = getActions(packName, order);
        if (actList == null || actList.size() == 0)
        {
            return;
        }

        logger.fine("Executing all " + order + " configuration actions for " + packName + " ...");
        for (ConfigurationAction act : actList)
        {
            // Inform progress bar if needed. Works only on AFTER_PACKS
            if (notifyProgress() && order.equals(ActionBase.AFTERPACKS))
            {
                listener.progress((act.getMessageID() != null) ? getMessage(act.getMessageID()) : "");
            }
            else
            {
                try
                {
                    act.performInstallAction();
                }
                catch (Exception e)
                {
                    throw new InstallerException(e);
                }
            }
        }
    }

    /**
     * Returns an ant call which is defined in the given XML element.
     *
     * @param el XML element which contains the description of an ant call
     * @return an ant call which is defined in the given XML element
     * @throws InstallerException
     */
    private ConfigurationAction readConfigAction(IXMLElement el) throws InstallerException
    {
        if (el == null)
        {
            return null;
        }
        ConfigurationAction act = new ConfigurationAction();
        try
        {
            act.setOrder(spec.getRequiredAttribute(el, ActionBase.ORDER));
        }
        catch (Exception e)
        {
            throw new InstallerException(e);
        }

        // Read specific attributes and nested elements
        substlocal = new VariableSubstitutorImpl(readVariables(el.getFirstChildNamed(SPEC_VARIABLES)));
        act.setActionTasks(readConfigurables(el.getFirstChildNamed(SPEC_CONFIGURABLES)));

        return act;
    }

    private String substituteVariables(String name)
    {
        try
        {
            name = replacer.substitute(name);
        }
        catch (Exception exception)
        {
            logger.log(Level.WARNING, "Failed to substitute: " + name, exception);
        }
        if (substlocal != null)
        {
            try
            {
                name = substlocal.substitute(name);
            }
            catch (Exception exception)
            {
                logger.log(Level.WARNING, "Failed to substitute: " + name, exception);
            }
        }
        return name;
    }

    private <K, V> void readMultiMapConfigFileTaskCommonAttributes(IXMLElement el, MultiMapConfigFileTask<K, V> task)
            throws InstallerException
    {
    	Boolean boolAttr = null;
    	
    	boolAttr = (getBooleanAttribute(el, SPEC_KEEPKEYS));
    	if (boolAttr != null)
		{
    		task.setPatchPreserveEntries(boolAttr);
		}
    	boolAttr = (getBooleanAttribute(el, SPEC_KEEPVALUES));
    	if (boolAttr != null)
		{
    		task.setPatchPreserveValues(boolAttr);
		}
    	boolAttr = (getBooleanAttribute(el, SPEC_RESOLVE));
    	if (boolAttr != null)
		{
    		task.setPatchResolveVariables(boolAttr);
		}
    	boolAttr = (getBooleanAttribute(el, SPEC_ESCAPE));
    	if (boolAttr != null)
		{
    		task.setEscape(boolAttr);
		}
    	boolAttr = (getBooleanAttribute(el, SPEC_ESCAPENEWLINE));
    	if (boolAttr != null)
		{
    		task.setEscapeNewLine(boolAttr);
		}        
    	boolAttr = (getBooleanAttribute(el, SPEC_HEADER));
    	if (boolAttr != null)
		{
    		task.setHeaderComment(boolAttr);
		}
    	boolAttr = (getBooleanAttribute(el, SPEC_EMPTYLINES));
    	if (boolAttr != null)
		{
    		task.setEmptyLines(boolAttr);
		}
    	String operator = getAttribute(el, SPEC_OPERATOR);
        if (operator != null)
        {
            task.setOperator(operator);
        }
    }

    private void readConfigFileTaskCommonAttributes(InstallData idata, IXMLElement config, ConfigFileTask task)
            throws InstallerException
    {
    	// read common file attributes
        task.setToFile(FileUtil.getAbsoluteFile(requireAttribute(config, SPEC_TOFILE), idata.getInstallPath()));
        task.setSrcFile(FileUtil.getAbsoluteFile(getAttribute(config, SPEC_FROMFILE), idata.getInstallPath()));
        task.setTargetFile(FileUtil.getAbsoluteFile(getAttribute(config, SPEC_TARGETFILE), idata.getInstallPath()));
        task.setToDir(FileUtil.getAbsoluteFile(getAttribute(config, SPEC_TODIR), idata.getInstallPath()));
        
        // read common flag attributes
        Boolean boolAttr = (getBooleanAttribute(config, SPEC_CLEANUP));
    	if (boolAttr != null)
		{
    		task.setCleanup(boolAttr);
		}
    	boolAttr = getBooleanAttribute(config, SPEC_CREATE);
    	if (boolAttr != null)
		{
    		task.setCreate(boolAttr);
		}
    	boolAttr = (getBooleanAttribute(config, SPEC_OVERWRITE));
    	if (boolAttr != null)
		{
    		task.setOverwrite(boolAttr);
		}
    	boolAttr = (getBooleanAttribute(config, SPEC_FAIL));
    	if (boolAttr != null)
		{
    		task.setFailOnError(boolAttr);
		}
    	boolAttr = (getBooleanAttribute(config, SPEC_PRESERVEMOD));
    	if (boolAttr != null)
		{
    		task.setPreserveLastModified(boolAttr);
		}
    	boolAttr = (getBooleanAttribute(config, SPEC_EMPTYDIRS));
    	if (boolAttr != null)
		{
    		task.setIncludeEmptyDirs(boolAttr);
		}
    	boolAttr = (getBooleanAttribute(config, SPEC_MAPPINGS));
    	if (boolAttr != null)
		{
    		task.setEnableMultipleMappings(boolAttr);
		}

    	// read common nested elements
    	for (FileSet fs : readFileSets(config.getChildrenNamed(SPEC_FILESET)))
        {
            task.addFileSet(fs);
        }
        try
        {
            for (FileNameMapper mapper : readMappers(config.getChildrenNamed(SPEC_MAPPER)))
            {
                task.add(mapper);
            }
        }
        catch (Exception e)
        {
            throw new InstallerException(e.getMessage());
        }
	}

    protected List<ConfigurationActionTask> readConfigurables(IXMLElement configurables) throws InstallerException
    {
        List<ConfigurationActionTask> configtasks = new ArrayList<ConfigurationActionTask>();
        InstallData idata = getInstallData();
        for (IXMLElement config : configurables.getChildren())
        {
            String attrib = config.getName();
            ConfigType configType;
            if (attrib != null)
            {
                configType = ConfigType.getFromAttribute(attrib);
                if (configType == null)
                {
                    throw new InstallerException("Unknown configurable type '" + attrib + "'");
                }
            }
            else
            {
                logger.warning("ignoring unexpected character data in element " + SPEC_CONFIGURABLES);
                continue;
            }

            ConfigurableTask task;
            switch (configType)
            {
                case OPTIONS:
                    task = new PropertyFileConfigTask();
                    readConfigFileTaskCommonAttributes(idata, config, (ConfigFileTask) task);
                    readMultiMapConfigFileTaskCommonAttributes(config, (PropertyFileConfigTask) task);
                    readAndAddMultiMapConfigEntries(config.getChildrenNamed(SPEC_ENTRY), (PropertyFileConfigTask) task);
                    break;

                case INI:
                    task = new IniFileConfigTask();
                    readConfigFileTaskCommonAttributes(idata, config, (ConfigFileTask) task);
                    readMultiMapConfigFileTaskCommonAttributes(config, (IniFileConfigTask) task);
                    readAndAddMultiMapConfigEntries(config.getChildrenNamed(SPEC_ENTRY), (IniFileConfigTask) task);
                    break;

                case XML:
                    task = new XmlFileConfigTask();
                    readConfigFileTaskCommonAttributes(idata, config, (ConfigFileTask) task);
                    ((XmlFileConfigTask) task).setConfigFile(
                            FileUtil.getAbsoluteFile(getAttribute(config, SPEC_XML_CONFIGFILE), idata.getInstallPath()));                    
                    readAndAddXPathProperties(config.getChildrenNamed(SPEC_XPATH_PROP), (XmlFileConfigTask) task);
                    break;

                case REGISTRY:
                    task = new RegistryConfigTask();
                    ((RegistryConfigTask) task).setSrcKey(getAttribute(config, SPEC_REG_FROM));
                    ((RegistryConfigTask) task).setToKey(requireAttribute(config, SPEC_REG_TO));
                    readMultiMapConfigFileTaskCommonAttributes(config, (RegistryConfigTask) task);
                    readAndAddRegistryConfigEntries(config.getChildrenNamed(SPEC_ENTRY), (RegistryConfigTask) task);
                    break;

                default:
                    // This should never happen
                    throw new InstallerException(
                            "Type '" + configType.getAttribute() + "' currently not allowed for Configurable");
            }

            configtasks.add(new ConfigurationActionTask(task, getAttribute(config, SPEC_CONDITION),
                                                        getInstallData().getRules()));
        }
        return configtasks;
    }


    public <K, V> void readAndAddMultiMapConfigEntries(List<IXMLElement> entries, MultiMapConfigFileTask<K, V> task) throws InstallerException
    {    	
        for (IXMLElement entrySpec : entries)
        {
        	MultiMapConfigEntry entry = new MultiMapConfigEntry();
            readMultiMapConfigEntryCommonAttributes(entrySpec, entry);
            entry.setSection(getAttribute(entrySpec, SPEC_SECTION));
            entry.setKey(requireAttribute(entrySpec, SPEC_KEY));
            entry.setValue(getAttribute(entrySpec, SPEC_VALUE));
            task.addEntry(entry);
        }
    }
        
    public <K, V> void readAndAddRegistryConfigEntries(List<IXMLElement> entries, MultiMapConfigFileTask<K, V> task) throws InstallerException
    {
        for (IXMLElement entrySpec : entries)
        {
        	MultiMapConfigEntry entry = new MultiMapConfigEntry();
            readMultiMapConfigEntryCommonAttributes(entrySpec, entry);
        	entry.setSection(getAttribute(entrySpec, SPEC_REG_KEY));
            entry.setKey(requireAttribute(entrySpec, SPEC_REG_VALUE));
            entry.setValue(getAttribute(entrySpec, SPEC_REG_DATA));
            task.addEntry(entry);
        }
    }

    private void readMultiMapConfigEntryCommonAttributes(IXMLElement entrySpec, MultiMapConfigEntry entry) throws InstallerException
    {
        String attrib = entrySpec.getAttribute(SPEC_DATATYPE);
        if (attrib != null)
        {
            Type type = Type.getFromAttribute(attrib);
            if (type == null)
            {
                // TODO Inform about misconfigured configuration actions during
                // compilation
                throw new InstallerException(MessageFormat.format(ERRMSG_CONFIGACTION_BADATTR,
                		SPEC_DATATYPE, attrib));
            }
            entry.setType(type);
        }
        attrib = entrySpec.getAttribute(SPEC_LOOKUPTYPE);
        if (attrib != null)
        {
            LookupType lookupType = LookupType.getFromAttribute(attrib);
            if (lookupType == null)
            {
                // TODO Inform about misconfigured configuration actions during compilation
                throw new InstallerException(MessageFormat.format(ERRMSG_CONFIGACTION_BADATTR,
                		SPEC_LOOKUPTYPE, attrib));
            }
            entry.setLookupType(lookupType);
        }
        attrib = entrySpec.getAttribute(SPEC_OPERATION);
        if (attrib != null)
        {
            Operation operation = Operation.getFromAttribute(attrib);
            if (operation == null)
            {
              // TODO Inform about misconfigured configuration actions during compilation
              throw new InstallerException(
                  MessageFormat.format(
                      ERRMSG_CONFIGACTION_BADATTR,
                      SPEC_OPERATION, attrib)
                  );
            }
            entry.setOperation(operation);
        }
        attrib = entrySpec.getAttribute(SPEC_UNIT);
        if (attrib != null)
        {
            Unit unit = Unit.getFromAttribute(attrib);
            if (unit == null)
            {
                // TODO Inform about misconfigured configuration actions during compilation
                throw new InstallerException(MessageFormat.format(ERRMSG_CONFIGACTION_BADATTR,
                		SPEC_UNIT, attrib));
            }
            entry.setUnit(unit);
        }
        entry.setDefault(entrySpec.getAttribute(SPEC_DEFAULT));
        entry.setPattern(entrySpec.getAttribute(SPEC_PATTERN));
    }


    private void readAndAddXPathProperties(List<IXMLElement> xpathProps, XmlFileConfigTask task)
            throws InstallerException
    {
        for (IXMLElement xpathProp : xpathProps)
        {
            task.addProperty(requireAttribute(xpathProp, SPEC_XPATH_KEY), requireAttribute(xpathProp, SPEC_XPATH_VALUE));
        }
    }

    protected List<FileSet> readFileSets(List<IXMLElement> filesetSpecs)
            throws InstallerException
    {
        List<FileSet> filesets = new ArrayList<FileSet>();
        try
        {
            String installPath = getInstallData().getInstallPath();
            for (IXMLElement filesetSpec : filesetSpecs)
            {
                FileSet fileset = new FileSet();

                String strattr = getAttribute(filesetSpec, SPEC_FS_DIR);
                if (strattr != null)
                {
                    fileset.setDir(FileUtil.getAbsoluteFile(strattr, installPath));
                }

                strattr = getAttribute(filesetSpec, SPEC_FS_FILE);
                if (strattr != null)
                {
                    fileset.setFile(FileUtil.getAbsoluteFile(strattr, installPath));
                }
                else
                {
                    if (fileset.getDir() == null)
                    {
                        throw new InstallerException(
                                "At least one of both attributes, 'dir' or 'file' required in fileset");
                    }
                }

                strattr = getAttribute(filesetSpec, SPEC_FS_INCLUDES);
                if (strattr != null)
                {
                    fileset.setIncludes(strattr);
                }

                strattr = getAttribute(filesetSpec, SPEC_FS_EXCLUDES);
                if (strattr != null)
                {
                    fileset.setExcludes(strattr);
                }

                Boolean boolAttr = getBooleanAttribute(filesetSpec, SPEC_CASESENSITIVE);
                if (boolAttr != null)
                {
                    fileset.setCaseSensitive(boolAttr);
                }

                boolAttr = getBooleanAttribute(filesetSpec, SPEC_FS_DEFEXCLUDES);
                if (boolAttr != null)
                {
                    fileset.setDefaultexcludes(boolAttr);
                }

                boolAttr = getBooleanAttribute(filesetSpec, SPEC_FS_FOLLOWSLINKS);
                if (boolAttr != null)
                {
                    fileset.setFollowSymlinks(boolAttr);
                }

                readAndAddFilesetIncludes(filesetSpec.getChildrenNamed(SPEC_FS_INCLUDE), fileset);
                readAndAddFilesetExcludes(filesetSpec.getChildrenNamed(SPEC_FS_EXCLUDE), fileset);

                filesets.add(fileset);
            }
        }
        catch (Exception e)
        {
            throw new InstallerException(e);
        }
        return filesets;
    }

    protected List<FileNameMapper> readMappers(List<IXMLElement> mapperSpecs)
            throws InstallerException
    {
        List<FileNameMapper> mappers = new ArrayList<FileNameMapper>();
        try
        {
            for (IXMLElement mapperSpec : mapperSpecs)
            {
                String attrib = requireAttribute(mapperSpec, SPEC_MAPPER_TYPE);
                Mapper.MapperType mappertype;
                if (attrib != null)
                {
                    mappertype = Mapper.MapperType.getFromAttribute(attrib);
                    if (mappertype == null)
                    {
                        throw new InstallerException("Unknown filename mapper type '" + attrib + "'");
                    }
                }
                else
                {
                    throw new InstallerException("Missing filename mapper type");
                }
                FileNameMapper mapper = (FileNameMapper) Class.forName(mappertype.getImplementation()).newInstance();
                if (mapper instanceof GlobPatternMapper)
                {
                    Boolean boolAttr = getBooleanAttribute(mapperSpec, SPEC_CASESENSITIVE);
                    if (boolAttr != null)
                    {
                        ((GlobPatternMapper) mapper).setCaseSensitive(boolAttr);
                    }
                    mapper.setFrom(requireAttribute(mapperSpec, SPEC_MAPPER_FROM));
                    mapper.setTo(requireAttribute(mapperSpec, SPEC_MAPPER_TO));
                }
                else
                {
                    throw new InstallerException("Filename mapper type \"" + "\" currently not supported");
                }

                mappers.add(mapper);
            }
        }
        catch (Exception e)
        {
            throw new InstallerException(e);
        }
        return mappers;
    }

    private void readAndAddFilesetIncludes(List<IXMLElement> includeSpecs, FileSet fileset)
            throws InstallerException
    {
        for (IXMLElement includeSpec : includeSpecs)
        {
            fileset.createInclude().setName(requireAttribute(includeSpec, SPEC_FS_NAME));
        }
    }

    private void readAndAddFilesetExcludes(List<IXMLElement> excludeSpecs, FileSet fileset)
            throws InstallerException
    {
        for (IXMLElement excludeSpec : excludeSpecs)
        {
            fileset.createExclude().setName(requireAttribute(excludeSpec, SPEC_FS_NAME));
        }
    }

    private int getConfigFileType(String varname, String type)
            throws InstallerException
    {
        int filetype = ConfigFileValue.CONFIGFILE_TYPE_OPTIONS;
        if (type != null)
        {
            if (type.equalsIgnoreCase("options"))
            {
                filetype = ConfigFileValue.CONFIGFILE_TYPE_OPTIONS;
            }
            else if (type.equalsIgnoreCase("xml"))
            {
                filetype = ConfigFileValue.CONFIGFILE_TYPE_XML;
            }
            else if (type.equalsIgnoreCase("ini"))
            {
                filetype = ConfigFileValue.CONFIGFILE_TYPE_INI;
            }
            else
            {
                parseError("Error in definition of dynamic variable " + varname + ": Unknown entry type " + type);
            }
        }
        return filetype;
    }

    protected Properties readVariables(IXMLElement variables) throws InstallerException
    {
        List<DynamicVariable> dynamicVariables = null;

        if (variables != null)
        {
            dynamicVariables = new LinkedList<DynamicVariable>();

            for (IXMLElement var : variables.getChildrenNamed(SPEC_VARIABLE))
            {
                String name = requireAttribute(var, "name");
                logger.fine("Reading variable '" + name +"'");

                DynamicVariable dynamicVariable = new DynamicVariableImpl();
                dynamicVariable.setName(name);

                // Check for plain value
                String value = getAttribute(var, "value");
                if (value != null)
                {
                    dynamicVariable.setValue(new PlainValue(value));
                }
                else
                {
                    IXMLElement valueElement = var.getFirstChildNamed("value");
                    if (valueElement != null)
                    {
                        value = valueElement.getContent();
                        if (value == null)
                        {
                            parseError("Empty value element for dynamic variable " + name);
                        }
                        dynamicVariable.setValue(new PlainValue(value));
                    }
                }
                // Check for environment variable value
                value = getAttribute(var, "environment");
                if (value != null)
                {
                    if (dynamicVariable.getValue() == null)
                    {
                        dynamicVariable.setValue(new EnvironmentValue(value));
                    }
                    else
                    {
                        parseError("Ambiguous environment value definition for dynamic variable " + name);
                    }
                }
                // Check for registry value
                value = getAttribute(var, "regkey");
                if (value != null)
                {
                    String regroot = getAttribute(var, "regroot");
                    String regvalue = getAttribute(var, "regvalue");
                    if (dynamicVariable.getValue() == null)
                    {
                        dynamicVariable.setValue(
                                new RegistryValue(regroot, value, regvalue));
                    }
                    else
                    {
                        parseError("Ambiguous registry value definition for dynamic variable " + name);
                    }
                }
                // Check for value from plain config file
                value = var.getAttribute("file");
                if (value != null)
                {
                    String stype = var.getAttribute("type");
                    String filesection = var.getAttribute("section");
                    String filekey = requireAttribute(var, "key");
                    if (dynamicVariable.getValue() == null)
                    {
                        dynamicVariable.setValue(new PlainConfigFileValue(value, getConfigFileType(
                                name, stype), filesection, filekey));
                    }
                    else
                    {
                        // unexpected combination of variable attributes
                        parseError("Ambiguous file value definition for dynamic variable " + name);
                    }
                }
                // Check for value from config file entry in a zip file
                value = var.getAttribute("zipfile");
                if (value != null)
                {
                    String entryname = requireAttribute(var, "entry");
                    String stype = var.getAttribute("type");
                    String filesection = var.getAttribute("section");
                    String filekey = requireAttribute(var, "key");
                    if (dynamicVariable.getValue() == null)
                    {
                        dynamicVariable.setValue(new ZipEntryConfigFileValue(value, entryname,
                                                                             getConfigFileType(name, stype), filesection,
                                                                             filekey));
                    }
                    else
                    {
                        // unexpected combination of variable attributes
                        parseError("Ambiguous file value definition for dynamic variable " + name);
                    }
                }
                // Check for value from config file entry in a jar file
                value = var.getAttribute("jarfile");
                if (value != null)
                {
                    String entryname = requireAttribute(var, "entry");
                    String stype = var.getAttribute("type");
                    String filesection = var.getAttribute("section");
                    String filekey = requireAttribute(var, "key");
                    if (dynamicVariable.getValue() == null)
                    {
                        dynamicVariable.setValue(new JarEntryConfigValue(value, entryname,
                                                                         getConfigFileType(name, stype), filesection,
                                                                         filekey));
                    }
                    else
                    {
                        // unexpected combination of variable attributes
                        parseError("Ambiguous file value definition for dynamic variable " + name);
                    }
                }
                // Check for config file value
                value = getAttribute(var, "executable");
                if (value != null)
                {
                    if (dynamicVariable.getValue() == null)
                    {
                        String dir = var.getAttribute("dir");
                        String exectype = var.getAttribute("type");
                        String boolval = var.getAttribute("stderr");
                        boolean stderr = false;
                        if (boolval != null)
                        {
                            stderr = Boolean.parseBoolean(boolval);
                        }

                        if (value.length() <= 0)
                        {
                            parseError("No command given in definition of dynamic variable " + name);
                        }
                        Vector<String> cmd = new Vector<String>();
                        cmd.add(value);
                        List<IXMLElement> args = var.getChildrenNamed("arg");
                        if (args != null)
                        {
                            for (IXMLElement arg : args)
                            {
                                String content = arg.getContent();
                                if (content != null)
                                {
                                    cmd.add(content);
                                }
                            }
                        }
                        String[] cmdarr = new String[cmd.size()];
                        if (exectype.equalsIgnoreCase("process") || exectype == null)
                        {
                            dynamicVariable.setValue(new ExecValue(cmd.toArray(cmdarr), dir, false, stderr));
                        }
                        else if (exectype.equalsIgnoreCase("shell"))
                        {
                            dynamicVariable.setValue(new ExecValue(cmd.toArray(cmdarr), dir, true, stderr));
                        }
                        else
                        {
                            parseError("Bad execution type " + exectype + " given for dynamic variable " + name);
                        }
                    }
                    else
                    {
                        parseError("Ambiguous execution output value definition for dynamic variable " + name);
                    }
                }

                if (dynamicVariable.getValue() == null)
                {
                    parseError("No value specified at all for dynamic variable " + name);
                }

                // Check whether dynamic variable has to be evaluated only once during installation
                value = getAttribute(var, "checkonce");
                if (value != null)
                {
                    dynamicVariable.setCheckonce(Boolean.valueOf(value));
                }

                // Check whether evaluation failures of the dynamic variable should be ignored
                value = getAttribute(var, "ignorefailure");
                if (value != null)
                {
                    dynamicVariable.setIgnoreFailure(Boolean.valueOf(value));
                }

                IXMLElement filters = var.getFirstChildNamed("filters");
                if (filters != null)
                {
                    List<IXMLElement> filterList = filters.getChildren();
                    for (IXMLElement filterElement : filterList)
                    {
                        if (filterElement.getName().equals("regex"))
                        {
                            String expression = filterElement.getAttribute("regexp");
                            String selectexpr = filterElement.getAttribute("select");
                            String replaceexpr = filterElement.getAttribute("replace");
                            String defaultvalue = filterElement.getAttribute("defaultvalue");
                            String scasesensitive = filterElement.getAttribute("casesensitive");
                            String sglobal = filterElement.getAttribute("global");
                            dynamicVariable.addFilter(
                                    new RegularExpressionFilter(
                                            expression, selectexpr,
                                            replaceexpr, defaultvalue,
                                            Boolean.valueOf(scasesensitive != null ? scasesensitive : "true"),
                                            Boolean.valueOf(sglobal != null ? sglobal : "false")));
                        }
                        else if (filterElement.getName().equals("location"))
                        {
                            String basedir = filterElement.getAttribute("basedir");
                            dynamicVariable.addFilter(new LocationFilter(basedir));
                        }
                    }
                }
                try
                {
                    dynamicVariable.validate();
                }
                catch (Exception e)
                {
                    parseError("Error in definition of dynamic variable " + name + ": "
                                       + e.getMessage());
                }

                String conditionid = getAttribute(var, "condition");
                dynamicVariable.setConditionid(conditionid);

                dynamicVariables.add(dynamicVariable);
            }
        }

        return evaluateDynamicVariables(dynamicVariables);
    }

    private Properties evaluateDynamicVariables(List<DynamicVariable> dynamicvariables)
            throws InstallerException
    {
        // FIXME change DynamicVariableSubstitutor constructor interface
        //DynamicVariableSubstitutor dynsubst = new DynamicVariableSubstitutor((List)dynamicvariables, installdata.getRules());
        Properties props = new Properties();

        if (dynamicvariables != null)
        {
            logger.fine("Evaluating configuration variables");
            RulesEngine rules = getInstallData().getRules();
            for (DynamicVariable dynvar : dynamicvariables)
            {
                String name = dynvar.getName();
                logger.fine("Configuration variable: " + name);
                boolean refresh = false;
                String conditionid = dynvar.getConditionid();
                logger.fine("condition: " + conditionid);
                if ((conditionid != null) && (conditionid.length() > 0))
                {
                    if ((rules != null) && rules.isConditionTrue(conditionid))
                    {
                        logger.fine("Refresh configuration variable \"" + name
                                            + "\" based on global condition \" " + conditionid + "\"");
                        // condition for this rule is true
                        refresh = true;
                    }
                }
                else
                {
                    logger.fine("Refresh configuration variable \"" + name
                                        + "\" based on local condition \" " + conditionid + "\"");
                    // empty condition
                    refresh = true;
                }
                if (refresh)
                {
                    try
                    {
                        String newValue = dynvar.evaluate(replacer);
                        if (newValue != null)
                        {
                            logger.fine("Configuration variable " + name + ": " + newValue);
                            props.setProperty(name, newValue);
                        }
                        else
                        {
                            logger.fine("Configuration variable " + name + " unchanged: " + dynvar.getValue());
                        }
                    }
                    catch (Exception e)
                    {
                        throw new InstallerException(e);
                    }
                }
            }
        }

        return props;
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or
     * empty. It is an error for 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected String requireAttribute(IXMLElement element, String attribute)
            throws InstallerException
    {
        String value = getAttribute(element, attribute);
        if (value == null)
        {
            parseError(element, "<" + element.getName() + "> requires attribute '" + attribute + "'");
        }
        return value;
    }

    protected String getAttribute(IXMLElement element, String attribute)
            throws InstallerException
    {
        String value = element.getAttribute(attribute);
        if (value != null)
        {
            return substituteVariables(value);
        }
        return value;
    }
    
    /**
     * Call getAttribute on an element, and return the boolean representation of the value.
     * The value is considered to represent {@code true} if it is set to "true", "yes", or "on".
     * 
     * @param element the element containing the attribute
     * @param attribute the name of the attribute to evaluate
     * @return true, if the value is "true", "yes", or "on"; false if any other value (including
     * the empty string); null if the attribute was not specified
     */
    protected Boolean getBooleanAttribute(IXMLElement element, String attribute)
    		throws InstallerException
	{
    	String value = getAttribute(element, attribute);
    	if ( value == null)
    	{ 
    		return null;
    	}
    	else if	(value.equalsIgnoreCase("true") || 
    			 value.equalsIgnoreCase("1"))
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
	}

    /**
     * Create parse error with consistent messages. Includes file name.
     *
     * @param message Brief message explaining error
     */
    protected void parseError(String message) throws InstallerException
    {
        throw new InstallerException(SPEC_FILE_NAME + ":" + message);
    }

    /**
     * Create parse error with consistent messages. Includes file name and line # of parent. It is
     * an error for 'parent' to be null.     *
     *
     * @param parent  The element in which the error occured
     * @param message Brief message explaining error
     */
    protected void parseError(IXMLElement parent, String message) throws InstallerException
    {
        throw new InstallerException(SPEC_FILE_NAME + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Create a parse warning with consistent messages. Includes file name and line # of parent.     *
     *
     * @param parent  The element in which the warning occured
     * @param message Warning message
     */
    protected void parseWarn(IXMLElement parent, String message)
    {
        System.out.println("Warning: " + SPEC_FILE_NAME + ":" + parent.getLineNr() + ": " + message);
    }

    public enum ConfigType
    {
        OPTIONS("propertyfile"), INI("inifile"), XML("xmlfile"), REGISTRY("registry");

        private static Map<String, ConfigType> lookup;

        private String attribute;

        ConfigType(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, ConfigType>();
            for (ConfigType operation : EnumSet.allOf(ConfigType.class))
            {
                lookup.put(operation.getAttribute(), operation);
            }
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static ConfigType getFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }
    }

}
