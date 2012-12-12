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

import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.User;
import server.dao.DAOFactory;
import server.dao.SessionDAO;
import server.exceptions.CinnamonException;
import server.global.ConfThreadLocal;
import server.i18n.Language;
import server.i18n.UiLanguage;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;


//import org.hibernate.annotations.Cascade;

@Entity
@Table(name = "sessions")
public class Session implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Column(name = "ticket",
            length = 255)
    private String ticket;

    @Column(name = "expires")
    private Date expires = new Date();

    @Column(name = "lifetime")
    private long lifetime;

    @Column(name = "username",
            length = server.global.Constants.NAME_LENGTH)
    private String username;

    @Column(name = "machinename",
            length = server.global.Constants.NAME_LENGTH)
    private String machinename;

    @Column(name = "message",
            length = 65000)
    @Type(type = "text")
    private String message = "";

    @ManyToOne
    @JoinColumn(name = "ui_language_id",
            nullable = true)
    private UiLanguage language;

    @ManyToOne(targetEntity = server.User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Version
    @Column(name = "obj_version")
    @SuppressWarnings("unused")
    private Long obj_version = 0L;

    public Session() {
    }

    public Session(String repository, User user, String machinename, UiLanguage language) {
        long expirationTime = ConfThreadLocal.getConf().getSessionExpirationTime(repository);
        ticket = UUID.randomUUID().toString() + "@" + repository;
        this.user = user;
        this.machinename = machinename;
        this.language = language;
        username = user.getName(); // while we still have direct SQL queries.
        expires.setTime(expires.getTime() + expirationTime); // for testing
    }

    /**
     * Copy a session, but assign a new UUID.
     * @param repository This session's repository's name.
     * @return the copy of the Session, with new UUID and extended expiration time.
     */
    public Session copy(String repository){
        Session session = new Session();
        long expirationTime = ConfThreadLocal.getConf().getSessionExpirationTime(repository);
        session.ticket = UUID.randomUUID().toString()+"@"+repository;
        session.user = user;
        session.machinename = getMachinename();
        session.language = session.getLanguage();
        session.expires.setTime(expires.getTime() + expirationTime );
        return session;
    }

    public long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    private void setId(long id) {
        this.id = id;
    }

    public long getLifetime() {
        return lifetime;
    }

    public void setLifetime(long lifetime) {
        this.lifetime = lifetime;
    }

    public String getMachinename() {
        return machinename;
    }

    public void setMachinename(String machinename) {
        this.machinename = machinename;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public UiLanguage getLanguage() {
        return language;
    }

    public void setLanguage(UiLanguage language) {
        this.language = language;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void checkForExpiration() {
        if (getExpires().getTime() < new Date().getTime()) {
            Logger log = LoggerFactory.getLogger(Session.class);
            log.debug("session is expired.");
            throw new CinnamonException("error.session.expired");
        }
    }

    public void renewSession(Long expirationTime) {
        Long newTime = (new Date()).getTime() + expirationTime;
        setExpires(new Date(newTime));
    }

    static DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);

    public static Session initSession(EntityManager em, String ticket, String repositoryName, String command) {
        Logger log = LoggerFactory.getLogger(Session.class);
        log.debug("Fetching session-Object with em: " + em);

        SessionDAO sessionDAO = daoFactory.getSessionDAO(em);
        Session session;

        log.debug("looking up session by ticket: " + ticket);
        session = sessionDAO.findByTicket(ticket);
        // prevent a user from using an expired session, (unless he wishes to disconnect):
        if(command.equals("disconnect")){
            log.debug("disconnect requested; no need for expiration check");
            return null;
        }
        else{
            if(session == null){
                throw new CinnamonException("error.session.not_found");
            }
            session.checkForExpiration();
            log.debug("after checkForExpiration");
            Long sessionExpirationTime =
                    ConfThreadLocal.getConf().getSessionExpirationTime(repositoryName);
            log.debug("Session is valid");
            session.renewSession(sessionExpirationTime);
            log.debug("Session renewed until " + session.getExpires());
        }
        
        log.debug("retrieved result: " + session + " with ticket " + session.getTicket());

        if (session.getUser() == null) {
            log.debug("Invalid session ticket - session.getUser() is null");
            throw new CinnamonException("error.session.invalid");
        }
    
        return session;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session)) return false;

        Session session = (Session) o;

        if (lifetime != session.lifetime) return false;
        if (expires != null ? !expires.equals(session.expires) : session.expires != null) return false;
        if (language != null ? !language.equals(session.language) : session.language != null) return false;
        if (machinename != null ? !machinename.equals(session.machinename) : session.machinename != null) return false;
        if (message != null ? !message.equals(session.message) : session.message != null) return false;
        if (ticket != null ? !ticket.equals(session.ticket) : session.ticket != null) return false;
        if (user != null ? !user.equals(session.user) : session.user != null) return false;
        if (username != null ? !username.equals(session.username) : session.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ticket != null ? ticket.hashCode() : 0;
    }

    public String toString(){
        return "Session#"+id+" "+username +" "+ticket+" "+expires.toString();
    }
}
