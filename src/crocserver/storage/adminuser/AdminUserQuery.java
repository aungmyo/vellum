/*
 * Vellum by Evan Summers under Apache Software License 2.0 from ASF.
 * 
 */
package crocserver.storage.adminuser;

/**
 *
 * @author evan.summers
 */
public enum AdminUserQuery {
    validate,
    insert,
    update_login,
    update_logout,
    update_org,
    update_secret,
    update_cert,
    update_display_name,
    update_display_name_subject_cert,
    exists_username,
    exists_email,
    delete_username,
    find_username,
    find_email,
    list,
}