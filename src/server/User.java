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

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Group;
import server.GroupUser;
import server.dao.DAOFactory;
import server.dao.GroupDAO;
import server.exceptions.CinnamonException;
import server.global.ConfThreadLocal;
import server.global.Constants;
import server.i18n.Language;
import server.i18n.UiLanguage;
import utils.HibernateSession;
import utils.ParamParser;
import utils.security.HashMaker;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "users",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})}
)
public class User implements Serializable {

    private transient Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    static DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);

    private transient Element xmlNode = null;
    private transient Boolean userIsSuperuser = null;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Column(name = "name",
            length = server.global.Constants.NAME_LENGTH,
            nullable = false)
    private String name;

    @Column(name = "pwd",
            length = 255,
            nullable = false)
    private String pwd;

    @Column(name = "fullname",
            length = server.global.Constants.NAME_LENGTH,
            nullable = false)
    private String fullname;

    @Column(name = "description",
            length = server.global.Constants.DESCRIPTION_SIZE,
            nullable = false)
    private String description;

    @Column(name = "activated",
            nullable = false)
    private Boolean activated = true;

    @Column(name = "sudoer",
            nullable = false)
    private Boolean sudoer = false;

    @Column(name = "sudoable",
            nullable = false)
    private Boolean sudoable = false;

    //-------------- needed for Springsecurity plugin ----
    @Column(name = "account_expired",
            nullable = false)
    private Boolean accountExpired = false;

    @Column(name = "account_locked",
            nullable = false)
    private Boolean accountLocked = false;

    @Column(name = "password_expired",
            nullable = false)
    private Boolean passwordExpired = false;
    //----------------------------------------------------

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "token_age",
            nullable = false)
    private Date tokenAge = new Date(0L);

    @Column(name = "tokens_today",
            nullable = false)
    private Integer tokensToday = 0;

    @Column(name = "token",
            nullable = false,
            length = 128)
    private String token = UUID.randomUUID().toString();

    @Column(name = "email",
            nullable = true,
            length = 255
    )
    private String email;

    @ManyToOne
    @JoinColumn(name = "ui_language_id",
            nullable = true)
    private UiLanguage language;

    @Version
    @Column(name = "obj_version")
    @SuppressWarnings("unused")
    private Long obj_version = 0L;

    @OneToMany(
            mappedBy = "user",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE}
    )
    private Set<GroupUser> groupUsers = new HashSet<GroupUser>();

    public User() {
    }

    public User(Map<String, String> cmd) {
        setName(cmd.get("name"));
        setPwd(cmd.get("pwd"));
        fullname = cmd.get("fullname");
        description = cmd.get("description");
        email = cmd.get("email");
        language = UiLanguage.getDefaultLanguage();
        if (cmd.containsKey("sudoable")) {
            sudoable = cmd.get("sudoable").equals("true");
        }
        if (cmd.containsKey("sudoer")) {
            sudoer = cmd.get("sudoer").equals("true");
        }
    }

    public User(String name, String pwd, String fullname, String description) {
        setName(name);
        setPwd(pwd);
        this.fullname = fullname;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    private void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getName() {
        // commented out: we should prevent an empty name from ever getting set, not
        // throwing an error once it happens to be null.
//        if(name == null || name.trim().length() == 0){
//            throw new CinnamonException("error.param.name");
//        }
        return name;
    }

    /**
     * Set the name of this user. Must not be null or consist only of whitespace, else a CinnamonException is thrown.
     *
     * @param name the name of the user
     */
    public void setName(String name) {
        if (name == null || name.trim().length() == 0) {
            throw new CinnamonException("error.param.name");
        }
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    /**
     * Hash the given password and store it in user.pwd.
     *
     * @param unencrypted_password the unencrypted password
     */
    public void setPwd(String unencrypted_password) {
        if (ConfThreadLocal.getConf().getField("encryptPasswords", "false").equals("true")) {
            log.debug("store password in encrypted form");
            this.pwd = HashMaker.createDigest(unencrypted_password);
        } else {
            this.pwd = unencrypted_password;
        }
    }

    /**
     * Set user.pwd to the value of an already encrypted/hashed password.
     * <br/>
     * This method is needed by the AdminTool (Dandelion), which uses Grails and
     * sets all params at once, which does not play well with setPwd(), as it may
     * hash a password for a second time.
     *
     * @param pwd the new password
     */
    public void updatePassword(String pwd) {
        this.pwd = pwd;
    }

    public Boolean verifySuperuserStatus(EntityManager em) {
        if (userIsSuperuser != null) {
            return userIsSuperuser;
        }
        GroupDAO groupDao = daoFactory.getGroupDAO(em);
        Group adminGroup = groupDao.findByName(Constants.GROUP_SUPERUSERS);
        if (adminGroup == null) {
            log.debug("Superuser-Group was not found.");
            return false;
        }
        Set<GroupUser> adminGroupUsers = new HashSet<GroupUser>();
        adminGroupUsers.addAll(adminGroup.getGroupUsers());
        // debug code:
//		log.debug("groupUsers for admin-group: "+adminGroupUsers);
//		log.debug("groupUsers for user: "+getGroupUsers());
//		for(GroupUser gu : adminGroupUsers){
//            if(getGroupUsers().contains(gu)){
//                log.debug(getGroupUsers() + " contains "+gu);
//            }
//            else{
//                log.debug(getGroupUsers() + " does not contain "+gu);
//            }
//        }
        userIsSuperuser = adminGroupUsers.removeAll(getGroupUsers());
        log.debug("superuserStatus: " + getName() + " == " + userIsSuperuser);
        log.debug("adminGroupUsers: " + adminGroupUsers);
        return userIsSuperuser;
    }

    @Transient
    public Set<GroupUser> getGroupUsers() {
        return groupUsers;
    }

    public void setGroupUsers(Set<GroupUser> groupUsers) {
        this.groupUsers = groupUsers;
    }

    /**
     * @return the activated
     */

    public Boolean getActivated() {
        return activated;
    }

    /**
     * @param activated the activated to set
     */
    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    /**
     * Add the User's fields as child-elements to a new element with the given name.
     * If the user is null, simply return an empty element.
     *
     * @param elementName the element to which the serialized user object will be appended.
     * @param user        the user object which will be serialized
     * @return the new dom4j element.
     */
    public static Element asElement(String elementName, User user) {
        Logger log = LoggerFactory.getLogger(User.class);
        EntityManager em = HibernateSession.getLocalEntityManager();
        log.debug("UserAsElement with element " + elementName);

        if (user != null) {
            if (user.xmlNode != null) {
                user.xmlNode.setName(elementName);
                return (Element) ParamParser.parseXml(user.xmlNode.asXML(), null);
            } else {
                Element e = DocumentHelper.createElement(elementName);

                log.debug("user is not null");
                e.addElement("id").addText(String.valueOf(user.getId()));
                e.addElement("name").addText(user.getName());
                e.addElement("fullname").addText(user.getFullname());
                e.addElement("description").addText(user.getDescription());
                e.addElement("activated").addText(String.valueOf(user.getActivated()));
                e.addElement("isSuperuser").addText(user.verifySuperuserStatus(em).toString());
                e.addElement("sudoer").addText(user.isSudoer().toString());
                e.addElement("sudoable").addText(user.isSudoable().toString());
                Element userEmail = e.addElement("email");
                if (user.getEmail() != null) {
                    userEmail.addText(user.getEmail());
                }
                if (user.getLanguage() != null) {
                    user.getLanguage().toXmlElement(e);
                } else {
                    e.addElement("language");
                }
                log.debug("finished adding elements.");
                user.xmlNode = e;
//				log.debug("e::"+e.asXML());
                return (Element) ParamParser.parseXml(e.asXML(), null);
            }
        } else {
            return DocumentHelper.createElement(elementName);
        }
    }

    /**
     * Find all groups that the user is a member of. This includes each groups' ancestors.
     *
     * @return a Set of all Groups the user is a member of - directly or indirectly
     *         via inheritance
     */
    Set<Group> findAllGroups() {
        Set<Group> groups = new HashSet<Group>();
        for (GroupUser gu : getGroupUsers()) {
            groups.add(gu.getGroup());
            groups.addAll(gu.getGroup().findAncestors());

        }
        return groups;
    }

    /**
     * Create a new UUID token and store it in {@link server.User#token} for email validation and password reset.
     *
     * @return the new UUID string token
     * @throws CinnamonException if maxTokensPerDay has been reached.
     */
    public String createToken() {
        /*
           * reset tokens_today if token_age > 24 hours
           * get maxTokens a user may create per day
           * throw exception if too many tokens have already been created
           * create a new token
           */

        // just using Date-86400s would also work...
        Calendar today = Calendar.getInstance();
        Calendar tokenCal = Calendar.getInstance();
        tokenCal.setTime(tokenAge);
        if (!(today.get(Calendar.DAY_OF_MONTH) == (tokenCal.get(Calendar.DAY_OF_MONTH)))) {
            tokensToday = 0;
        } else {
            tokensToday++;
        }

        String maxTokensPerDay = ConfThreadLocal.getConf().getField("maxTokensPerDay", "3");
        Integer maxTokens = Integer.parseInt(maxTokensPerDay);
        if (tokensToday >= maxTokens) { // we start tokensToday with 0, so >= it is.
            throw new CinnamonException("error.too_many_tokens");
        }

        token = UUID.randomUUID().toString();
        return token;
    }

    /**
     * Sets the token field to a random value.
     */
    public void clearToken() {
        token = Math.random() + "::" + Math.random();
    }

    public String getToken() {
        return token;
    }

    /**
     * @return the email address of this user
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public UiLanguage getLanguage() {
        return language;
    }

    public void setLanguage(UiLanguage language) {
        this.language = language;
    }

    public Boolean getAccountExpired() {
        return accountExpired;
    }

    public void setAccountExpired(Boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public Boolean getPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(Boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }

    public Boolean isSudoer() {
        return sudoer;
    }

    public void setSudoer(Boolean sudoer) {
        this.sudoer = sudoer;
    }

    public Boolean isSudoable() {
        return sudoable;
    }

    public void setSudoable(Boolean sudoable) {
        this.sudoable = sudoable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (accountExpired != null ? !accountExpired.equals(user.accountExpired) : user.accountExpired != null)
            return false;
        if (accountLocked != null ? !accountLocked.equals(user.accountLocked) : user.accountLocked != null)
            return false;
        if (activated != null ? !activated.equals(user.activated) : user.activated != null) return false;
        if (sudoer != null ? !sudoer.equals(user.sudoer) : user.sudoer != null) return false;
        if (sudoable != null ? !sudoable.equals(user.sudoable) : user.sudoable != null) return false;
        if (description != null ? !description.equals(user.description) : user.description != null) return false;
        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        if (fullname != null ? !fullname.equals(user.fullname) : user.fullname != null) return false;
        if (language != null ? !language.equals(user.language) : user.language != null) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        if (passwordExpired != null ? !passwordExpired.equals(user.passwordExpired) : user.passwordExpired != null)
            return false;
        if (pwd != null ? !pwd.equals(user.pwd) : user.pwd != null) return false;
        if (token != null ? !token.equals(user.token) : user.token != null) return false;
        if (tokenAge != null ? !tokenAge.equals(user.tokenAge) : user.tokenAge != null) return false;
        if (tokensToday != null ? !tokensToday.equals(user.tokensToday) : user.tokensToday != null) return false;
        if (userIsSuperuser != null ? !userIsSuperuser.equals(user.userIsSuperuser) : user.userIsSuperuser != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String toString() {
        return "User #" + id + " " + name;
    }
}
