package gov.hhs.fha.nhinc.conformance.dao;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public final class ConformanceDAO {

	 private static final Logger LOG = Logger.getLogger(ConformanceDAO.class);
	    private static final ConformanceDAO INSTANCE = new ConformanceDAO();

	    /**
	     * The constructor.
	     */
	    private ConformanceDAO() {
	        LOG.trace("ConformanceDAO initialized");
	    }

	    /**
	     * Returns the instance of the DAO according to the Singleton Pattern.
	     * 
	     * @return ConformanceDAO
	     */
	    public static ConformanceDAO getConformanceDAOInstance() {
	        LOG.trace("getConformanceDAOInstance()...");
	        return INSTANCE;
	    }

	    /**
	     * Inserts a single ConformanceMessage object into the database, returns boolean on success or failure.
	     * 
	     * @param ConformanceMessage
	     * @return boolean
	     */
	    public boolean insertIntoConformanceRepo(ConformanceMessage confMessage) {

	        LOG.trace("ConformanceDAO.insertIntoConformanceRepo() - Begin");
	        Session session = null;
	        Transaction tx = null;
	        boolean result = true;

	        if (confMessage != null) {
	            try {
	                SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	                session = sessionFactory.openSession();
	                tx = session.beginTransaction();
	                LOG.trace("Inserting Record...");

	                session.persist(confMessage);

	                LOG.info("ConformanceMessage with ID " +
	                		confMessage.getMessageID() +
	                		" Inserted successfully...");
	                tx.commit();
	            } catch (HibernateException e) {
	                result = false;
	                transactionRollback(tx);
	                LOG.error("Exception during insertion caused by :" + e.getMessage(), e);
	            } finally {
	                closeSession(session, false);
	            }
	        }
	        LOG.trace("ConformanceDAO.insertIntoConformanceRepo() - End");
	        return result;
	    }
	    
	    private void closeSession(Session session, boolean flush) {
	        if (session != null) {
	            if (flush) {
	                session.flush();
	            }
	            session.close();
	        }
	    }
	    
	    private void transactionRollback(Transaction tx) {
	        if (tx != null) {
	            tx.rollback();
	        }
	    }
}
