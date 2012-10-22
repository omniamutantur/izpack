package com.izforge.izpack.util.config;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class XmlFileConfigTaskTest {

	private XmlFileConfigTask task;
	
	@Before
	public void setUp() throws Exception {
		task = new XmlFileConfigTask();
	}

	@Test
	public void testValidateAttributes() {
		File fooFile = new File("foo.bar");
		task.setToFile(fooFile);
		
		//Test valid and invalid combinations of confProps and configFile
		task.setConfigFile(fooFile);
		tryValidation(true, "Unexpected validation fail (only configFile set)");
		task.addProperty("foo", "bar");
		tryValidation(false, "Expected validation to fail (confProp and configFile set)");
		task.setConfigFile(null);
		tryValidation(true, "Unexpected validation fail (only confProp set)");		
	}

	/* NON-JAVADOC
	 * Wrap validateAttributes in try/catch, and fail if exception-is-null does 
	 * not match value of shouldValidate (null exception mean successful validation).
	 */
	private void tryValidation(boolean shouldValidate, String errMessage)
	{
		Exception exception = null;
		try
		{
			task.validateAttributes();
		}
		catch (Exception e)
		{
			exception = e;
		}
		if (!(exception == null == shouldValidate))
		{
			fail(errMessage + (exception == null ? "" : ": " + exception.getMessage()));
		}
	}
}
