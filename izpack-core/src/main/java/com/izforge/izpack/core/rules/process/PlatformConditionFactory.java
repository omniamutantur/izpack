package com.izforge.izpack.core.rules.process;

import java.util.HashMap;
import java.util.Map;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;

/**
 * Generates the pre-defined OS conditions.
 * 
 * @author daniela
 *
 */
public class PlatformConditionFactory {
	
	/**
	 * An enum encapsulating all available pre-defined OS conditions.
	 * 
	 * @author daniela
	 *
	 */
	private static enum PlatformConditionSpec{
		AIX ("izpack.aixinstall", Platforms.AIX),
		WINDOWS ("izpack.windowsinstall", Platforms.WINDOWS),
		WINDOWS_XP ("izpack.windowsinstall.xp", Platforms.WINDOWS_XP),
		WINDOWS_2003 ("izpack.windowsinstall.2003", Platforms.WINDOWS_2003),
		WINDOWS_VISTA ("izpack.windowsinstall.vista", Platforms.WINDOWS_VISTA),
		WINDOWS_7 ("izpack.windowsinstall.7", Platforms.WINDOWS_7),
		LINUX ("izpack.linuxinstall", Platforms.LINUX),
		SUNOS ("izpack.solarisinstall", Platforms.SUNOS),
		MAC ("izpack.macinstall", Platforms.MAC),
		MAC_OSX ("izpack.macinstall.osx", Platforms.MAC_OSX),
		SUNOS_X86 ("izpack.solarisinstall.x86", Platforms.SUNOS_X86),
		SUNOS_SPARC ("izpack.solarisinstall.sparc", Platforms.SUNOS_SPARC);
		
		private String key;
		private Platform platform;
		
		PlatformConditionSpec(String key, Platform platform){
			this.key = key;
			this.platform = platform;
		}
		
		String getKey(){
			return key;
		}
		
		Platform getPlatform(){
			return platform;
		}
	}
	
	

	/**
	 * Returns a Map containing all pre-defined OS conditions for the specified platform.
	 * 
	 * @param currentPlatform the current OS
	 * @return a map containing the pre-defined OS conditions
	 */
	public static Map<String,Condition> createPlatformConditions(Platform currentPlatform, InstallData installData){
		Map<String,Condition> conditionsMap = new HashMap<String,Condition>();
		for(PlatformConditionSpec spec : PlatformConditionSpec.values()){
			conditionsMap.put(spec.getKey(), createPlatformCondition(spec.getKey(), currentPlatform, spec.getPlatform(), installData));
		}
		return conditionsMap;
	}
	
    /**
     * Creates a condition to determine if the current platform is that specified.
     *
     * @param conditionId the condition identifier
     * @param current     the current platform
     * @param platform    the platform to compare against
     * @param installData the install data
     */
    private static Condition createPlatformCondition(String conditionId, Platform current, Platform platform, InstallData installData)
    {
        final boolean isA = current.isA(platform);
        // create a condition that simply returns the isA value. This condition doesn't need to be serializable
        Condition condition = new Condition()
        {
            @Override
            public boolean isTrue()
            {
                return isA;
            }

            @Override
            public void readFromXML(IXMLElement condition) throws Exception
            {
            }

            @Override
            public void makeXMLData(IXMLElement conditionRoot)
            {
            }
        };
        condition.setInstallData(installData);
        condition.setId(conditionId);
        return condition;
    }


}
