package com.k_int.sql.sql_syntax;

import java.sql.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.StringWriter;

// An object that can have where conditions added to it

/**
 * Title:       Restrictable
 * Version:     $Id: Restrictable.java,v 1.1 2004/10/22 09:24:28 ibbo Exp $
 * Author:      Ian Ibbotson
 * Description: Base class for building SQL Insert statements
 */

public interface Restrictable
{
    public void addCondition(BaseWhereCondition c);
}
