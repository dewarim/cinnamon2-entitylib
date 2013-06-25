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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.data.ObjectSystemData;
import server.interfaces.IMetasetJoin;
import utils.HibernateSession;

import javax.persistence.*;
import java.io.Serializable;


/**
 * Member is the intermediate class to associate users and groups.
 *
 * @author ingo
 */

@Entity
@Table(name = "osd_metasets")
public class OsdMetaset implements Serializable, IMetasetJoin {

    private transient Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "osd_id",
            nullable = false)
    private ObjectSystemData osd;

    @ManyToOne
    @JoinColumn(name = "metaset_id",            
            nullable = false)
    private Metaset metaset;

    @Version
    @Column(name = "obj_version")
    @SuppressWarnings("unused")
    private Long obj_version = 0L;

    public OsdMetaset() {
    }

    public OsdMetaset(ObjectSystemData osd, Metaset metaset) {
        this.osd = osd;
        this.metaset = metaset;
        osd.getOsdMetasets().add(this);
        metaset.getOsdMetasets().add(this);
    }

    public String toString() {
        return "OsdMetaset: " + id + " OSD: " + osd.getId() + " Metaset: " + metaset.getId();
    }

    public long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    private void setId(long id) {
        this.id = id;
    }

    public ObjectSystemData getOsd() {
        return osd;
    }

    public void setOsd(ObjectSystemData osd) {
        this.osd = osd;
    }

    public Metaset getMetaset() {
        return metaset;
    }

    public void setMetaset(Metaset metaset) {
        this.metaset = metaset;
    }

    public void doDelete() {  
        osd.getOsdMetasets().remove(this);
        metaset.getOsdMetasets().remove(this);
        EntityManager em = HibernateSession.getLocalEntityManager();        
        em.remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OsdMetaset)) return false;

        OsdMetaset that = (OsdMetaset) o;

        if (metaset != null ? !metaset.equals(that.metaset) : that.metaset != null) return false;
        if (osd != null ? !osd.equals(that.osd) : that.osd != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = osd != null ? osd.hashCode() : 0;
        result = 31 * result + (metaset != null ? metaset.hashCode() : 0);
        return result;
    }

}
