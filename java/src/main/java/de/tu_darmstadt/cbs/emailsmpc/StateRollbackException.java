package de.tu_darmstadt.cbs.emailsmpc;

public class StateRollbackException extends Exception {
  private static final long serialVersionUID = 1749110854330913294L;
  public StateRollbackException(String msg) {
    super(msg);
  }
}
