package com.epsagon.vendored.lambdainternal.util;

import java.io.IOException;
import java.io.InputStream;

import sun.misc.Unsafe;

public class NativeMemoryAsInputStream extends InputStream {
   private final long nativeStart;
   private final long nativeEnd;
   private long nativeCur;

   public NativeMemoryAsInputStream(long var1, long var3) {
      this.nativeStart = var1;
      this.nativeCur = var1;
      this.nativeEnd = var3;
   }

   public final int available() {
      return (int)(this.nativeEnd - this.nativeCur);
   }

   public final int read() throws IOException {
      if(this.nativeCur >= this.nativeEnd) {
         return -1;
      } else {
         byte var1 = UnsafeUtil.TheUnsafe.getByte(this.nativeCur);
         ++this.nativeCur;
         return var1 & 255;
      }
   }

   public final int read(byte[] var1) {
      return this.uncheckedBoundsRead(var1, 0, var1.length);
   }

   public final int read(byte[] var1, int var2, int var3) {
      if(var1 == null) {
         throw new NullPointerException();
      } else if(var2 >= 0 && var3 >= 0 && var3 <= var1.length - var2) {
         return this.uncheckedBoundsRead(var1, var2, var3);
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   private final int uncheckedBoundsRead(byte[] var1, int var2, int var3) {
      long var4 = this.nativeEnd - this.nativeCur;
      return var4 <= 0L?-1:(var3 == 0?0:this.uncheckedRead(var1, var2, var3, var4));
   }

   private final int uncheckedRead(byte[] var1, int var2, int var3, long var4) {
      int var6 = (int)Math.min((long)var3, var4);
      UnsafeUtil.TheUnsafe.copyMemory((Object)null, this.nativeCur, var1, (long)(Unsafe.ARRAY_BYTE_BASE_OFFSET + var2), (long)var6);
      this.nativeCur += (long)var6;
      return var6;
   }
}
