package com.icontrol.android.openhomesimulator.camera;

import com.icontrol.android.openhomesimulator.camera.resources.*;
import com.icontrol.rest.framework.RestService;

public class OpenHomeRestService extends RestService {

    public OpenHomeRestService() {

        super(new String[]{

        },new String[]{
                SystemTimeResource.class.getName(),
                StreamingResource.class.getName(),
                StreamingMediaTunnelResource.class.getName(),
                SystemResource.class.getName(),
                SystemNetworkInterfacesResource.class.getName(),
                SystemVideoInputsResource.class.getName(),
                SystemAudioChannelsResource.class.getName(),
                SecurityResource.class.getName(),
                SystemLoggingResource.class.getName(),
                SystemHistoryResource.class.getName(),
                CustomEventResource.class.getName(),
                CustomMotionDetectionPirResource.class.getName()
        },null);


    }







   /**
    public void enableUserAccess()
    {
        userAccessEnabled = true;
    }

    public void enableInstallerAccess()
    {
        installerAccessEnabled = true;
    }

    public void enableAdminAccess()
    {
        adminAccessEnabled = true;
    }

    public void enableServerAccess()
    {
        serverAccessEnabled = true;
    }

    public void enableGatewayAccess()
    {
        gatewayAccessEnabled = true;
    }



    /**
     * the fact that this method throws excpetion is amazingly annoying to me,
     * but some objects beneath throw exception and I don't want to handle them
     * here
     *
     * @param user
     * @param httpMethod
     * @param path
     * @param request
     * @param response
     * @throws Exception
     */

   /**
    public void route(RequestUser user, String httpMethod, String path,
                      HttpServletRequest request, RestfulResponse response)
            throws Exception {
        // create history event object
        History.CommandEvent event = null;
        if (!(this instanceof GatewayRestImpl))
            event = History.getInstance().createCommandEvent(path, new Date(), new Date(), 0) ;

        try {
            //log.debug("Camera process route method:"+httpMethod+" path:"+path);
            ResolvedPath rp = direct.resolve(httpMethod, path);
            if (rp == null) {
                throw new HttpCodeException(HttpServletResponse.SC_NOT_FOUND, "No such resource");
            }
            if (rp.getResource() instanceof ResourceIf) {
                ResourceIf resource = ((ResourceIf) rp.getResource());
                // ApiRequest session = new HttpApiRequest(request);
                // if (resource.passesGates(user, rp, session)) {
                //if (true)   {
                    resource.invoke(user, rp, request, response);
                //} else {
                //    throw new HttpCodeException(HttpServletResponse.SC_UNAUTHORIZED, "Forbidden");
                //}
                // set event history to be successful
                if (event != null)
                    event.setResponseCode(200);
            } else {
                throw new HttpCodeException(HttpServletResponse.SC_NOT_FOUND, "no such resource");
            }

        } catch (ICUserException e) {
            ErrorCode ec = e.firstErrorCode();
            response.setHeader("X-errorName", response.urlEncode(ec.getName()));
            if (ec == ErrorCode.AUTHENTICATION_FAILED) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ec.getDescription());
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, ec.getDescription());
            }
            // set event history
            event.setResponseCode(ec.getID());
        }

        // update history
        if (event != null)
            History.getInstance().add(event);
    }

    /*
    private AuthResource getAuthResource(String partner) {
        AuthResource authResource = authResourceMap.get(partner);
        if (authResource == null) {
            authResource = new AuthResource(partner);
            authResourceMap.put(partner, authResource);
        }

        return authResource;
    }
    */

