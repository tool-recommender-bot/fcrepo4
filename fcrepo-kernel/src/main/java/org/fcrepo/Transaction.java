package org.fcrepo;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.modeshape.jcr.JcrSession;

@XmlRootElement(name = "transaction")
@XmlAccessorType(XmlAccessType.FIELD)
public class Transaction {
	
	// the default timeout is 3 minutes
	public static final long DEFAULT_TIMEOUT =
			3l * 60l * 1000l;
	
	public static final String TIMEOUT_SYSTEM_PROPERTY =
			"fcrepo4.tx.timeout";

	public static enum State {
		DIRTY, NEW, COMMITED, ROLLED_BACK;
	}

	@XmlTransient
	private final Session session;

	@XmlAttribute(name = "id")
	private final String id;

	@XmlAttribute(name = "created")
	private final Date created;

	@XmlAttribute(name = "expires")
	private Calendar expires;

	private State state = State.NEW;

	private Transaction(){
		this.session = null;
		this.created = null;
		this.id = null;
		this.expires = null;
	}

	public Transaction(Session session) {
		super();
		this.session = session;
		this.created = new Date();
		this.id = UUID.randomUUID().toString();
		this.expires = Calendar.getInstance();
		this.updateExpiryDate();
	}

	public Session getSession() {
		return session;
	}

	public Date getCreated() {
		return created;
	}

	public String getId() {
		return id;
	}

	public State getState() throws RepositoryException {
		if (this.session != null && this.session.hasPendingChanges()) {
			return State.DIRTY;
		}
		return state;
	}

    public Date getExpires() {
        return expires.getTime();
    }

    public void commit() throws RepositoryException {
        this.session.save();
        this.state = State.COMMITED;
        this.expire();
    }
    
    /**
     * End the session, and mark for reaping
     * @throws RepositoryException
     */
    public void expire() throws RepositoryException {
    	this.session.logout();
        this.expires.setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Discard all unpersisted changes and expire
     * @throws RepositoryException
     */
    public void rollback() throws RepositoryException {
        this.state = State.ROLLED_BACK;
        this.session.refresh(false);
        this.expire();
    }
    
    /**
     * Roll forward the expiration date for recent activity
     */
    public void updateExpiryDate() {
        long duration;
        if (System.getProperty(TIMEOUT_SYSTEM_PROPERTY) != null){
            duration = Long.parseLong(System.getProperty(TIMEOUT_SYSTEM_PROPERTY));
        }else{
        	duration = DEFAULT_TIMEOUT;
        }
        this.expires.setTimeInMillis(System.currentTimeMillis() + duration);
    }

}
