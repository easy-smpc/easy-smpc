package de.tu_darmstadt.cbs.differentialprivacy;

/**
 * This class provides calculators for composed DP epsilon and deltas
 * @author Tobias Kussel
 */
public class ComposeDP {
  /**
   * Get overall epsilon for advanced k-fold compositions.
   * Calculation after C. Dwork and A. Roth
   * "The Algorithmic Foundation of Differential Privacy", Corollary 3.21
   * @param singleEpsilon Epsilon of individual algorithms
   * @param singleDelta Delta of Individual algorithms
   * @param k Number of compositions
   */
  public static double getKFoldCompleteEpsilon(double singleEpsilon, double singleDelta, int k){
    return Math.sqrt(2*(double)k*Math.log(1/singleDelta))*singleEpsilon + k*singleEpsilon*(Math.exp(singleEpsilon)-1);
  }
  /**
   * Get individual epsilon for simple uniformly composition.
   * Calculation after C. Dwork and A. Roth
   * "The Algorithmic Foundation of Differential Privacy", Theorem 3.16
   * @param overalEpsilon Epsilon of complete algorithms
   * @param k Number of compositions
   */
  public static double getSimpleIndividualEpsilon(double overalEpsilon, int k){
    return overalEpsilon/k;
  }
  /**
   * Get individual delta for simple uniformly composition.
   * Calculation after C. Dwork and A. Roth
   * "The Algorithmic Foundation of Differential Privacy", Theorem 3.16
   * @param overalDelta Delta of complete algorithms
   * @param k Number of compositions
   */
  public static double getSimpleIndividualDelta(double overalDelta, int k){
    return  overalDelta/k;
  }
}
