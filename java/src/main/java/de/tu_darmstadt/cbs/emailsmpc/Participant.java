package de.tu_darmstadt.cbs.emailsmpc;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Participant {
    public final String name;
    public final String emailAddress;
    static private Pattern regex = Pattern
            .compile("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$");

    public Participant(String name, String emailAddress) throws IllegalArgumentException {
        if (!Participant.validEmail(emailAddress))
            throw new IllegalArgumentException("Invalid Email Address " + emailAddress);
        this.name = name;
        this.emailAddress = emailAddress;
    }

    @Override
    public String toString() {
        return name + ": " + emailAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Participant))
            return false;
        Participant p = (Participant) o;
        return p.name.equals(name) && p.emailAddress.equals(emailAddress);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + emailAddress.hashCode();
        return result;
    }

    public static boolean validEmail(String email) {
        Matcher m = regex.matcher(email);
        return m.matches();
    }
}
