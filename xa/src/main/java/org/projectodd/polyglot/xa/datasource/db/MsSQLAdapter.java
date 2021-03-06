/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.projectodd.polyglot.xa.datasource.db;

import java.util.HashMap;
import java.util.Map;


public class MsSQLAdapter extends Adapter {

    public MsSQLAdapter() {
        super( "mssql", "jdbc/jtds", "net.sourceforge.jtds.jdbc.Driver", "net.sourceforge.jtds.jdbcx.JtdsDataSource" );
    }

    @Override
    public String[] getNames() {
        return new String[] {
            "mssql",
            "jdbcmssql",
            "jtds",
        };
    }

    @Override
    public Map<String, String> getPropertiesFor(Map<String, Object> config) {
        Map<String, String> properties = new HashMap<String, String>();

        properties.put( "ServerName", null == config.get( "host" ) ? "localhost" : "" + config.get( "host" ) );
        properties.put( "PortNumber", null == config.get( "port" ) ? "1433" : "" + config.get( "port" ) );
        properties.put( "DatabaseName", "" + config.get( "database" ) );
        properties.put( "User", "" + config.get( "username" ) );
        properties.put( "Password", "" + config.get( "password" ) );

        return properties;
    }

}
