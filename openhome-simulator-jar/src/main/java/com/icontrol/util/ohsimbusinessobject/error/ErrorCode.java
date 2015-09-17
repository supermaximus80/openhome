package com.icontrol.util.ohsimbusinessobject.error;


import java.util.HashMap;
import java.util.Map;


public enum ErrorCode {
    USER_NOT_FOUND(10000, "no_such_user", "User not found"),
    FORGOT_PASSWORD_OTHER(10001, "other_forgot_password", "Error while handling forgotten password request"),
    USERNAME_UNAVAILABLE(10002, "username_not_available", "Username is not available"),
    AUTHENTICATION_FAILED(10003, "authentication_failed", "Unable to authenticate successfully"),
    RESET_PASSWORD_OTHER(10004, "reset_password_failed", "Unable to reset password for user"),
    RESET_PASSWORD_UNABLE_TO_SEND_MESSAGE(10005, "reset_password_email_failed", "Unable to send reset password e-mail message"),
    FORGOT_PASSWORD_UNABLE_TO_SEND_MESSAGE(10006, "forgotten_password_email_failed", "Unable to send forgotten password e-mail message"),
    USER_LOG_UNABLE_TO_WRITE(10007, "user_log_unable_to_write", "Unable to write entry to the user log"),
    UNABLE_TO_SEND_MESSAGE(10008, "unable_to_send_message", "Unable to send message"),
    INVITE_USER_OTHER(10009, "other_invite_user", "Error while handling invite user request"),
    USER_UNABLE_TO_ADD_USER(2072, "user_unable_to_add_user", "Unable to add a user"),
    USER_ATTEMPT_TO_PERSIST_INCOMPLETELY_SPECIFIED_USER(10010, "attempt_to_persist_incompletely_specified_user", "An attempt was made to persist a user that did not have all required data"),
    USER_INVALID_USERNAME(10011, "invalid_username", "Invalid username"),
    USER_USERNAME_UNAVAILABLE(10012, "username_unavailable", "Username specified is unavailable"),
    USER_ATTEMPT_TO_PERSIST_INCOMPLETELY_SPECIFIED_PASSWORD_QUESTION(10013, "attempt_to_persist_invalid_password_question", "An attempt was made to persist a password question that did not have all required data"),
    USER_ATTEMPT_TO_AUTHENTICATE_WITH_UNSAVED_PASSWORD_QUESTION(10014, "attempt_to_authenticate_with_unsaved_pq", "An attempt was made to authenticate a user using a password question that was not yet persisted"),
    USER_CANNOT_AUTHENTICATE_NON_ACTIVE_USER(10015, "user_not_active_authentication", "Authentication failed -- the user is not active"),
    USER_NOT_ENOUGH_PASSWORD_QUESTIONS(10016, "user_not_enough_password_questions", "Not enough password questions"),
    USER_IS_LOCKED(10017, "user_is_locked", "User is locked"),
    USER_UNABLE_TO_UPDATE_PASSWORD_QUESTIONS_GENERAL(10018, "user_unable_to_update_pass_quest_general", "Unable to update password questions"),
    OKAY(10004, "okay", "No error"),
    USER_FORGOTTEN_USERNAME_OTHER(10019, "user_forgotten_username_other", "An unexpected error was encountered while performing the forgotten username request"),
    USER_FORGOT_PASSWORD_OTHER(10020, "user_forgot_password_other", "An unexpected error was encountered while performing the forgot password request"),
    USER_UNABLE_TO_GET_SECURITY_DATA(10021, "user_unable_to_get_security_data", "Unable to get security data for the user"),
    USER_UNABLE_TO_DELETE_USER_IS_OWNER(10022, "user_is_owner_unable_to_delete", "The user cannot be deleted because it is the owner one or more sites"),
    USER_UNABLE_TO_UNLOCK(10023, "user_unable_to_unlock", "An error occurred while attempting to unlock the user"),
    USER_MAX_FORGOT_PASSWORD_REQUESTS_EXCEEDED(10024, "user_max_forgot_password_attempts_exceeded", "The maximum number of forgotten password attempts has been exceeded.  Please call customer support or wait one day before trying again."),
    USER_MAX_FORGOT_USERNAME_REQUESTS_EXCEEDED(10025, "user_max_forgot_username_attempts_exceeded", "The maximum number of forgotten username attempts has been exceeded.  Please call customer support or wait one day before trying again."),
    USER_CANNOT_AUTHENTICATE_NON_LOCAL_USER(10026, "user_not_local_authentication", "Cannot perform local authentication on a non-local user"),
    USER_USERNAME_EMAIL_NOT_EQUAL(10027, "user_unequal_username_email", "The username and email address must match"),
    USER_TEMP_TOKEN_LIMIT_EXCEEDED(10028, "user_temp_token_limit_exceeded", "The user has exceeded the maximum number of temporary tokens in the configured time period"),
    USER_NON_LOCAL_CANNOT_SET_PASSWORD(10029, "user_non_local_cannot_set_password", "A user with non-local authentication privileges cannot set a local account password"),

