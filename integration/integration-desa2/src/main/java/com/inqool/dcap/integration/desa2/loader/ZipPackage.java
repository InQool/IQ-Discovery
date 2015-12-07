/*
 * ZipPackage.java
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
 *//*


package com.inqool.dcap.integration.desa2.loader;

import com.inqool.dcap.exception.ExWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.zip.ZipFile;

*/
/**
 * @author Matus Zamborsky (inQool)
 *
 *//*

public class ZipPackage implements Package{
    private final ZipFile zipFile;

    public ZipPackage(final String zipFilePath) throws IOException {
        this.zipFile = new ZipFile(zipFilePath);
    }

    @Override
    public Optional<InputStream> getFile(final String path) {
        return Optional.of(path)
                .map(zipFile::getEntry)
                .map(ExWrapper.apply(zipFile::getInputStream));
    }
}
*/

//this should not be used because of leaks when zipfile is not closed properly