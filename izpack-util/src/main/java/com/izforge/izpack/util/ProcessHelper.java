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

package com.izforge.izpack.util;

import static com.izforge.izpack.util.Platform.Name.OS_2;
import static com.izforge.izpack.util.Platform.Name.WINDOWS;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Process helper methods.
 *
 * @author Tim Anderson
 */
public class ProcessHelper
{

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(ProcessHelper.class.getName());

    /**
     * Default java home directory.
     */
    private static final String JAVA_HOME = System.getProperty("java.home");


    /**
     * Returns the command to launch java on the current platform.
     *
     * @return the command. This will be fully qualified if the command is found
     */
    public static String getJavaCommand()
    {
        String executable = "java";
        if (OsVersion.PLATFORM.isA(WINDOWS) || OsVersion.PLATFORM.isA(OS_2))
        {
            executable += ".exe";
        }

        String dir = new File(JAVA_HOME + "/bin").getAbsolutePath();
        File exe = new File(dir, executable);

        if (!exe.exists())
        {
            // if java.home isn't pointing to the correct location, assume java is somewhere on the PATH.
            return executable;
        }
        return exe.getAbsolutePath();
    }
    
    /**
     * Converts a complete command line into a list. Handles quoted and unquoted command
     * line elements in any combination.
     * 
     * @param commandLine a complete command line
     * @return the command elements in a list
     */
    public static List<String> splitCommandLine(String commandLine)
    {
        StringReader in = new StringReader(commandLine.trim());
        int c;
        char delim = ' ';
        boolean readingCommand = false;
        StringBuffer cache = new StringBuffer();
        List<String> command = new ArrayList<String>();
        try
        {
            while ( (c = in.read()) != -1)
            {
                if (readingCommand)
                {
                    if ((char)c == delim) //end of command element, add to list
                    {
                        readingCommand = false;
                        if (cache.length() > 0) 
                        {
                            command.add(cache.toString().trim());
                            cache = new StringBuffer();
                        }
                    }
                    else //still reading command, append to buffer
                    {
                        cache.append((char)c);
                    }
                }
                else //looking for start of next element
                {
                    if ((char)c == '"')
                    {
                        delim = '"';
                        readingCommand = true;
                    }
                    else if (c != ' ') //ignore whitespace between elements
                    {
                        delim = ' ';
                        cache.append((char)c);
                        readingCommand = true;
                    }
                }
            }
            if ( cache.length() > 0 ) //add final non-quoted argument
            {
                command.add(cache.toString().trim());
            }
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Unexpected error processing command " + commandLine, ioe);
        }
        finally
        {
            in.close();
        }
        return command;
    }
    
    /**
     * Executes the specified command in a new process. See {@link #exec(String...)}.
     * 
     * @param commandLine the complete command line to execute
     * @return the process
     * @throws IOException if an I/O error occurs
     */
    public static Process exec(String commandLine) throws IOException
    {
        return exec(splitCommandLine(commandLine));
    }

    /**
     * <p>Executes the specified command in a new process. This is a convenience method to
     * configure an instance of {@link ProcessBuilder} to generate the process.</p>
     * 
     * <p>The resulting process writes all standard error messages to the 
     * standard out stream (see {@link ProcessBuilder#redirectErrorStream(boolean)}), and the 
     * standard input stream cannot be written to (this method closes the output stream
     * {@link Process#getOutputStream()}).</p>
     *
     * @param command the command to execute
     * @return the process
     * @throws IOException if an I/O error occurs
     */
    public static Process exec(String... command) throws IOException
    {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        process.getOutputStream().close();
        LoggingReader reader = new LoggingReader(new InputStreamReader(process.getInputStream()));
        Thread thread = new Thread(reader);
        thread.setDaemon(true);
        thread.start();
        return process;
    }

    /**
     * Executes the specified command in a new process. See {@link #exec(String...)}.
     *
     * @param command the command to execute
     * @return the process
     * @throws IOException if an I/O error occurs
     */
    public static Process exec(List<String> command) throws IOException
    {
        return exec(command.toArray(new String[command.size()]));
    }

    /**
     * Verifies that java can be executed in a separate process.
     *
     * @throws IOException       if java cannot be executed
     * @throws SecurityException if a security manager exists and doesn't allow creation of a process
     */
    public static void tryExecJava() throws IOException
    {
        Process process = exec(getJavaCommand(), "-version");

        try
        {
            // even if it returns an error code, it was at least found
            process.waitFor();
        }
        catch (InterruptedException exception)
        {
            throw new IOException("Unable to create a java subprocess", exception);
        }
    }

    private static class LoggingReader extends RunnableReader
    {

        /**
         * Constructs a {@link LoggingReader}.
         *
         * @param reader the reader to read from
         */
        public LoggingReader(Reader reader)
        {
            super(reader);
        }

        /**
         * Invoked after a line has been read.
         *
         * @param line the line
         */
        @Override
        protected void read(String line)
        {
            logger.fine(line);
        }
    }

}
