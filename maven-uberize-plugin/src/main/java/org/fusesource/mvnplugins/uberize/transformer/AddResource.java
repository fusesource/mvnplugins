package org.fusesource.mvnplugins.uberize.transformer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import org.fusesource.mvnplugins.uberize.UberEntry;
import org.fusesource.mvnplugins.uberize.Transformer;
import org.fusesource.mvnplugins.uberize.Uberizer;

/**
 * A resource processor that allows the addition of an arbitrary file
 * content into the uber JAR.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AddResource implements Transformer {

    String path;
    File file;

    public void process(Uberizer uberizer, File workDir, TreeMap<String, UberEntry> uberEntries) throws IOException {
        if( file!=null && file.exists() && path!=null ) {
            UberEntry uberEntry = uberEntries.get(path);
            if (uberEntry == null) {
            	uberEntry = new UberEntry(path);
            }
            UberEntry modEntry = new UberEntry(path, uberEntry);
            modEntry.getSources().add(file);
            modEntry.getSources().addAll(uberEntry.getSources());
            uberEntries.put(path, modEntry);
        }
    }

}
