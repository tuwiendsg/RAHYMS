package at.ac.tuwien.dsg.salam.rest.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class CollectiveNotAvailable extends WebApplicationException {
	
    private static final long serialVersionUID = -4538801908426891117L;

    public CollectiveNotAvailable() {
		super(
			Response
				.status( Status.SERVICE_UNAVAILABLE )
				.build()
		);
	}
}
