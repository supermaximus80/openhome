package com.icontrol.android.ohsimbusinessobject.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
  * stub user cloass
 */
public class User  {

    private static final Logger log = LoggerFactory.getLogger(User.class);

    private String partner;
    private String login;
    private String password;
    private String displayName;
    /*
    private UserPermissions permissions;
    private String originalLogin;
    private String emailAddress;
    private boolean passwordIsDirty = false;
    private long idOfUserRequestingAccess;
    private String guid;
    private String accountGuid;
    private ICAuthType authType = ICAuthType.ICONTROL;
    private long firstAuthTimestamp;
    private long lastAuthTimestamp;
    private Profile profile;
    */

    public User() {
    }

    /*
    public User(long id,
                String partner,
                String login,
                String displayName,
                UserPermissions permissions,
                String emailAddress,
                BOStatus status,
                UserFactory factory) {
        this(id, partner, login, displayName, permissions, emailAddress, null, null, ICAuthType.ICONTROL,
                -1, status.getID(), 0, 0, null, factory);
    }

    public User(long id,
                String partner,
                String login,
                String displayName,
                UserPermissions permissions,
                String emailAddress,
                String guid,
                String accountGuid,
                ICAuthType authType,
                long idOfUserRequestingAccess,
                int status,
                long firstAuthTimestamp,
                long lastAuthTimestamp,
                Document profileXml,
                UserFactory factory) {
        super(id, 0, -1, 0, -1, ICCommonConstants.EMPTY, status, 0, factory);
        setId(id);
        setPartner(partner);
        setLogin(login);
        setDisplayName(displayName);
        this.permissions = permissions;
        setOriginalLogin(login);
        setEmailAddress(emailAddress);
        setGuid(guid);
        setAccountGuid(accountGuid);
        setAuthType(authType != null ? authType : ICAuthType.ICONTROL);
        this.idOfUserRequestingAccess = idOfUserRequestingAccess;
        this.firstAuthTimestamp = firstAuthTimestamp;
        this.lastAuthTimestamp = lastAuthTimestamp;
        setProfileXml(profileXml);
    }
    */

    /*
    public String getGuid() {
        return guid;
    }

    public String getAccountGuid() {
        return accountGuid;
    }

    public ICAuthType getAuthType() {
        return authType;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public void setAccountGuid(String accountGuid) {
        this.accountGuid = accountGuid;
    }

    public void setAuthType(ICAuthType authType) {
        this.authType = authType;
    }
    */

    /*
    @Override
    public UserFactory getFactory() {
        return (UserFactory) super.getFactory();
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public void setLogin(String login) {
        if (login != null) {
            login = login.toLowerCase();
        }
        this.login = login;
    }

    private void setPassword(String password, boolean markAsDirty) {
        this.password = password;
        if (markAsDirty) {
            this.passwordIsDirty = true;
        }
    }

    public void setPassword(String password) {
        setPassword(password, true);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    */

    /*
    public void setDisplayName(String firstName, String lastName) {
        this.displayName = getFactory().getDisplayName(firstName, lastName);
    }

    public void setPermissions(UserPermissions permissions) {
        this.permissions = permissions;
    }

    private void setOriginalLogin(String login) {
        if (login != null) {
            login = login.toLowerCase();
        }
        this.originalLogin = login;
    }

    public void setEmailAddress(String email) {
        if (email != null) {
            email = email.toLowerCase();
        }
        this.emailAddress = email;
    }

    public boolean passwordIsDirty() {
        return passwordIsDirty;
    }

    public void setPasswordIsDirty(boolean isDirty) {
        this.passwordIsDirty = isDirty;
    }
    */


}
