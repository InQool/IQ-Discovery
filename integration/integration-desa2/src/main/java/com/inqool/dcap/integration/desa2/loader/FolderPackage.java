/*
 * FolderPackage.java
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

package com.inqool.dcap.integration.desa2.loader;

import com.inqool.dcap.exception.ExWrapper;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

/**
 * Provides access to a SIP package accessible as a single folder.
 *
 * @author Matus Zamborsky (inQool)
 */
public class FolderPackage implements Package {
    private final String folderPath;

    public FolderPackage(String folderPath) {
        if(!folderPath.endsWith("/") || !folderPath.endsWith("\\")) {
            folderPath += "/";
        }
        this.folderPath = folderPath;
    }

    @Override
    public Optional<InputStream> getFile(final String filePath) {
        return Optional.of(filePath)
                .map(path -> folderPath + path)
                .map(ExWrapper.apply(FileInputStream::new));
    }
}
