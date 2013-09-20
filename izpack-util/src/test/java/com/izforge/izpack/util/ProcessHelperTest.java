package com.izforge.izpack.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class ProcessHelperTest {

    @Test
    public void testCommandLineToArray() {
        //test sample uninstaller command line
        String commandLine = "\"C:\\Program Files\\Java\\jre6\\bin\\javaw.exe\" -jar \"C:\\Program Files\\IzPack-5.0.0-rc1-SNAPSHOT-CLEAN\\uninstaller\\uninstaller.jar\"";
        List<String> command = ProcessHelper.splitCommandLine(commandLine);
        assertEquals(3, command.size());
        assertEquals("C:\\Program Files\\Java\\jre6\\bin\\javaw.exe", command.get(0));
        assertEquals("-jar", command.get(1));
        assertEquals("C:\\Program Files\\IzPack-5.0.0-rc1-SNAPSHOT-CLEAN\\uninstaller\\uninstaller.jar", command.get(2));
        //test ignore whitespace
        commandLine = "    \"C:\\Program Files\\Java\\jre6\\bin\\javaw.exe\"   -jar    \"C:\\Program Files\\IzPack-5.0.0-rc1-SNAPSHOT-CLEAN\\uninstaller\\uninstaller.jar\"   ";
        command = ProcessHelper.splitCommandLine(commandLine);
        assertEquals(3, command.size());
        assertEquals("C:\\Program Files\\Java\\jre6\\bin\\javaw.exe", command.get(0));
        assertEquals("-jar", command.get(1));
        assertEquals("C:\\Program Files\\IzPack-5.0.0-rc1-SNAPSHOT-CLEAN\\uninstaller\\uninstaller.jar", command.get(2));
        //test start with unquoted element
        commandLine = "javaw.exe -jar \"C:\\Program Files\\IzPack-5.0.0-rc1-SNAPSHOT-CLEAN\\uninstaller\\uninstaller.jar\"";
        command = ProcessHelper.splitCommandLine(commandLine);
        assertEquals(3, command.size());
        assertEquals("javaw.exe", command.get(0));
        assertEquals("-jar", command.get(1));
        assertEquals("C:\\Program Files\\IzPack-5.0.0-rc1-SNAPSHOT-CLEAN\\uninstaller\\uninstaller.jar", command.get(2));
        //test all unquoted elements
        commandLine = "javaw.exe -jar uninstaller.jar";
        command = ProcessHelper.splitCommandLine(commandLine);
        assertEquals(3, command.size());
        assertEquals("javaw.exe", command.get(0));
        assertEquals("-jar", command.get(1));
        assertEquals("uninstaller.jar", command.get(2));
    }
}
