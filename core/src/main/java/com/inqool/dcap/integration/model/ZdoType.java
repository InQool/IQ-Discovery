/*
 * ZdoType.java
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

package com.inqool.dcap.integration.model;

public enum ZdoType {
    periodical,
    volume,
    issue,
    monograph,
    chapter,
    page,
    throwAway,
    cho,
    bornDigital,
    binary,
    spine;  //helper type, added just because we need to recognize if spine comes before cover page so that we don't set spine as a book's thumbnail

    public static final String PERIODICAL = "periodical";
    public static final String VOLUME = "volume";
    public static final String ISSUE = "issue";
    public static final String MONOGRAPH = "monograph";
    public static final String CHAPTER = "chapter";
    public static final String PAGE = "page";
    public static final String THROW_AWAY = "throwAway";
    public static final String CHO = "cho";
    public static final String BORNDIGITAL = "bornDigital";
    public static final String BINARY = "binary";

    public static boolean isRootCategory(String type) {
        switch(type) {
            case("periodical"):
            case("monograph"):
            case("cho"):
            case("bornDigital"):
                return true;
            case("volume"):
            case("issue"):
            default:
                return false;
        }
    }

    public static boolean isBranchEndCategory(String type) {
        switch(type) {
            case("issue"):
            case("monograph"):
            case("cho"):
            case("bornDigital"):
                return true;
            case("periodical"):
            case("volume"):
            default:
                return false;
        }
    }

    public static boolean isAbovePageCategory(String type) {
        switch(type) {
            case("periodical"):
            case("volume"):
            case("issue"):
            case("monograph"):
            case("cho"):
            case("bornDigital"):
                return true;
            default:
                return false;
        }
    }
}
