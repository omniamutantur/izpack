package com.izforge.izpack.util.config.base.spi;

import com.izforge.izpack.util.config.base.Config;

public class RegParser extends IniParser {
	
	private static final String COMMENTS = ";";
	private static final String OPERATORS = "=";

	public RegParser() 
	{
		super(OPERATORS, COMMENTS);
	}
	
    public static RegParser newInstance()
    {
        return ServiceFinder.findService(RegParser.class);
    }

    public static RegParser newInstance(Config config)
    {
        RegParser instance = newInstance();

        instance.setConfig(config);

        return instance;
    }
}