    //persistence messages start at 20,000
    DB_PERSISTENCE_ERROR(20000, "db_persistence_error", "Error while reading/writing to the DB"),

    //secure tokens, 30,000
    SECURE_TOKEN_INVALID_LIFETIME(30000, "secure_token_invalid_lifetime", "Cannot create a secure token because the specified lifetime is invalid"),

    //network, 40,000
    NETWORK_INVALID_NETWORK_GROUP(40000, "invalid_network_group_specified", "Invalid network group specified"),
    NETWORK_INVALID_NETWORK_CLUSTER(40001, "invalid_cluster_for_network", "Invalid network cluster or no network cluster specified"),
    NETWORK_NO_CURRENT_CLUSTER(40002, "network_no_current_cluster", "No cluster configured for adding new networks"),
    NETWORK_INVALID_NETWORK_OWNER(40003, "network_invalid_owner", "Invalid or nonexistant owner specified"),

    //prefs, 50,000
    PREF_ATTEMPT_TO_SAVE_BEFORE_ENTITY_SAVED(50000, "save_before_entity_save", "A pref cannot be saved before the associated entity is saved"),
    PREF_NOT_FOUND(50001, "pref_not_found", "The specified pref does not exist"),

    //groups, 60,000
    GROUP_ATTEMPT_TO_DELETE_NONEMPTY(60001, "delete_nonempty_group", "A group must be empty before it can be deleted"),

    //partner 70,000
    PARTNER_NOT_FOUND(70001, "partner_not_found", "The partner was not found"),
    PARTNER_QUERY_FACTORY_NOT_FOUND(700002, "query_factory_not_found", "The requested query factory was not found"),

    //reports 80,000
    REPORTS_NO_FILE_DATA(80001, "error.reports.no.file.data", "No file data provided"),
    REPORTS_ILLEGAL_FILENAME(80002, "error.reports.illegal.filename", "Illegal filename provided"),
    REPORTS_FILE_UNAVAILABLE(80003, "error.reports.file.unavailable", "Report file unavailable"),
    REPORTS_FILE_CANNOT_SAVE(80004, "error.reports.file.cannot.save", "Cannot save the report file"),
    REPORTS_REPORT_UPLOAD_SUCCESS(80005, "result.report.upload.success", "Report uploaded successfully"),
    REPORTS_REPORT_DELETE_SUCCESS(80006, "result.report.delete.success", "Report deleted successfully"),
    REPORTS_VALIDATION_ERROR(80007, "result.report.validation.error", "Error in validating report: ${0}"),

    //general errors start at 1,000,000
    ATTEMPT_TO_MODIFY_IMMUTABLE(1000000, "attempt_to_modify_immutable", "An attempt was made to modify an immutable entity"),
    ATTEMPT_TO_MODIFY_UNPERSISTED(1000001, "attempt_to_modify_unpersisted", "An attempt was made to modify an entity that must be persisted first"),
    ATTEMPT_TO_DELETE_UNPERSISTED(1000002, "attempt_to_delete_unpersisted", "An attempt to delete an entity was made but that entity was never persisted"),
    ILLEGAL_NULL_ENTITY(1000003, "illegal_null_entity", "Null was passed for an entity value that cannot be null");

    private static Map<Integer, ErrorCode> idToUserEvent = new HashMap<Integer, ErrorCode>();

    static {
        for (ErrorCode userEvent : ErrorCode.values()) {
            idToUserEvent.put(userEvent.getID(), userEvent);
        }
    }

    private int id;
    private String name;
    private String description;

    ErrorCode(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static ErrorCode getByID(int id) {
        return idToUserEvent.get(id);
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
