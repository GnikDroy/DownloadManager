/* 
 * The MIT License
 *
 * Copyright 2018 gnik.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.downloadmanager;


/**
 * This object represents a part of download file.
 * It contains a startByte and endByte.
 * @author gnik
 */
public class Part{
    long startByte;
    long endByte;
    
    public Part(long startByte,long endByte){
        this.startByte=startByte;
        this.endByte=endByte;
    }

    public long getStartByte() {
        return startByte;
    }

    public long getEndByte() {
        return endByte;
    }

    public void setStartByte(long startByte) {
        this.startByte = startByte;
    }

    public void setEndByte(long endByte) {
        this.endByte = endByte;
    }
    
    @Override
    public String toString(){
        return String.valueOf(startByte)+"-"+String.valueOf(endByte);
    }
    
}
