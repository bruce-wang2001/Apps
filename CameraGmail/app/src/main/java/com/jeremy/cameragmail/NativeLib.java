package com.jeremy.cameragmail;

public class NativeLib {

  static {
    System.loadLibrary("account");
  }
  
 
  /**
   * Returns Hello World string
   */
  public native String account();
  public native byte[] password();
  public native byte[] getRawKey();
  public native String email();
}
