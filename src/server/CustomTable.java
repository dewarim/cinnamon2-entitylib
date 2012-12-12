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

package server;

import server.Acl;
import server.dao.AclDAO;
import server.dao.DAOFactory;
import server.global.Constants;
import utils.HibernateSession;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * The customtables are used in CmnServer to get jdbc-connection URLs for
 * tables and databases with individual customer data.
 * <p/>
 * This Hibernate class exists so that
 * a) the customtables-table is documented.
 * b) Hibernate can create an empty table when used with hibernate.hbm2ddl.auto=update or create.
 *
 * @author Ingo Wiarda
 */

@Entity
@Table(name = "customtables",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})}
)
public class CustomTable implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    static final DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);

    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Column(name = "name",
            length = Constants.NAME_LENGTH,
            nullable = false)
    private String name;

    @Column(name = "connstring",
            length = 512,
            nullable = false)
    private String connstring;

    @ManyToOne
    @JoinColumn(name = "acl_id",
            nullable = false
    )
    private Acl acl;

    @Column(name = "jdbcdriver",
            length = 128,
            nullable = false)
    private String jdbcDriver;

    @Version
    @Column(name = "obj_version")
    @SuppressWarnings("unused")
    private Long obj_version = 0L;

    public CustomTable() {

    }

    public CustomTable(String name, String connstring, String jdbcDriver, Acl acl) {
        this.name = name;
        this.connstring = connstring;
        this.acl = acl;
        this.jdbcDriver = jdbcDriver;
    }

    public CustomTable(Map<String, String> cmd) {
        name = cmd.get("name");
        connstring = cmd.get("connstring");
        jdbcDriver = cmd.get("jdbc_driver");
        AclDAO aclDAO = daoFactory.getAclDAO(HibernateSession.getLocalEntityManager());
        acl = aclDAO.get(new Long(cmd.get("acl_id")));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the connstring
     */
    public String getConnstring() {
        return connstring;
    }

    /**
     * @param connstring the connstring to set
     */
    public void setConnstring(String connstring) {
        this.connstring = connstring;
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

    public long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    private void setId(long id) {
        this.id = id;
    }

    /**
     * @return the jdbcDriver
     */
    public String getJdbcDriver() {
        return jdbcDriver;
    }

    /**
     * @param jdbcDriver the jdbcDriver to set
     */
    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomTable)) return false;

        CustomTable that = (CustomTable) o;

        if (acl != null ? !acl.equals(that.acl) : that.acl != null) return false;
        if (connstring != null ? !connstring.equals(that.connstring) : that.connstring != null) return false;
        if (jdbcDriver != null ? !jdbcDriver.equals(that.jdbcDriver) : that.jdbcDriver != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (connstring != null ? connstring.hashCode() : 0);
        result = 31 * result + (acl != null ? acl.hashCode() : 0);
        result = 31 * result + (jdbcDriver != null ? jdbcDriver.hashCode() : 0);
        return result;
    }
}
