<%@ page import="javax.management.MBeanServer,
                 javax.management.MBeanServerFactory,
                 javax.management.ObjectName,
                 java.io.ByteArrayOutputStream,
                 java.net.URLDecoder,
                 java.util.*" %>
<%!
    void parseProperties(String querystr, Properties queryMap) {
        // System.out.println("parseProperties querystr:"+querystr);
        StringTokenizer st = new StringTokenizer(querystr, "&");
        while (st.hasMoreTokens()) {
            String nextnv = st.nextToken();
            int ind = nextnv.indexOf("=");
            if (ind != -1) {
                String name = nextnv.substring(0, ind).trim();
                String val = URLDecoder.decode(nextnv.substring(ind + 1).trim());
                // System.out.println("name:"+name+", val:"+val);
                queryMap.setProperty(name, val);
            }
        }
    }

    Properties parseQueryString(HttpServletRequest request) throws Exception {
        String querystr = request.getQueryString();
        Properties queryMap = new Properties();
        if (querystr != null) {
            parseProperties(querystr, queryMap);
        }
        int contentLength = request.getContentLength();
        // System.out.println(" content length:"+contentLength);
        if (contentLength > 0) {
            ServletInputStream inputStream = request.getInputStream();
            byte[] b = new byte[1024];
            int r = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (-1 != (r = inputStream.read(b, 0, b.length))) {
                // System.out.println(" read bytes:"+r+" ["+new String(b, 0, r)+"]");
                baos.write(b, 0, r);
            }
            String bodyAsString = new String(baos.toByteArray(), "UTF-8");
            parseProperties(bodyAsString, queryMap);

            Enumeration enum2 = request.getParameterNames();
            while (enum2.hasMoreElements()) {
                String next = (String) enum2.nextElement();
                String val = request.getParameter(next);
                // System.out.println("next:"+next+" val:"+val);
                queryMap.put(next, val);
            }


            //   int readint = -1;
            //   int times = 5;
            //   while ((times-- > 0) || (-1 != (readint = inputStream.readLine( b, 0, 1024 )))) {
            //       if (readint > 0) {
            //           String line = new String( b, 0, readint );
            //           parseProperties(line, queryMap);
            //       }
            //   }
        }
        return queryMap;
    }

%>