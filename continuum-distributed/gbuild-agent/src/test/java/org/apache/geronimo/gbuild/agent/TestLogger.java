/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.gbuild.agent;

import org.codehaus.plexus.logging.Logger;

/**
 * @version $Rev$ $Date$
 */
public class TestLogger implements org.codehaus.plexus.logging.Logger {

    private final String name;

    public TestLogger(String name) {
        this.name = name;
    }

    public TestLogger() {
        this.name = hashCode()+"";
    }

    public void debug(String string) {
        System.out.println("[debug] " + name +" - " + string);
    }

    public void debug(String string, Throwable throwable) {
        System.out.println("[debug] " + name +" - " + string);
        throwable.printStackTrace();
    }

    public boolean isDebugEnabled() {
        return false;
    }

    public void info(String string) {
        System.out.println("[info] " + name +" - " + string);
    }

    public void info(String string, Throwable throwable) {
        System.out.println("[info] " + name +" - " + string);
        throwable.printStackTrace();
    }

    public boolean isInfoEnabled() {
        return false;
    }

    public void warn(String string) {
        System.out.println("[warn] " + name +" - " + string);
    }

    public void warn(String string, Throwable throwable) {
        System.out.println("[warn] " + name +" - " + string);
        throwable.printStackTrace();
    }

    public boolean isWarnEnabled() {
        return false;
    }

    public void error(String string) {
        System.out.println("[error] " + name +" - " + string);
    }

    public void error(String string, Throwable throwable) {
        System.out.println("[error] " + name +" - " + string);
        throwable.printStackTrace();
    }

    public boolean isErrorEnabled() {
        return false;
    }

    public void fatalError(String string) {
        System.out.println("[fatalError] " + name +" - " + string);
    }

    public void fatalError(String string, Throwable throwable) {
        System.out.println("[fatalError] " + name +" - " + string);
        throwable.printStackTrace();
    }

    public boolean isFatalErrorEnabled() {
        return false;
    }

    public Logger getChildLogger(String string) {
        return null;
    }

    public int getThreshold() {
        return 0;
    }

    public String getName() {
        return null;
    }
}
