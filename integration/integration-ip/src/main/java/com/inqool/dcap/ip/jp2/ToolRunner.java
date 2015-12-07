/*
 * ToolRunner.java
 *
 * Copyright (c) 2014  inQool a.s.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.inqool.dcap.ip.jp2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * This class runs an external tool via command line and buffers stdout and stderr
 * This multi-threaded approach has to be used as otherwise Windows hangs
 * @author wpalmer
 * @author Matus Zamborsky (inQool)
 *
 */
public class ToolRunner {

    private BufferedReader gStdout = null;

    /**
     * Executes a given command line.  Note stdout and stderr will be populated by this method.
     * @param pCommandLine command line to run
     * @return exit code from execution of the command line
     * @throws java.io.IOException error
     */
    public int runCommand(List<String> pCommandLine, String temp) throws IOException {
        //check there are no command line options that are empty
        while(pCommandLine.contains("")) {
            pCommandLine.remove("");
        }

        ProcessBuilder pb = new ProcessBuilder(pCommandLine);
        pb.redirectErrorStream(true);
        pb.environment().put("TEMP", temp);
        //set the working directory to our temporary directory
        //pb.directory(new File(gTempDir));

        //log outputs to file(s) - fixes hangs on windows
        //and logs *all* output (unlike when using IOStreamThread)
        File stdoutFile = File.createTempFile("stdout-log-", ".log");
        stdoutFile.deleteOnExit();
        pb.redirectOutput(stdoutFile);

        //start the executable
        Process proc = pb.start();

        try {
            //wait for process to end before continuing
            proc.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }

        //create a log of the console output
        gStdout = new BufferedReader(new FileReader(stdoutFile));

        return proc.exitValue();
    }

    /**
     * Get stdout buffer
     * @return stdout buffer
     */
    public BufferedReader getStdout() {
        return gStdout;
    }
}