    /**
     * Check if access is enabled per the user type.
     *
     * @param userToken Authenticated user token
     * @return true if access is enabled; false otherwise
     */
    /*
    private boolean isAccessEnabled(RequestUser userToken) throws IOException {
        if (RequestUser.isEndUser(userToken.getUser())) {
            return userAccessEnabled;
        } else if (RequestUser.isInstaller(userToken.getUser())) {
            return installerAccessEnabled;
        } else if (RequestUser.isAdmin(userToken.getUser(), 0)) {
            return adminAccessEnabled;
        }

        return false;
    }

    private RequestUser locateUser(AuthResource authResource,
                                  String partner,
                                  String login,
                                  String password,
                                  String authToken,
                                  long expires,
                                  String remoteaddr,
                                  ClientType clientType,
                                  AccessType accessType) throws IOException {
        RequestUser userToken = null;

        // try to locate the user
        User locate = RequestUser.locateUser(partner, login);
        if (locate != null) {
            UserFactory cuf = locate.getFactory();
            RequestContext rc = RequestContext.getNew();
            rc.setAccessType(accessType);
            rc.setClientType(clientType);
            if (remoteaddr != null) {
                rc.setIPAddress(remoteaddr);
            }
            rc.setUserRequestingAccess(locate);
            cuf.setRequestContext(rc);

            User user = null;
            // try to authenticate user with token
            if (login != null && authToken != null && authToken.length() > 0) {
                user = cuf.authenticate(login, authToken, ICPasswordType.CACHED_LOGIN);
                if (user == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Failed to authentication token: login=%s, token=%s",
                            login, authToken));
                    }
                } else {
                    SecureToken token = SecureTokenFactory.getInstance(partner).get(user.getId(), authToken);
                    if (token != null) {
                        expires = token.expires();
                    } else {
                        expires = -1;
                    }
                }
            }

            // try to authenticate user with password
            if (user == null && login != null && password != null) {
                // reset authToken
                authToken = null;

                switch (locate.getAuthType()) {
                case ICONTROL:
                    user = cuf.authenticate(login, password);
                    break;
                case SSO:
                    if (log.isDebugEnabled()) {
                        log.debug("Process SAML assertion from value: " + password);
                    }
                    String saml = new String(Base64.decodeBase64(password.getBytes()));
                    if (log.isDebugEnabled()) {
                        log.debug("Process SAML assertion: " + saml);
                    }

                    if (authResource != null) {
                        // try to validate SAML assertion as password
                        LoginToken loginToken = authResource.restTokenFactory.getLoginTokenFromAssertion(saml);
                        if (Token.Status.Type.SUCCESS.equals(loginToken.getStatus().getType())) {
                            if (loginToken.getNameID().equals(locate.getAccountGuid())) {
                                user = locate;
                            } else {
                                log.error("User login does not match token NameID");
                            }
                        }
                    } else {
                        log.error("No valid SSO authentication resource");
                    }
                    break;
                }

                if (user != null) {
                    // user has been authenticated
                    if (expires > 0) {
                        // generate new token if expiration time is specified
                        SecureToken secureToken = cuf.generateSecureToken(user, expires);

                        // update local "authToken" and "expires" value
                        authToken = secureToken.token();
                        expires = secureToken.expires();
                    }
                }
            }

            if (user != null) {
                userToken = new RequestUser(user, authToken, expires);
                if (authToken != null) {
                    // cache userToken if authToken is valid
                    userTokenCache.put(authToken, userToken);
                }
            }
        }

        return userToken;
    }
    */

    /*
    public RequestUser getRequestUser(String partner, 
                                  String login, 
                                  String password, 
                                  String authToken, 
                                  long expires,
                                  String remoteaddr,
                                  Cookie[] cookies,
                                  ClientType clientType,
                                  AccessType accessType) throws IOException {
        RequestUser userToken = null;
        if (authToken != null) {
            userToken = userTokenCache.get(authToken);

            // make sure the token is still valid
            if (userToken != null && userToken.getExpires() <= System.currentTimeMillis()) {
                // remove userToken from cache if it has expired and force re-authentication
                userTokenCache.remove(authToken);
                userToken = null;
            }
        }

        if (userToken == null) {
            // check for secure context
            AuthResource authResource = null;
            if (Preferences.get("com.icontrol.partner."+partner+".sso.enabled", false)) {
                authResource = getAuthResource(partner);
            }
            if (login == null && authResource != null && authResource.contextValidator != null) {
                SessionToken sessionToken = authResource.contextValidator.validate(cookies);
                if (sessionToken != null && sessionToken.getUsername().equals(login)) {
                    login = sessionToken.getUsername();
                    authToken = sessionToken.getPassword();
                    expires = sessionToken.getExpires();
                }
            }

            if (login != null && login.length() > 0) {
                userToken = locateUser(authResource, partner, login, password, authToken,expires,
                        remoteaddr, clientType, accessType);
            }
        }

        // make sure user token exists and the associated partner is indeed the partner in the request
        if (userToken != null && userToken.getUser().getPartner().equals(partner) && userToken.getUser().getLogin().equalsIgnoreCase(login) && isAccessEnabled(userToken)) {
            return userToken;
        } else {
            return null;
        }
    }
    */


