package gov.hhs.fha.nhinc.conformance;

import gov.hhs.fha.nhinc.conformance.dao.ConformanceDAO;
import gov.hhs.fha.nhinc.conformance.dao.ConformanceMessage;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.log4j.Logger;

public class ConformanceOutboundInterceptor extends AbstractSoapInterceptor{

private static final Logger LOG = Logger.getLogger(ConformanceOutboundInterceptor.class);
private final String DIRECTION = "OUTBOUND RESPONSE";

	public ConformanceOutboundInterceptor() {
		super(Phase.PRE_STREAM);
        addBefore(StaxOutInterceptor.class.getName());
	}
	
	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		LOG.debug("ConformanceOutboundInteceptor handleMessage() begin.");
		
		final OutputStream os = message.getContent(OutputStream.class);
		if (os == null) {
			return;
		}
		final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(os);
		message.setContent(OutputStream.class, newOut);
		newOut.registerCallback(new ConformanceCallback(os, message));
	}
	
	class ConformanceCallback implements CachedOutputStreamCallback {
		OutputStream origStream;
		SoapMessage message;
		
		public ConformanceCallback(OutputStream os, SoapMessage message){
			origStream = os;
			this.message = message;
		}
		
		@Override
		public void onClose(CachedOutputStream cos) {
			LOG.debug("Conformance callback executing.");
			OutputStream os = cos.getOut();
            if (os instanceof ByteArrayOutputStream) {
                try {
					String envelope = 
							ConformanceUtil.getSoapEnvelope(os.toString());
					if(envelope != null){
						ConformanceMessage confMessage = 
		            			createConfMessageObject(message, envelope, DIRECTION);
		            	if(confMessage != null){
		            		ConformanceDAO.getConformanceDAOInstance().
		            			insertIntoConformanceRepo(confMessage);	
		            	}
		            }
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
            }
            
            try {
            	//empty out the cache
            	cos.lockOutputStream();
            	cos.resetOut(null, false);
            } catch (Exception e) {
            	LOG.error(e.getMessage(), e);
            }
            
            message.setContent(OutputStream.class, origStream);
		}

		@Override
		public void onFlush(CachedOutputStream os) {

		}
		
		private ConformanceMessage createConfMessageObject(SoapMessage origMessage,
				final String confMessage, final String direction) {
			ConformanceMessage result = new ConformanceMessage();
		
	        result.setMessageID(getHeaderElement
	        	(origMessage, NhincConstants.WS_SOAP_HEADER_MESSAGE_ID));
	        result.setAction(getHeaderElement
	            (origMessage, NhincConstants.WS_SOAP_HEADER_ACTION));    
	        result.setRelatesToID(getHeaderElement
	            (origMessage, NhincConstants.WS_SOAP_HEADER_RELATESTO));   
	        result.setMessage(ConformanceUtil.stringToBlob(confMessage)); 
	        result.setConfTime(ConformanceUtil.createTimestamp());  
	        result.setDirection(direction);
	               
	        return result;
		}
		
		private String getHeaderElement(SoapMessage message, String name){
			QName qname = new QName(NhincConstants.WS_ADDRESSING_URL, name);
			String headerValue = null;
			Header header = message.getHeader(qname);
			if(header != null){
				JAXBElement headerObject = (JAXBElement) header.getObject();			
				if(qname.getLocalPart().equalsIgnoreCase(NhincConstants.WS_SOAP_HEADER_RELATESTO)){
					RelatesToType relatesToType = (RelatesToType) headerObject.getValue();
					headerValue = relatesToType.getValue();
				}else {
					AttributedURIType headerAttrType = (AttributedURIType) headerObject.getValue();
					headerValue = headerAttrType.getValue();
				}				
			}
			return headerValue;			
		}
    } 

}
