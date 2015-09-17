package com.icontrol.openhomesimulator.gateway.xmppserver;

import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.openfire.user.UserProvider;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Implementation of the UserProvider interface, which read cpe/premise data from database
 * table.<p>
 *
 * This class uses jive's DbConnectionManager to get db access because it could not be
 * wired to use our datasource in xmpp server.
 *
 */
public class UserProviderXmpp implements UserProvider
{
    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(UserProviderXmpp.class);

    private static final String LOAD_USER =
            "SELECT premise_fk FROM cpe WHERE device_id=?";

    private static final String USER_COUNT =
            "SELECT count(*) FROM cpe";

    /**
     *
     * @param username Cpe device id.
     * @return
     * @throws org.jivesoftware.openfire.user.UserNotFoundException
     */
    public User loadUser(String username) throws UserNotFoundException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            logger.debug("Loading user: " + username);

            // check for username used in notification
            if ("0000000000".equals(username)) {
                return new User(username, username, null, null, null);
            }

            // parse username into serialNo and siteID
            String serialNo = null;
            String siteID = null;

            int score_index = username.indexOf("_");
            serialNo = username.substring(0, score_index);
            if (serialNo == null) {
                String error = "Username missing 'serialNo' in: " + username;
                throw new UserNotFoundException(error);
            }
            siteID = username.substring(score_index + 1);
            if (serialNo == null || siteID == null)
                throw new UserNotFoundException("Username missing 'siteID' in: " + username);

            return new User(username, username, null, null, null);
        }
        catch (Exception e) {
            logger.error("Failed to load user: " + e.getMessage());
            throw new UserNotFoundException(e);
        }
    }

    public User createUser(String username, String password, String name, String email)
            throws UserAlreadyExistsException
    {
        throw new UnsupportedOperationException();
    }

    public void deleteUser(String username)
    {
        throw new UnsupportedOperationException();
    }

    public int getUserCount()
    {
        int count = 10;
        return count;
    }

    public Collection<User> getUsers()
    {
    	throw new UnsupportedOperationException();
    }

    public Collection<String> getUsernames()
    {
    	throw new UnsupportedOperationException();
    }

    public Collection<User> getUsers(int startIndex, int numResults)
    {
    	throw new UnsupportedOperationException();
    }

    public void setName(String username, String name) throws UserNotFoundException
    {
    	throw new UnsupportedOperationException();
    }

    public void setEmail(String username, String email) throws UserNotFoundException
    {
        throw new UnsupportedOperationException();
    }

    public void setCreationDate(String username, Date creationDate) throws UserNotFoundException
    {
        throw new UnsupportedOperationException();
    }

    public void setModificationDate(String username, Date modificationDate) throws UserNotFoundException
    {
        throw new UnsupportedOperationException();
    }

    public Set<String> getSearchFields() throws UnsupportedOperationException
    {
        return new LinkedHashSet<String>(Arrays.asList("Username", "Name", "Email"));
    }

    public Collection<User> findUsers(Set<String> fields, String query)
            throws UnsupportedOperationException
    {
    	throw new UnsupportedOperationException();
    }

    public Collection<User> findUsers(Set<String> fields, String query, int startIndex,
            int numResults) throws UnsupportedOperationException
    {
    	throw new UnsupportedOperationException();

    }

    public boolean isReadOnly()
    {
        return true;
    }
}