    /*
    public RequestUser getInternalUser(String siteid, String login, String password) throws IOException {
        RequestUser userToken = null;
        if (siteid != null && siteid.length() > 0 && gatewayAccessEnabled) {
            if (log.isDebugEnabled()) {
                log.debug("gateway request from:"+siteid);
            }
            userToken = new RequestUser(siteid);
        } else if (serverAccessEnabled) {
            String serverLogin = Preferences.get("com.icontrol.ohsimrest.server.login");
            String serverPass = Preferences.get("com.icontrol.ohsimrest.server.password");
            if (login != null && password != null
                    && login.equals(serverLogin) && password.equals(serverPass)) {
                userToken = new RequestUser();
                userToken.setType(RequestUser.UserType.SERVER);
            }
        }

        return userToken;
    }
    */

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     */
   /** public void service(HttpServletRequest request,
                           HttpServletResponse response) throws ServletException, IOException {
        //request.getSession(); // make sure there is a session created before writing any response
        //request = MutatableHttpServletRequest.newInstance(request);
        RestfulResponse rr = new PureRestfulResponse(null, this, null, request, response);
        /*
        String format = request.getHeader(HttpApiRequest.PARAM_X_FORMAT);
        if (format != null && format.equals("xml")) {
            rr = new XmlRestfulResponse(null, this, null, request, response);
        } else {
            rr = new PureRestfulResponse(null, this, null, request, response);
        }


        try {
            // rr.setHeader(HttpApiRequest.PARAM_X_VERSION, "3.5");
            rr.setHeader(HttpApiRequest.PARAM_CACHE_CONTROL, "no-cache");
            /*
            String login = request.getHeader(HttpApiRequest.PARAM_X_LOGIN);
            String password = request.getHeader(HttpApiRequest.PARAM_X_PASSWORD);
            String authToken = request.getHeader(HttpApiRequest.PARAM_X_TOKEN);
            String expiresStr = request.getHeader(HttpApiRequest.PARAM_X_EXPIRES);
            long expires = (expiresStr != null) ? Long.parseLong(expiresStr) : 0;
            String requestID = request.getHeader(HttpApiRequest.PARAM_X_REQUEST_ID);
            String uagent = request.getHeader(HttpApiRequest.PARAM_USER_AGENT);

            if (log.isDebugEnabled()) {
                log.debug("User-Agent:" + uagent);
            }
            */

            // today iphone is sending: iphone-client
            // will be sending iphone-client/3.5
            /*
            boolean isiphone = false;
            if (uagent != null && (uagent.contains("iphone-client") || uagent.toLowerCase().startsWith(Preferences.get("com.icontrol.ohsimrest.iphone.uagenttoken", "iphone")))) {
                String[] incompatiblePrefixes = Preferences.getArray("com.icontrol.ohsimrest.iphone.incompatibleprefixes");
                if (incompatiblePrefixes == null || incompatiblePrefixes.length == 0) {
                    incompatiblePrefixes = new String[] {"iphone-client/3.2"};
                }
                for (String incompatiblePrefix : incompatiblePrefixes) {
                    if (uagent.toLowerCase().startsWith(incompatiblePrefix)) {
                        if (log.isDebugEnabled()) {
                            log.debug("sending upgrade command for:" + uagent);
                        }
                        rr.setHeader("Warning", "199 Please upgrade");
                    }
                }
                request.getSession().setAttribute("neediphonesvccheck", "yes");
                isiphone = true;
            }
            */

