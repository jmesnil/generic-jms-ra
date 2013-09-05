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

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.jboss.ejb3.annotation.ResourceAdapter;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2013 Red Hat inc.
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "/queue/source"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory;java.naming.provider.url=JBM_HOST:1099;java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces"),
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "XAConnectionFactory")
})
@ResourceAdapter("generic-jms-ra-1.0.RC2-SNAPSHOT_myra")
public class ExampleMDB implements MessageListener
{

    @Resource
    private MessageDrivenContext context;

    @Resource(name = "java:/GenericJmsXA")
    private ConnectionFactory cf;

    public void onMessage(final Message message)
    {
        Connection connection = null;

        try
        {
            connection = cf.createConnection();

            Destination replyTo = message.getJMSReplyTo();
            if (replyTo != null) {
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session.createProducer(replyTo);
                String replyText = ((TextMessage)message).getText();

                Message reply = session.createTextMessage(replyText);
                reply.setJMSCorrelationID(message.getJMSMessageID());
                producer.send(reply);
            }
        }
        catch (Exception e)
        {
            context.setRollbackOnly();
        }
        finally
        {
            if (connection != null)
            {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}