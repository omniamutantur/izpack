package com.izforge.izpack.util.config;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class ConfigFileTaskTest 
{
	TestConfigFileTask task;

	@Before
	public void setUp() throws Exception 
	{
		task = new TestConfigFileTask();
	}

	@Test
	public void testValidateAttributes() 
	{
		File fooFile = new File("foo.bar");
		File barFile = new File("bar.foo");
		
		//test invalid: no toFile
		assertNotNull("Expected validation to fail: no toFile set", tryValidation());
		
		// test valid: only toFile set
		task.setToFile(fooFile);
		assertNull("Unexpected validation fail: only toFile set", tryValidation());
		assertEquals(fooFile, task.getDestFile());
		//test valid: toFile and targetFile set
		task.setTargetFile(barFile);
		assertNull("Unexpected validation fail: toFile and targetFile set", tryValidation());
		assertEquals(barFile, task.getDestFile());
		//test valid: toFile, targetFile, and srcFile set
		task.setSrcFile(fooFile);
		assertNull("Unexpected validation fail: srcFile set to same as toFile", tryValidation());
		assertNull("srcFile should be null when equal to toFile", task.srcFile);
		
		//test invalid combination of !forceOverwrite and !create
		task.setOverwrite(false);
		task.setCreate(false);
		assertNotNull("Expected validation to fail when overwrite and create are false", tryValidation());

		//test valid combinations of forceOverwrite and/or create
		task.setOverwrite(true);
		task.setCreate(true);
		assertNull("Unexpected validation fail: overwrite and create are true", tryValidation());
		task.setOverwrite(true);
		task.setCreate(false);
		assertNull("Unexpected validation fail: overwrite is true, create is false", tryValidation());
		task.setOverwrite(false);
		task.setCreate(true);
		assertNull("Unexpected validation fail: overwrite is false, create is true", tryValidation());
	}
	
	/* NON-JAVADOC
	 * Wrap validateAttributes in try/catch, and return any exception caught 
	 */
	private Exception tryValidation()
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
		return exception;
	}

	/**
	 * A dummy implementation of ConfigFileTask, for testing purposes. 
	 * 
	 * @author daniela
	 *
	 */
	protected class TestConfigFileTask extends ConfigFileTask
	{

		@Override
		protected void doFileOperation(File srcFile, File patchFile, File destFile) throws Exception 
		{
			// do nothing
		}
		
		/**
		 * Return protected {@code FileCopyTask.destFile} member for testing purposes
		 * 
		 * @return protected {@code FileCopyTask.destFile} member for testing purposes
		 */
		protected File getDestFile()
		{
			return destFile;
		}
	}

}
