package org.jzkit.client.bsh.commands;

import bsh.*;
import org.jzkit.ServiceDirectory.*;



public class jzTidy {

  public jzTidy() {
  }

  public static void invoke( Interpreter env, CallStack callstack ) {
    System.gc();
    env.println("Tidy complete");
  }
}

