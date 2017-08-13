package org.openbase.bco.bcozy.model;

import rst.domotic.unit.UnitConfigType;

import java.util.List;

/**
 * @author vdasilva
 */
public interface SessionManagerFacade {

    /**
     * Checks, if the current user is an admin.
     *
     * @return true, if the current user is an admin, false otherwise
     */
    boolean isAdmin();

    boolean registerUser(NewUser user, String plainPassword, boolean asAdmin,
                         List<UnitConfigType.UnitConfig> groups);

    /**
     * Checks, if the username is available.
     *
     * @param username the username to check
     * @return false, if the username is already in use, true otherwise
     */
    boolean userNameAvailable(String username);

    /**
     * Validates the given password and compares it with the repeated password.
     *
     * @param password         the password to validate
     * @param repeatedPassword the repeated password
     * @return returns true, if {@code password} is a valid password and matches repeatedPassword
     */
    boolean passwordsValid(String password, String repeatedPassword);

    boolean phoneIsValid(String phoneNumber);

    boolean mailIsValid(String mailAdress);

    class NewUser {
        private final String username;
        private final String firstName;
        private final String lastName;
        private final String mail;
        private final String phone;

        public NewUser(String username, String firstName, String lastName, String mail, String phone) {
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
            this.mail = mail;
            this.phone = phone;
        }

        public String getUsername() {
            return username;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getMail() {
            return mail;
        }

        public String getPhone() {
            return phone;
        }
    }
}