package gov.hhs.fha.nhinc.conformance;

import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

public class ConformanceUtil {

	private static final Logger LOG = Logger.getLogger(ConformanceUtil.class);
	
	public static String getSoapEnvelope(String rawMessage) throws IOException{
		BufferedReader reader = new BufferedReader( new StringReader(rawMessage));
		
		String line;
		Boolean envelopeStart = false;
		Boolean completeEnv = false;
		StringBuffer result = new StringBuffer();
		
		while((line = reader.readLine()) != null){
			if(line.startsWith("<soap:Envelope")){
				envelopeStart = true;
			}			
			if(envelopeStart){
				result.append(line);
			}
			if(line.endsWith("</soap:Envelope>")){
				completeEnv = true;
				break;
			}
		}
		
		reader.close();
		
		if(completeEnv){
			return result.toString();
		}else{
			return null;
		}
	}
    
    public static Blob stringToBlob(String confMessage){
    	return Hibernate.createBlob(confMessage.getBytes());
    }
    
    public static Timestamp createTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
    
    public static Boolean isInterceptorEnabled(String confType){
    	PropertyAccessor propAccessor = PropertyAccessor.getInstance(NhincConstants.GATEWAY_PROPERTY_FILE);		
		try {
			if(!propAccessor.getPropertyBoolean(confType)){
				return false;
			}
		} catch (PropertyAccessException e) {
			LOG.error("Unable to access " + confType + " from " +
					NhincConstants.GATEWAY_PROPERTY_FILE, e);
			return false;
		}
		
		return true;
    }
}
