package com.icontrol.openhomesimulator.util;

/*
 * @author rbitonio
 */

public class AuthenticationInfo {

    public AuthenticationInfo(String username, String password) {
        this.username = username;
        this.password = password;
    }

    String username;
    String password;
    String challengeResponse;

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getChallengeResponse() {
        return this.challengeResponse;
    }

    public void setChallengeResponse(String challengeResponse) {
        this.challengeResponse = challengeResponse;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("AuthInfo: username=");
        buf.append(username);
        buf.append(", password=");
        buf.append(password);
        buf.append(", challengeResponse: ");
        buf.append(challengeResponse);
        return buf.toString();
    }
}
