/*
 * MarcDataType.java
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

package com.inqool.dcap.jena.type;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;

/**
 * @author Matus Zamborsky (inQool)
 */
public class MarcDataType extends BaseDatatype {
    // Singleton
    private static MarcDataType dataType = new MarcDataType() ;
    public static MarcDataType get() { return dataType; }

    /**
     * Constructor.
     *
     */
    public MarcDataType() {
        super("http://inqool.cz/zdo/1.0/MARC");
    }

    @Override
    public Class<?> getJavaClass() { return String.class ; }

    @Override
    public String unparse(Object value)
    {
        return value.toString() ;
    }

    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException
    {
        try {
            return String.valueOf(lexicalForm);
        } catch (NumberFormatException ex)
        {
            throw new DatatypeFormatException(lexicalForm, this, ex.getMessage()) ;
        }
    }

    @Override
    public String toString() { return "Unique universal identification number" ; }
}
