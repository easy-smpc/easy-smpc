package de.tu_darmstadt.cbs.emailsmpc;

import java.math.BigInteger;

import de.tu_darmstadt.cbs.secretshare.ArithmeticShare;
import de.tu_darmstadt.cbs.secretshare.ArithmeticSharing;

/**
 * Hello world!.
 */
public class EmailSMPC {
    
    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        int num_parties = 7;
        BigInteger secret = BigInteger.valueOf(15);
        if (args.length == 1) {
            secret = new BigInteger(args[0]);
        }
        ArithmeticSharing sharer = new ArithmeticSharing(num_parties);
        ArithmeticShare[] shares = sharer.share(secret);
        System.out.println(num_parties + " Parties:");
        for (int i = 0; i != num_parties; i++) {
            System.out.println("Share " + i + ": " + shares[i]);
        }
        System.out.println("The reconstructed secret is: " + ArithmeticSharing.reconstruct(shares) + " and should be: " + secret);
    }
}
