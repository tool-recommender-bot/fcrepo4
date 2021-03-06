/*
 * Licensed to DuraSpace under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * DuraSpace licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fcrepo.integration.kernel.modeshape;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.inject.Inject;

import org.fcrepo.kernel.api.FedoraRepository;
import org.fcrepo.kernel.api.FedoraSession;
import org.fcrepo.kernel.api.models.Container;
import org.fcrepo.kernel.api.models.FedoraBinary;
import org.fcrepo.kernel.api.models.FedoraResource;
import org.fcrepo.kernel.api.models.FedoraTimeMap;
import org.fcrepo.kernel.api.services.BinaryService;
import org.fcrepo.kernel.api.services.ContainerService;
import org.fcrepo.kernel.api.services.TimeMapService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.test.context.ContextConfiguration;

/**
 * <p>FedoraTimeMapImplIT class.</p>
 *
 * @author lsitu
 */
@ContextConfiguration({"/spring-test/fcrepo-config.xml"})
public class FedoraTimeMapImplIT extends AbstractIT {

    @Inject
    private FedoraRepository repo;

    @Inject
    private ContainerService containerService;

    @Inject
    private BinaryService binaryService;

    @Inject
    private TimeMapService timeMapService;

    private FedoraSession session;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        session = repo.login();
    }

    @After
    public void tearDown() {
        session.expire();
    }

    @Test
    public void testGetOriginalResource() {
        final String pid = getRandomPid();
        final Container object = containerService.findOrCreate(session, "/" + pid);
        timeMapService.findOrCreate(session, "/" + pid);
        session.commit();

        final FedoraTimeMap timeMap = (FedoraTimeMap) object.getTimeMap();

        final FedoraResource originalResource = timeMap.getOriginalResource();
        assertTrue(originalResource instanceof Container);
        assertEquals("Original resource must reference original container",
                object.getPath(), originalResource.getPath());
    }

    @Test
    public void testGetOriginalBinaryResource() throws Exception {
        final String pid = getRandomPid();
        final FedoraBinary object = binaryService.findOrCreate(session, "/" + pid);
        try (InputStream contentStream = new ByteArrayInputStream("content".getBytes())) {
            object.setContent(contentStream, "text/plain", null, null, null);
        }
        timeMapService.findOrCreate(session, "/" + pid);
        session.commit();

        final FedoraTimeMap timeMap = (FedoraTimeMap) object.getTimeMap();

        final FedoraResource originalResource = timeMap.getOriginalResource();
        assertTrue(originalResource instanceof FedoraBinary);
        assertEquals("Original resource must reference original container",
                object.getPath(), originalResource.getPath());
    }

    @Test
    public void testGetOriginalBinaryDescriptionResource() throws Exception {
        final String pid = getRandomPid();
        final FedoraBinary binary = binaryService.findOrCreate(session, "/" + pid);
        try (InputStream contentStream = new ByteArrayInputStream("content".getBytes())) {
            binary.setContent(contentStream, "text/plain", null, null, null);
        }
        timeMapService.findOrCreate(session, "/" + pid);
        session.commit();

        final FedoraResource description = binary.getDescribedResource();
        final FedoraTimeMap timeMap = (FedoraTimeMap) description.getTimeMap();

        final FedoraResource originalResource = timeMap.getOriginalResource();
        assertTrue(originalResource instanceof FedoraBinary);
        assertEquals("Original resource must reference original container",
                description.getPath(), originalResource.getPath());
    }
}
