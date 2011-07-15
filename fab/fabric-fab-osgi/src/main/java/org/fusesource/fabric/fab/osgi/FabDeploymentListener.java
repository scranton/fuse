/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.fabric.fab.osgi;

import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.fusesource.fabric.fab.osgi.url.ServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * A deployment listener that listens for spring xml applications
 * and creates bundles for these.
 */
public class FabDeploymentListener implements ArtifactUrlTransformer {

    private final Logger logger = LoggerFactory.getLogger(FabDeploymentListener.class);

    private DocumentBuilderFactory dbf;

    public boolean canHandle(File artifact) {
        try {
            // only handle .jar files
            String path = artifact.getPath();
            if (!path.endsWith(".jar") && !path.endsWith(".fab")) {
                return false;
            }
            JarFile jar = new JarFile(artifact);
            try {
                // only handle non OSGi jar
                Manifest manifest = jar.getManifest();
                boolean answer = false;
                if (manifest != null) {
                    Attributes attributes = manifest.getMainAttributes();
                    for (String name : ServiceConstants.FAB_PROPERTY_NAMES) {
                        if (attributes.getValue(name) != null) {
                            answer = true;
                            break;
                        }
                    }
                }
                return answer;
            } finally {
                jar.close();
            }
        } catch (Exception e) {
            logger.error("Unable to parse deployed file " + artifact.getAbsolutePath(), e);
        }
        return false;
    }

    public URL transform(URL artifact) {
        try {
            return new URL("fab", null, artifact.toString());
        } catch (Exception e) {
            logger.error("Unable to build blueprint application bundle", e);
            return null;
        }
    }

}