            /*
            if (requestID != null) {
                rr.setHeader(HttpApiRequest.PARAM_X_REQUEST_ID, rr.urlEncode(requestID));
            }

            ResolvedPath partnerPath = open.resolve(request.getMethod(), request.getPathInfo());
            if (partnerPath != null) {
                partner = partnerPath.getPathVar("partner");
            }

            // check partner
            if (partner == null || partner.length() == 0) {
                throw new HttpCodeException(HttpServletResponse.SC_NOT_FOUND, "No such resource");
            }

            RequestUser userToken = getRequestUser(partner,
                    login, 
                    password, 
                    authToken, 
                    expires,
                    request.getRemoteAddr(),
                    request.getCookies(),
                    isiphone ? ClientType.MOBILE_IPHONE : ClientType.REST,
                    AccessType.INTERACTIVE);

            if (userToken == null) {
                userToken = getInternalUser(request.getHeader("network"), login, password);
            }

            if (userToken != null) {
                if (userToken.getUser() != null) {
                    rr.setHeader(HttpApiRequest.PARAM_X_LOGIN, userToken.getUser().getLogin());
                }
                if (userToken.getToken() != null) {
                    rr.setHeader(HttpApiRequest.PARAM_X_TOKEN, userToken.getToken());
                }
                if (userToken.getExpires() > 0) {
                    rr.setHeader(HttpApiRequest.PARAM_X_EXPIRES, Long.toString(userToken.getExpires()));
                }
                rr.setRequestUser(userToken);
            } else {
                userToken = new RequestUser();
                // throw new HttpCodeException(response.SC_UNAUTHORIZED, "Unauthorized");
            }


            log.debug("Received request. method:"+request.getMethod()+" path:"+request.getPathInfo());

            RequestUser userToken = new RequestUser(new User(), null, 0);
            ResolvedPath rp = direct.resolve(request.getMethod(), request.getPathInfo());
            rr.setResolvedPath(rp);

            Set methods = direct.getMethods(request.getPathInfo());
            String allow = computeAllow(methods);
            rr.setHeader(HttpApiRequest.PARAM_ALLOW, allow);
            if (methods == null || methods.size() == 0) {
                throw new HttpCodeException(HttpServletResponse.SC_NOT_FOUND, "No such resource");
            } else if (!methods.contains(request.getMethod())) {
                throw new HttpCodeException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        "Method not allowed.  Allowed methods " + allow);
            }
            // rr.setRequestUser(userToken);
            this.route(userToken, request.getMethod(), request.getPathInfo(), request, rr);

            /*
            String network = rp.getPathVar("network");
            if (network != null && userToken.getUser() != null) {
                // generate login/logout events for each network accessed.
                // This call will check if one has already been generated for the given network (and not create another)
                RequestContext rc = userToken.getUser().getFactory().getRequestContext();
                new LoginEvent(request,
                        new SourceInfo(userToken.getUser().getLogin(), ""+userToken.getUser().getId(),
                                RequestUser.getSourceInfoUserType(userToken.getUser())),
                        network, partner,
                        rc.getClientType(), rc.getAccessType());

                // generate portal keepalive for each network accessed_
                // This call will check if one has already been generated for the given network (and not create another)
                GatewayPortalKeepalive.getKeepalive(request.getSession(), network, partner);
            }
            */
        /*
        } catch (ICUserException e) {
            ErrorCode ec = e.firstErrorCode();
            log.error("ICUserException "+e.getMessage());
            // response.setHeader(HttpApiRequest.PARAM_X_ERROR_NAME, rr.urlEncode(ec.getName()));
            response.sendError(ec == ErrorCode.AUTHENTICATION_FAILED ?
                    HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN, ec.getDescription());

        } catch (NumberFormatException e) {
            //log.error("NumberFormatException "+e.getMessage());
            rr.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (UnsupportedOperationException e) {
            //log.error("UnsupportedOperationException "+e.getMessage());
            rr.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Operation not supported");
        } catch (HttpCodeException e) {
            String errorString = e.getErrorString();
            /*
            if (errorString != null) {
                rr.setHeader(HttpApiRequest.PARAM_X_ERROR, rr.urlEncode(errorString));
            }

            rr.sendError(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            // catchall Error, IOException, Throwable
            e.printStackTrace();
            rr.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        } finally {
            rr.flush();
            rr.close();
        }
    }

    public ResourceIf getResource(String path) {
        return path2ResourceMap.get(path);
    }

    public ResourceIf getResource(Class theClass) {
        ResourceIf res = null;
        for (ResourceIf r : path2ResourceMap.values()) {
            if (r.getClass().equals(theClass)) {
                if (res != null)
                    throw new IllegalArgumentException("multiple instances of "+theClass+" found");
                res = r;
            }
        }
        return res;
    }

    public String computeAllow(Set set) {
        StringBuffer sb = new StringBuffer();
        if (set == null || set.equals("")) {
            return "";
        }

        for (Iterator i = set.iterator(); i.hasNext();) {
            sb.append(i.next().toString());
            if (i.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Wrapper class to hold partner-specific public key and REST token factory
     */
    /*
    private static class AuthResource {

        private SessionContextValidator contextValidator;
        private RestTokenFactory restTokenFactory;

        private AuthResource(String partner) {
            try {
                // process signing certificate if specified
                String sigCert = Preferences.getFile(Preferences.AUTH_PREFIX + partner + "."
                        + Preferences.AUTH_SIG_CERT);
                if (sigCert != null) {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(
                            new ByteArrayInputStream(sigCert.getBytes()));
                    contextValidator = new SessionContextValidator(partner, cert.getPublicKey());
                } else {
                    contextValidator = null;
                }
            } catch (Exception e) {
                log.error("Error initializing context validator for partner " + partner + e, e);
                contextValidator = null;
            }

            try {
                String factoryClassName = (Preferences.get(Preferences.AUTH_PREFIX +
                        partner + "." + Preferences.AUTH_REST_TOKEN_FACTORY));
                if (factoryClassName != null) {
                    Class<?> factoryClass = Class.forName(factoryClassName);
                    restTokenFactory = (RestTokenFactory) factoryClass.newInstance();
                    SsoConfig configs = new SsoConfig();
                    configs.setRestCert(Preferences.getFile(Preferences.AUTH_PREFIX +
                            partner + "." + Preferences.AUTH_REST_CERT));
                    restTokenFactory.init(configs.entryMap());
                } else {
                    restTokenFactory = null;
                }
            } catch (Exception e) {
                // failed to initialized REST token factory
                log.error("Error initializing REST token factory for partner " + partner + e);
                restTokenFactory = null;
            }

        }

    }
    */

}
