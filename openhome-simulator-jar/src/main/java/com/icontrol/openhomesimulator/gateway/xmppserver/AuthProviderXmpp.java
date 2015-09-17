package com.icontrol.openhomesimulator.gateway.xmppserver;

import com.icontrol.openhomesimulator.gateway.resources.BootStrapResource;
import org.jivesoftware.openfire.auth.AuthFactory;
import org.jivesoftware.openfire.auth.AuthProvider;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.slf4j.LoggerFactory;


/**
 * AuthProvider implementation.
 */
public class AuthProviderXmpp implements AuthProvider {

    protected static final org.slf4j.Logger log = LoggerFactory.getLogger(AuthProviderXmpp.class);

    /**
     * Constructs a new DefaultAuthProvider.
     */
    public AuthProviderXmpp() {
    }

    @Override
    public void authenticate(String username, String password) throws UnauthorizedException {
        if (username == null || password == null) {
            String error = "Authenticate - Missing one value of (username, password): (" + username + ", " + password + ")";
            log.error(error);
            throw new UnauthorizedException(error);
        }

        username = username.trim().toLowerCase();
        log.debug("Authenticate (via authenticate() 1) - username: " + username);

        String serialNo = null;
        String siteID = null;
        String domain = null;
        String resource = null;

        int score_index = username.indexOf("_");
        serialNo = username.substring(0, score_index);
        if (serialNo == null) {
            String error = "Username missing 'serialNo' in: " + username;
            log.error(error);
            throw new UnauthorizedException(error);
        }

        int at_index = username.indexOf("@");
        if (at_index != -1) {
            siteID = username.substring(score_index + 1, at_index);

            String end = username.substring(at_index + 1);
            int dindex = end.indexOf("/");
            if (dindex != -1) {
                domain = end.substring(0, dindex);
                resource = end.substring(dindex + 1);
            }
        } else {
            siteID = username.substring(score_index + 1);
        }

        // Todo: Check that the specified domain matches the server's domain

        log.debug("serialNo: " + serialNo);
        log.debug("siteId: " + siteID);
        log.debug("domain: " + domain);
        log.debug("resource: " + resource);

        try {
            String gwPassword = getPassword(serialNo, siteID);
            log.debug("Expected password for serialNo: " + gwPassword);
            log.debug("Actual password passed for serialNo: " + password);

            if (!password.equals(gwPassword)) {
                String error = "Failed to get correct password for serialNo: " + serialNo;
                log.error(error);
                throw new UnauthorizedException(error);
            } else {
                log.debug("Correct password for serialNo: " + serialNo);
            }
        } catch (UserNotFoundException unfe) {
            String error = "Failed to authorize username: " + username + " - " + unfe.getMessage();
            log.error(error);
            throw new UnauthorizedException(error);
        }

        // Got this far, so the user must be authorized.
        // notify user of new connection, TODO find a better trigger for detecting new connection
        log.debug("XMPPAuthProvider serialNo=" + serialNo + " siteID=" + siteID + " sharedSecret=" + password + " domain=" + domain);
    }

    @Override
    public void authenticate(String username, String token, String digest) throws UnauthorizedException {
        if (username == null || token == null || digest == null) {
            String error = "Authenticate - Missing one value of (username, token, digest): (" + username + ", " + token + ", " + digest + ")";
            log.error(error);
            throw new UnauthorizedException(error);
        }

        username = username.trim().toLowerCase();
        log.debug("Authenticate (via authenticate() 2) - username: " + username);

        String serialNo = null;
        String domain = null;
        String siteID = null;
        String resource = null;

        int score_index = username.indexOf("_");
        serialNo = username.substring(0, score_index);
        if (serialNo == null) {
            String error = "Username missing 'serialNo' in: " + username;
            log.error(error);
            throw new UnauthorizedException(error);
        }

        int at_index = username.indexOf("@");
        if (at_index != -1) {
            siteID = username.substring(score_index + 1, at_index);

            String end = username.substring(at_index + 1);
            int dindex = end.indexOf("/");
            if (dindex != -1) {
                domain = end.substring(0, dindex);
                resource = end.substring(dindex + 1);
            }
        } else {
            siteID = username.substring(score_index + 1);
        }

        // Todo: Check that the specified domain matches the server's domain

        log.debug("serialNo: " + serialNo);
        log.debug("siteId: " + siteID);
        log.debug("domain: " + domain);
        log.debug("resource: " + resource);

        String password = null;
        try {
            password = getPassword(serialNo, siteID);
            String anticipatedDigest = AuthFactory.createDigest(token, password);

            log.debug("Expected digest for serialNo: " + anticipatedDigest);
            log.debug("Actual digest passed for serialNo: " + digest);

            if (!digest.equalsIgnoreCase(anticipatedDigest)) {
                String error = "Failed to get correct digest for serialNo: " + serialNo;
                log.error(error);
                throw new UnauthorizedException(error);
            } else {
                log.debug("Correct password for serialNo: " + serialNo);
            }
        } catch (UserNotFoundException unfe) {
            String error = "Failed to authorize username: " + username + " - " + unfe.getMessage();
            log.error(error);
            throw new UnauthorizedException(error);
        }

        // Got this far, so the user must be authorized.
        // notify user of new connection TODO find a better trigger for detecting new connection
        log.debug("XMPPAuthProvider serialNo=" + serialNo + " siteID=" + siteID + " sharedSecret=" + password + " domain=" + domain);
    }

    public boolean isPlainSupported() {
        return true;
    }

    public boolean isDigestSupported() {
        return true;
    }

    @Override
    public String getPassword(String username) throws UserNotFoundException {
        if (username == null) {
            String error = "GetPassword - Missing username: " + username;
            log.error(error);
            throw new UserNotFoundException(error);
        }

        username = username.trim().toLowerCase();
        log.debug("Authenticate (via getPassword()) - username: " + username);

        String serialNo = null;
        String domain = null;
        String siteID = null;
        String resource = null;

        int score_index = username.indexOf("_");
        serialNo = username.substring(0, score_index);
        if (serialNo == null) {
            String error = "Username missing 'serialNo' in: " + username;
            log.error(error);
            throw new UserNotFoundException(error);
        }

        int at_index = username.indexOf("@");
        if (at_index != -1) {
            siteID = username.substring(score_index + 1, at_index);

            String end = username.substring(at_index + 1);
            int dindex = end.indexOf("/");
            if (dindex != -1) {
                domain = end.substring(0, dindex);
                resource = end.substring(dindex + 1);
            }
        } else {
            siteID = username.substring(score_index + 1);
        }

        // Todo: Check that the specified domain matches the server's domain

        log.debug("serialNo: " + serialNo);
        log.debug("siteId: " + siteID);
        log.debug("domain: " + domain);
        log.debug("resource: " + resource);

        String pswd = BootStrapResource.getSharedSecretViaSerialNo(serialNo.trim(), siteID.trim());
        return pswd;
    }

    public String getPassword(String serialNo, String siteID) throws UserNotFoundException {
        if (serialNo == null || siteID == null) {
            String error = "Missing serialNo or siteId: serialNo=" + serialNo + ", siteId=" + siteID;
            log.error(error);
            throw null;
        }

        log.debug("serialNo: " + serialNo);
        log.debug("siteId: " + siteID);

        String pswd = BootStrapResource.getSharedSecretViaSerialNo(serialNo.trim(), siteID.trim());
        return pswd;
    }

    public void setPassword(String username, String password) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    public boolean supportsPasswordRetrieval() {
        return true;
    }
}
