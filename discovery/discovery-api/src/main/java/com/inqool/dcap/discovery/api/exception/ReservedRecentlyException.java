/*
 * SparqlException.java
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

package com.inqool.dcap.discovery.api.exception;

/**
 * User: Matus
 * Date: 9.11.2013
 * Time: 20:15
 */
public class ReservedRecentlyException extends Exception {
	public ReservedRecentlyException() {
	}

	public ReservedRecentlyException(String message) {
		super(message);
	}

	public ReservedRecentlyException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReservedRecentlyException(Throwable cause) {
		super(cause);
	}

	public ReservedRecentlyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
