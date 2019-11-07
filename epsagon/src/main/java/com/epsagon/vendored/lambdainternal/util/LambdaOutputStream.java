package com.epsagon.vendored.lambdainternal.util;

import java.io.IOException;
import java.io.OutputStream;
import com.epsagon.vendored.lambdainternal.LambdaRuntime;

public class LambdaOutputStream extends OutputStream {
   private final OutputStream inner;

   public LambdaOutputStream(OutputStream var1) {
      this.inner = var1;
   }

   public void write(int var1) throws IOException {
      this.write(new byte[]{(byte)var1});
   }

   public void write(byte[] var1) throws IOException {
      this.write(var1, 0, var1.length);
   }

   public void write(byte[] var1, int var2, int var3) throws IOException {
      if(LambdaRuntime.needsDebugLogs) {
         LambdaRuntime.streamLogsToSlicer(var1, var2, var3);
      }

      this.inner.write(var1, var2, var3);
   }
}
