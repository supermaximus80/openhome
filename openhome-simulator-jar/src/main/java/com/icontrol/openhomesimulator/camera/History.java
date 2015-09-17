package com.icontrol.openhomesimulator.camera;

import com.icontrol.openhome.data.*;
import com.icontrol.rest.framework.RestClient;
import com.icontrol.rest.framework.RestConstants;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.icontrol.ohsimsolver.Wrappers;
import javax.xml.datatype.DatatypeConfigurationException;
import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class History {

    /*
        static variables and methods
     */
    private static final Logger log = LoggerFactory.getLogger(History.class);

    static Object monitor = new Object();

    static private History historyInst = null;

    static public History getInstance() {
        synchronized(monitor) {
            if (historyInst == null)
                historyInst = new History();
        }
        return historyInst;
    }

    /*
        class variables and methods
     */
    int maxCommandEntries;
    int maxNotificationEntries;
    Queue<CommandEvent> commandQueue;
    Queue<NotifyEvent> notifyQueue;

    private History() {
        maxCommandEntries = 30;
        maxNotificationEntries = 30;
        commandQueue = new ConcurrentLinkedQueue<CommandEvent>();
        notifyQueue =  new ConcurrentLinkedQueue<NotifyEvent>();
    }

    public void add(CommandEvent event) {
        if (commandQueue.size() > maxCommandEntries)
            commandQueue.poll();
        commandQueue.add(event);
    }

    public void add(NotifyEvent event) {
        if (notifyQueue.size() > maxNotificationEntries)
            notifyQueue.poll();
        notifyQueue.add(event);
    }

    public int getMaxCommandEntries() {
        return maxCommandEntries;
    }

    public void setMaxCommandEntries(int maxCommandEntries) {
        this.maxCommandEntries = maxCommandEntries;
    }

    public int getMaxNotificationEntries() {
        return maxNotificationEntries;
    }

    public void setMaxNotificationEntries(int maxNotificationEntries) {
        this.maxNotificationEntries = maxNotificationEntries;
    }

    public String getHistoryList(Long sinceCommand, Long sinceNotification) {
        HistoryList historyList = new HistoryList();
        Iterator<CommandEvent> iter = commandQueue.iterator();
        while (iter.hasNext()) {
            try {
                historyList.getCommandHistoryAndNotificationHistory().add(iter.next().getCommandHistory());
            } catch (DatatypeConfigurationException e) {
                log.error("getHistoryList add Command caught"+e);
            }
        }
        Iterator<NotifyEvent> iterN = notifyQueue.iterator();
        while (iterN.hasNext()) {
            try {
                historyList.getCommandHistoryAndNotificationHistory().add(iterN.next().getNotificationHistory());
            } catch (DatatypeConfigurationException e) {
                log.error("getHistoryList add Command caught"+e);
            }
        }
        // marshall into string
        String ret = "";
        try {
            ret = RestClient.toString(historyList, RestConstants.ContentType.TEXT_XML);    //TODO: xml serialization problem
        } catch (Exception ex) {
            ret = "getHistoryList caught "+ex ;
            log.error(ret);
        }
        return ret;
    }

    public CommandEvent createCommandEvent(String commandURL, Date rxTime, Date execTime, int responseCode) {
        return new CommandEvent(commandURL, rxTime, execTime, responseCode);
    }

    public NotifyEvent createNotifyEvent(String notifyURL, Date rxTime, Date execTime, String xmlBody, int responseCode) {
        return new NotifyEvent(notifyURL, rxTime, execTime, xmlBody, responseCode);
    }

    /*
       inner classes
    */
    public class CommandEvent {
        String commandURL;
        Date rxTime, execTime;
        int responseCode;

        public CommandEvent(String commandURL, Date rxTime, Date execTime, int responseCode) {
            this.commandURL = commandURL;
            this.rxTime = rxTime;
            this.execTime = execTime;
            this.responseCode = responseCode;
        }

        public String getCommandURL() {
            return commandURL;
        }

        public Date getRxTime() {
            return rxTime;
        }

        public Date getExecTime() {
            return execTime;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        public CommandHistory getCommandHistory() throws DatatypeConfigurationException {
            CommandHistory commandHistory = new CommandHistory();
            commandHistory.setCommandURI(Wrappers.createStringCap(commandURL));
            commandHistory.setCommandRxTime(Wrappers.createDateTimeCap(rxTime));
            if (execTime != null)
                commandHistory.setCommandExecTime(Wrappers.createDateTimeCap(execTime));
            commandHistory.setResponseCode(Wrappers.createIntegerCap(responseCode));
            return commandHistory;
        }

    }

    public class NotifyEvent {
        String notifyURL;
        Date rxTime, execTime;
        String xmlBody;
        int responseCode;

        public NotifyEvent(String notifyURL, Date rxTime, Date execTime, String xmlBody, int responseCode) {
            this.notifyURL = notifyURL;
            this.rxTime = rxTime;
            this.execTime = execTime;
            this.xmlBody = xmlBody;
            this.responseCode = responseCode;
        }

        public String getNotifyURL() {
            return notifyURL;
        }

        public Date getRxTime() {
            return rxTime;
        }

        public Date getExecTime() {
            return execTime;
        }

        public String getXmlBody() {
            return xmlBody;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int code) {
            responseCode = code;
        }

        public NotificationHistory getNotificationHistory() throws DatatypeConfigurationException {
            NotificationHistory notificationHistory = new NotificationHistory();
            notificationHistory.setNotificationURI(Wrappers.createStringCap(notifyURL));
            notificationHistory.setNotifyTime(Wrappers.createDateTimeCap(rxTime));
            if (execTime != null)
                notificationHistory.setReceivedResponseTime(Wrappers.createDateTimeCap(execTime));
            notificationHistory.setXmlBody(Wrappers.createStringCap(xmlBody));
            notificationHistory.setResponseCode(Wrappers.createIntegerCap(responseCode));
            return notificationHistory;
        }
    }
}
