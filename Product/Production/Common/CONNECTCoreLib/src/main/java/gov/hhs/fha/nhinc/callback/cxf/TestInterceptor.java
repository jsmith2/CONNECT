/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.hhs.fha.nhinc.callback.cxf;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.Destination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jassmit
 */
public class TestInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOG = LoggerFactory.getLogger(TestInterceptor.class);
    
    public TestInterceptor() {
        super(Phase.PRE_LOGICAL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        
        if(message != null) {
            Destination destination = message.getDestination();
            Object test = message.get("testHeader");
            if(test != null && test instanceof String) {
                LOG.info("Test header = {}", (String) test);
            }
        }
    }
    
}
