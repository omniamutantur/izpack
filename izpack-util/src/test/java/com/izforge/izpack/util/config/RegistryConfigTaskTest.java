package com.izforge.izpack.util.config;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.config.base.Profile.Section;
import com.izforge.izpack.util.config.base.MultiMap;
import com.izforge.izpack.util.config.base.Reg;
import com.izforge.izpack.util.config.base.Registry;

public class RegistryConfigTaskTest {

	private static File testReg;
	
	private Reg config;
	private RegistryConfigTask task;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
        URL url = RegistryConfigTaskTest.class.getResource("test.reg");
        assertNotNull(url);
        testReg = new File(FileUtil.convertUrlToFilePath(url));
	}

	@Before
	public void setUp() throws Exception
	{
		//reinitialise task and MultiMap config
		task = new RegistryConfigTask();
		MultiMap<String, Section> config = task.readFromFile(testReg);
		assertTrue(config instanceof Reg);
		this.config = (Reg)config;
	}
	
	@Test
	public void testReadFromFile() throws Exception
	{
		verifyTestReg(config);
	}

	@Test
	public void testWriteToFile() throws Exception
	{
		//test write unmodified config to file and read back to verify 
		File out = File.createTempFile("izpack-config-test", ".reg");
		assertTrue(out.exists());
		task.writeToFile(config, out);
		MultiMap<String, Section> configCopy = task.readFromFile(out);
		assertTrue(configCopy instanceof Reg);
		verifyTestReg((Reg)configCopy);
	}
	
	/* NON-JAVADOC
	 * Verify unmodified test.reg
	 */
	private void verifyTestReg(Reg config)
	{
		Section key = config.get("HKEY_CURRENT_USER\\Software\\IZPACK-TEST");
		assertNotNull(key);
		assertEquals("value1", key.get("property1"));
		assertEquals("value1a", key.get("property1a"));
		assertEquals("value2", key.get("property2"));		
		Section subkey1 = config.get("HKEY_CURRENT_USER\\Software\\IZPACK-TEST\\Subkey1");
		assertNotNull(subkey1);
		assertEquals("value3", subkey1.get("property3"));
		assertEquals("value3a", subkey1.get("property3a"));
		assertEquals("value4", subkey1.get("property4"));		
		Section subkeyX = config.get("HKEY_CURRENT_USER\\Software\\IZPACK-TEST\\SubkeyX");
		assertNotNull(subkeyX);
		assertEquals("valueY", subkeyX.get("propertyY"));
		assertEquals("valueZ", subkeyX.get("propertyZ"));
	}
	
	@Test
	public void testSetEntryKey() throws Exception
	{
		String controlKey = "HKEY_FOO"; 
		task.setToKey(controlKey);
		MultiMapConfigEntry entry = new MultiMapConfigEntry();
		
		// test null/empty section/key name processing
		assertEquals(controlKey, task.setEntryKey(entry).getSection());
		entry.setSection("");
		assertEquals(controlKey, task.setEntryKey(entry).getSection());
		
		//test subkey name processing
		entry.setSection("subkey1");
		assertEquals(controlKey + Registry.KEY_SEPARATOR + "subkey1", task.setEntryKey(entry).getSection());
	}
	
	@Test
	public void testValidateRegAttributes() throws Exception
	{
		File fooFile = new File("foo.bar");
		Exception exception = null;
		
		//test reg file/dir mode (no key name)
		task.setToFile(fooFile);
		exception = tryValidation();
		assertNull("Unexpected validation fail (only toFile set): " 
				+ (exception == null ? "" : exception.getMessage()), exception);
		task.setToFile(null);
		task.setTargetFile(null);
		task.setToDir(fooFile);
		assertNull("Unexpected validation fail (only toDir set): "
				+ (exception == null ? "" : exception.getMessage()), exception);
		task.setToDir(null);
		
		//test registry mode (no file attributes)
		task.setToKey("fooKey");
		assertNull("Unexpected validation fail: only toKey set", tryValidation());
		
		//test file/dir/key both set
		task.setToFile(fooFile);
		assertNotNull("Expected validation to fail: toKey and toFile set", tryValidation());
		task.setToFile(null);
		task.setToDir(fooFile);
		assertNotNull("Expected validation to fail: toKey and toDir set", tryValidation());
	}
	
	/* NON-JAVADOC
	 * Wrap validateRegAttributes in try/catch, and return any exception caught 
	 */
	private Exception tryValidation()
	{
		Exception exception = null;
		try
		{
			task.validateRegAttributes();
		}
		catch (Exception e)
		{
			exception = e;
		}
		return exception;
	}

/* TODO: implement in izpack-test
	@Test
	public void testReadFromRegistry() throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testWriteToRegistry() throws Exception
	{
		fail("Not yet implemented"); // TODO
	}	
*/
}
