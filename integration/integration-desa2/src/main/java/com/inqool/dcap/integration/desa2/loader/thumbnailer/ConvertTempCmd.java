/*
 * ConvertCmd.java
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

package com.inqool.dcap.integration.desa2.loader.thumbnailer;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Adds support for specifying temp directory.
 *
 * @author Matus Zamborsky (inQool)
 */
@SuppressWarnings("unused")
public class ConvertTempCmd extends org.im4java.core.ConvertCmd {
    private String temp;

    public ConvertTempCmd(String temp) {
        super();
        this.temp = temp;
    }

    public ConvertTempCmd(boolean b, String temp) {
        super(b);
        this.temp = temp;
    }

    private Process startProcess(LinkedList<String> pArgs)
            throws IOException, InterruptedException {

        // if a global or per object search path is set, resolve the
        // the executable

        if (this.getSearchPath() != null) {
            String cmd = pArgs.getFirst();
            cmd = searchForCmd(cmd,this.getSearchPath());
            pArgs.set(0,cmd);
        } else if (getGlobalSearchPath() != null) {
            String cmd = pArgs.getFirst();
            cmd = searchForCmd(cmd, getGlobalSearchPath());
            pArgs.set(0,cmd);
        }
        ProcessBuilder builder = new ProcessBuilder(pArgs);
        builder.environment().put("TEMP", temp);
        return builder.start();
    }
}
