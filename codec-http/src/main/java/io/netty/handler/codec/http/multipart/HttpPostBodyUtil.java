/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.http.multipart;

import io.netty.buffer.ByteBuf;

/**
 * Shared Static object between HttpMessageDecoder, HttpPostRequestDecoder and HttpPostRequestEncoder 在HttpMessageDecoder、HttpPostRequestDecoder和HttpPostRequestEncoder之间共享静态对象
 */
final class HttpPostBodyUtil {

    public static final int chunkSize = 8096;

    /**
     * Default Content-Type in binary form
     */
    public static final String DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";

    /**
     * Default Content-Type in Text form
     */
    public static final String DEFAULT_TEXT_CONTENT_TYPE = "text/plain";

    /**
     * Allowed mechanism for multipart
     * mechanism := "7bit"
                  / "8bit"
                  / "binary"
       Not allowed: "quoted-printable"
                  / "base64"
     */
    public enum TransferEncodingMechanism {
        /**
         * Default encoding
         */
        BIT7("7bit"),
        /**
         * Short lines but not in ASCII - no encoding
         */
        BIT8("8bit"),
        /**
         * Could be long text not in ASCII - no encoding
         */
        BINARY("binary");

        private final String value;

        TransferEncodingMechanism(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private HttpPostBodyUtil() {
    }

    /**
    * This class intends to decrease the CPU in seeking ahead some bytes in
    * HttpPostRequestDecoder 这个类旨在减少在HttpPostRequestDecoder中查找某些字节的CPU占用
    */
    static class SeekAheadOptimize {
        byte[] bytes;
        int readerIndex;
        int pos;
        int origPos;
        int limit;
        ByteBuf buffer;

        /**
         * @param buffer buffer with a backing byte array
         */
        SeekAheadOptimize(ByteBuf buffer) {
            if (!buffer.hasArray()) {
                throw new IllegalArgumentException("buffer hasn't backing byte array");
            }
            this.buffer = buffer;
            bytes = buffer.array();
            readerIndex = buffer.readerIndex();
            origPos = pos = buffer.arrayOffset() + readerIndex;
            limit = buffer.arrayOffset() + buffer.writerIndex();
        }

        /**
        *
        * @param minus this value will be used as (currentPos - minus) to set
        * the current readerIndex in the buffer.
        */
        void setReadPosition(int minus) {
            pos -= minus;
            readerIndex = getReadPosition(pos);
            buffer.readerIndex(readerIndex);
        }

        /**
        *
        * @param index raw index of the array (pos in general)
        * @return the value equivalent of raw index to be used in readerIndex(value)
        */
        int getReadPosition(int index) {
            return index - origPos + readerIndex;
        }
    }

    /**
     * Find the first non whitespace
     * @return the rank of the first non whitespace
     */
    static int findNonWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result ++) {
            if (!Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    /**
     * Find the end of String
     * @return the rank of the end of string
     */
    static int findEndOfString(String sb) {
        int result;
        for (result = sb.length(); result > 0; result --) {
            if (!Character.isWhitespace(sb.charAt(result - 1))) {
                break;
            }
        }
        return result;
    }

}
