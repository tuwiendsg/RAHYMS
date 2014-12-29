package at.ac.tuwien.dsg.hcu.rest.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class AlreadyExistsException extends WebApplicationException {
	private static final long serialVersionUID = 6817489620338221395L;

	public AlreadyExistsException() {
		super(
			Response
				.status( Status.CONFLICT )
				.build()
		);
	}
}
