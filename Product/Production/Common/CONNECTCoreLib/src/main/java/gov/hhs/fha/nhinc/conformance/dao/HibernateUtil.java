package gov.hhs.fha.nhinc.conformance.dao;


import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.properties.HibernateAccessor;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;

import java.io.File;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static final SessionFactory SESSION_FACTORY;
    private static final Logger LOG = Logger.getLogger(HibernateUtil.class);

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            SESSION_FACTORY = new Configuration().configure(getConfigFile()).buildSessionFactory();
        } catch (ExceptionInInitializerError ex) {
            // Make sure you log the exception, as it might be swallowed
            LOG.error("Initial SessionFactory creation failed." + ex, ex.getCause());
            throw ex;
        }
    }

    /**
     * Method returns an instance of Hibernate SessionFactory.
     * 
     * @return SessionFactory   The Hibernate Session Factory
     */
    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    private static File getConfigFile() {
        File result = null;

        try {
        	result = HibernateAccessor.getInstance().getHibernateFile(NhincConstants.HIBERNATE_CONFORMANCE_REPOSITORY);
        } catch (PropertyAccessException ex) {
        	LOG.error("Unable to load " + NhincConstants.HIBERNATE_CONFORMANCE_REPOSITORY + " " + ex.getMessage(), ex);
        }

        return result;
    }
}
