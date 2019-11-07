package com.epsagon.vendored.lambdainternal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class LambdaByteArrayOutputStream extends ByteArrayOutputStream {
   public LambdaByteArrayOutputStream() {
   }

   public LambdaByteArrayOutputStream(int var1) {
      super(var1);
   }

   public byte[] getRawBuf() {
      return super.buf;
   }

   public int getValidByteCount() {
      return super.count;
   }

   public void readAll(InputStream var1) throws IOException {
      while(true) {
         int var2 = Math.max(var1.available(), 1024);
         this.ensureSpaceAvailable(var2);
         int var3 = var1.read(this.buf, this.count, var2);
         if(var3 < 0) {
            return;
         }

         this.count += var3;
      }
   }

   private void ensureSpaceAvailable(int var1) {
      if(var1 > 0) {
         int var2 = this.count - this.buf.length;
         if(var2 < var1) {
            int var3 = this.buf.length * 2;
            if(var3 < this.buf.length) {
               var3 = Integer.MAX_VALUE;
            }

            byte[] var4 = new byte[var3];
            System.arraycopy(this.buf, 0, var4, 0, this.count);
            this.buf = var4;
         }

      }
   }
}
