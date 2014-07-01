/** 
 * (C) Copyright 2014 Chiral Behaviors, All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.chiralBehaviors.groo;

import java.beans.ConstructorProperties;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * @author hhildebrand
 * 
 */
public class OperationResult<T> implements Serializable {
    public static String getStackTrace(Throwable e) {
        StringWriter out = new StringWriter();
        PrintWriter s = new PrintWriter(out);
        e.printStackTrace(s);
        s.flush();
        return out.toString();
    }

    private static final long   serialVersionUID = 1L;

    private static final String SUCCESS          = "success";

    private final boolean       success;
    private final T             result;
    private final String        message;
    private final String        stackTrace;

    public OperationResult() {
        this(true, SUCCESS);
    }

    /**
     * @param success
     * @param message
     * @param stackTrace
     */
    @ConstructorProperties({ "success", "result", "message", "stackTrace" })
    public OperationResult(boolean success, T result, String message,
                           String stackTrace) {
        this.success = success;
        this.message = message;
        this.stackTrace = stackTrace;
        this.result = result;
    }

    public OperationResult(T result) {
        this(true, result, "success", (String) null);
    }

    public OperationResult(boolean b, T result, String msg, Throwable e) {
        this(b, result, msg, getStackTrace(e));
    }

    /**
     * @param b
     * @param msg
     */
    public OperationResult(boolean b, String msg) {
        this(b, null, msg, (String) null);
    }

    public OperationResult(String msg, Throwable e) {
        this(false, null, msg, getStackTrace(e));
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    public T getResult() {
        return result;
    }

    /**
     * @return the stackTrace
     */
    public String getStackTrace() {
        return stackTrace;
    }

    /**
     * @return the result
     */
    public boolean isSuccess() {
        return success;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OperationResult [success=" + success + ", message=" + message
               + ", stackTrace=" + stackTrace + "]";
    }

}
