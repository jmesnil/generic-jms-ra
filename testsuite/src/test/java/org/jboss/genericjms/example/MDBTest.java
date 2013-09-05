/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.genericjms.example;

import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2013 Red Hat inc.
 */
@RunWith(Arquillian.class)
public class MDBTest {

    @Deployment
    public static EnterpriseArchive createDeployment() {
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "example.ear");

        final JavaArchive rar = ShrinkWrap.create(ZipImporter.class, "example.rar")
                .importFrom(new File("../generic-jms-ra-rar/target/generic-jms-ra-1.0.RC2-SNAPSHOT.rar"))
                .as(JavaArchive.class);

        rar.as(ZipExporter.class)
                .exportTo(new File("/tmp/wtf.rar"), true);

        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "example.jar");
        ejbJar.addClasses(ExampleMDB.class);

        ear.addAsModule(rar);
        ear.addAsModule(ejbJar);

        return ear;
    }

    @Resource(mappedName = "/ConnectionFactory")
    private ConnectionFactory factory;

    @Test
    public void test() {
        assertTrue(true);
    }
}
