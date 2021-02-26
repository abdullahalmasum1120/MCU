package com.aam.mcu.extra;

// Java code to validate a password

public class PasswordValidator {

    public int LENGTH_ERROR = 121;
    public int SPACE_ERROR = 122;
    public int DIGIT_ERROR = 123;
    public int SPECIAL_CHAR_ERROR = 124;
    public int CAPITAL_LETTER_ERROR = 125;
    public int SMALL_LETTER_ERROR = 126;
    public int NO_ERROR = 127;

    // whether a password is valid or not
    public int isValid(String password)
    {

        // for checking if password length
        // is between 8 and 15
        if (!((password.length() >= 8)
                && (password.length() <= 15))) {
            return LENGTH_ERROR;
        }

        // to check space
        if (password.contains(" ")) {
            return SPACE_ERROR;
        }
        if (true) {
            int count = 0;

            // check digits from 0 to 9
            for (int i = 0; i <= 9; i++) {

                // to convert int to string
                String str1 = Integer.toString(i);

                if (password.contains(str1)) {
                    count = 1;
                }
            }
            if (count == 0) {
                return DIGIT_ERROR;
            }
        }

        // for special characters
        if (!(password.contains("@") || password.contains("#")
                || password.contains("!") || password.contains("~")
                || password.contains("$") || password.contains("%")
                || password.contains("^") || password.contains("&")
                || password.contains("*") || password.contains("(")
                || password.contains(")") || password.contains("-")
                || password.contains("+") || password.contains("/")
                || password.contains(":") || password.contains(".")
                || password.contains(", ") || password.contains("<")
                || password.contains(">") || password.contains("?")
                || password.contains("|"))) {
            return SPECIAL_CHAR_ERROR;
        }

        if (true) {
            int count = 0;

            // checking capital letters
            for (int i = 65; i <= 90; i++) {

                // type casting
                char c = (char)i;

                String str1 = Character.toString(c);
                if (password.contains(str1)) {
                    count = 1;
                }
            }
            if (count == 0) {
                return CAPITAL_LETTER_ERROR;
            }
        }

        if (true) {
            int count = 0;

            // checking small letters
            for (int i = 90; i <= 122; i++) {

                // type casting
                char c = (char)i;
                String str1 = Character.toString(c);

                if (password.contains(str1)) {
                    count = 1;
                }
            }
            if (count == 0) {
                return SMALL_LETTER_ERROR;
            }
        }

        // if all conditions fails
        return NO_ERROR;
    }
}

