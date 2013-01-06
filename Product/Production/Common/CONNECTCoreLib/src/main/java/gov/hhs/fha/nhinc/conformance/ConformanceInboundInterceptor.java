package gov.hhs.fha.nhinc.conformance;

import gov.hhs.fha.nhinc.conformance.dao.ConformanceDAO;
import gov.hhs.fha.nhinc.conformance.dao.ConformanceMessage;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.log4j.Logger;

public class ConformanceInboundInterceptor extends AbstractSoapInterceptor{

	private static final Logger LOG = Logger.getLogger(ConformanceInboundInterceptor.class);
	private final String DIRECTION = "INBOUND REQUEST";
	
	public ConformanceInboundInterceptor() {
		super(Phase.UNMARSHAL);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		LOG.debug("ConformanceInboundInteceptor handleMessage() begin.");
		try {
			SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapMessage.writeTo(out);
            message.setContent(SOAPMessage.class, soapMessage);
            
            String envelope = 
            		ConformanceUtil.getSoapEnvelope(out.toString());
            
            if(envelope != null){
            	ConformanceMessage confMessage = 
            			createConfMessageObject(soapMessage, envelope, DIRECTION);
            	if(confMessage != null){
            		ConformanceDAO.getConformanceDAOInstance().
            			insertIntoConformanceRepo(confMessage);	
            	}
            }            
            out.close();
			
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} catch (SOAPException e) {
			LOG.error(e.getMessage(), e);
		}	
	}
	
	private ConformanceMessage createConfMessageObject(SOAPMessage soapMessage,
			String confMessage, String direction) throws SOAPException{
		SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope soapEnvelope;
        
        soapEnvelope = soapPart.getEnvelope();
        SOAPHeader soapHeader = soapEnvelope.getHeader();

		ConformanceMessage result = new ConformanceMessage();
		
		result.setAction(getStringfromElement(soapHeader, NhincConstants.WS_SOAP_HEADER_ACTION));
		result.setMessageID(getStringfromElement(soapHeader, NhincConstants.WS_SOAP_HEADER_MESSAGE_ID));
		result.setRelatesToID(getStringfromElement(soapHeader, NhincConstants.WS_SOAP_HEADER_RELATESTO));
        result.setMessage(ConformanceUtil.stringToBlob(confMessage)); 
        result.setConfTime(ConformanceUtil.createTimestamp());  
        result.setDirection(direction);
               
        return result;
	}
	
	private String getStringfromElement(SOAPHeader header, String headerElement){
		SOAPElement messageIdElement = getFirstChild(header, NhincConstants.WS_ADDRESSING_URL,
				headerElement);
		if(messageIdElement != null){
			return messageIdElement.getTextContent();
		}else {
			return null;
		}
	}
	
	private SOAPElement getFirstChild(SOAPHeader header, String ns, String name) {
        QName qname = new QName(ns, name);
        return getFirstChild(header, qname);
    }

    private SOAPElement getFirstChild(SOAPHeader header, QName qname) {
        SOAPElement result = null;
        if (header != null) {
            Iterator iter = header.getChildElements(qname);
            if (iter.hasNext()) {
                result = (SOAPElement) iter.next();
            }
        }
        return result;
    }
}
