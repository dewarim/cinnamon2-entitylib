// cinnamon - the Open Enterprise CMS project
// Copyright (C) 2007-2009 Horner GmbH (http://www.horner-project.eu)
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
// (or visit: http://www.gnu.org/licenses/lgpl.html)

package server.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Acl;
import server.exceptions.CinnamonConfigurationException;
import server.exceptions.CinnamonException;

import java.sql.Connection;
import java.sql.DriverManager;

public class SqlCustomConn {
	
	private Connection connection;
	private Acl acl;
	
	public SqlCustomConn(String connectionString, String jdbcDriver, Acl acl)
			throws CinnamonException {
		Logger log=LoggerFactory.getLogger(this.getClass());
        log.debug(String.format(
								"trying to create a SQL custom connection with driver '%s' and connectionString '%s'",
								jdbcDriver, connectionString));
		// Initialization of JDBC driver
		try {
			// Note: the connection String could be for a completely different
			// DB...
			// TODO: refactor to have SqlCustomConn configurable via XML config
			// file.
			Class.forName(jdbcDriver).newInstance();
			connection = DriverManager.getConnection(connectionString);
			this.acl = acl;

		} catch (Exception e) {
			throw new CinnamonConfigurationException("Could not create SqlCustomConn: "
					+ e.getMessage(), e);
		}
	}
	
	public Connection getConnection() {
		return connection;
	}

	/**
	 * @return the acl
	 */
	public Acl getAcl() {
		return acl;
	}

	/**
	 * @param acl the acl to set
	 */
	public void setAcl(Acl acl) {
		this.acl = acl;
	}
	
}
