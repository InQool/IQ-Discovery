/*
 * FailedToParseException.java
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
package com.inqool.dcap.integration.exception;

import java.io.Serializable;

@SuppressWarnings("unused")
public class FailedToParseException extends Exception implements Serializable {
    public FailedToParseException() {
    }

    public FailedToParseException(String message) {
        super(message);
    }

    public FailedToParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedToParseException(Throwable cause) {
        super(cause);
    }

    public FailedToParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
