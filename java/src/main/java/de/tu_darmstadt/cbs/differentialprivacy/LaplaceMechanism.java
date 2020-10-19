package de.tu_darmstadt.cbs.differentialprivacy;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * This class implements the laplacian mechanism
 * @author Tobias Kussel
 */
public class LaplaceMechanism {
  /**
   * Draw from a laplacian distribution
   * @param mu mean of distribution
   * @param b diversity of distribution
   */
  public static double laplace(double mu, double b){
    SecureRandom rand = new SecureRandom();
    double min = -0.5;
    double max = 0.5;
    double uniform = min + rand.nextDouble() * (max - min);
    return mu - b * Math.signum(uniform)*Math.log(1-2*Math.abs(uniform));
  }
  /**
   * Permute a value with the (epsilo, 0) laplacian mechanism
   * @param value clear value to permute
   * @param sensitivity sensitivity of query
   * @param epsilon epsilon parameter of differential privacy
   */
  public static double privatize(double value, double sensitivity, double epsilon){
    return value + LaplaceMechanism.laplace(0,sensitivity/epsilon);
  }
  /**
   * Permute a BigInteger with the (epsilo, 0) laplacian mechanism
   * @param count clear value to permute
   * @param sensitivity sensitivity of query
   * @param epsilon epsilon parameter of differential privacy
   */
  public static BigInteger privatize(BigInteger count, double sensitivity, double epsilon) {
    double noise_draw = LaplaceMechanism.laplace(0, sensitivity/epsilon);
    BigInteger noise;
    if (Math.signum(noise_draw) == -1){
      noise = BigInteger.valueOf((long)Math.floor(noise_draw));
    } else if (Math.signum(noise_draw) == 1){
      noise = BigInteger.valueOf((long)Math.ceil(noise_draw));
    } else {
      return count;
    }
    return count.add(noise);
  }
  public static BigInteger[] privatize(BigInteger[] count, double sensitivity, double epsilon) {
    for (BigInteger v : count){
      v = LaplaceMechanism.privatize(v, sensitivity, epsilon);
    }

    return count;
  }

}
