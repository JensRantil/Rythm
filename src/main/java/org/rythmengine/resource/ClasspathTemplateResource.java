/* 
 * Copyright (C) 2013 The Rythm Engine project
 * Gelin Luo <greenlaw110(at)gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.rythmengine.resource;

import org.rythmengine.extension.ITemplateResourceLoader;
import org.rythmengine.utils.IO;

import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: luog
 * Date: 20/01/12
 * Time: 11:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClasspathTemplateResource extends TemplateResourceBase implements ITemplateResource {

    private static final long serialVersionUID = -164305020378609839L;
    private URL url;
    private String key;

    ClasspathTemplateResource(String path, ITemplateResourceLoader loader) {
        super(loader);
        ClassLoader cl = loader.getEngine().classLoader();

        // strip leading slash so path will work with classes in a JAR file
        while (path.startsWith("/")) path = path.substring(1);

        url = cl.getResource(path);

        if( !isValid() ) {
            url = cl.getResource( loader.getResourceLoaderRoot() + "/" + path );
        }

        key = path;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String reload() {
        return IO.readContentAsString(url);
    }

    @Override
    protected long lastModified() {
        if (getEngine().isProdMode()) return 0;

        String fileName;
        if ("file".equals(url.getProtocol())) {
            fileName = url.getFile();
        } else if ("jar".equals(url.getProtocol())) {
            try {
                java.net.JarURLConnection jarUrl = (java.net.JarURLConnection) url.openConnection();
                fileName = jarUrl.getJarFile().getName();
            } catch (Exception e) {
                return System.currentTimeMillis() + 1;
            }
        } else {
            return System.currentTimeMillis() + 1;
        }

        java.io.File file = new java.io.File(fileName);
        return file.lastModified();
    }

    @Override
    public boolean isValid() {
        return null != url;
    }

    @Override
    protected long defCheckInterval() {
        return -1;
    }

    @Override
    protected Long userCheckInterval() {
        return Long.valueOf(1000 * 5);
    }

    @Override
    public String getSuggestedClassName() {
        return path2CN(key);
    }

}
