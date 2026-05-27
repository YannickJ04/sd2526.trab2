package sd2526.trab.impl.rest.servers;

import java.util.List;
import java.util.Set;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import sd2526.trab.api.User;
import sd2526.trab.api.java.Users;
import sd2526.trab.api.rest.RestUsers;
import sd2526.trab.impl.api.java.AdminUsers;
import sd2526.trab.impl.api.rest.RestAdminUsers;
import sd2526.trab.impl.java.clients.Clients;
import sd2526.trab.impl.java.servers.JavaUsers;
import sd2526.trab.impl.utils.ServerSecret;

@Singleton
public class RestUsersResource extends RestResource implements RestUsers, RestAdminUsers {

	static boolean isGateway = false;
	
	Users impl;

	@Context
    HttpHeaders headers;

	synchronized Users impl() {
		if( impl == null )
			impl = isGateway ? Clients.UsersClient.get() : JavaUsers.getInstance();	
		return impl;
	}
		
	public RestUsersResource() {}
	
	RestUsersResource(boolean gw) {	
		isGateway = gw;
	}

	private void checkSecret() {
		String incoming = headers.getHeaderString(ServerSecret.SECRET_HEADER);
		if (!ServerSecret.isValid(incoming)) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}
	}
	
	@Override
	public String postUser(User user) {
		return super.resultOrThrow( impl().postUser(user));
	}

	@Override
	public User getUser(String name, String pwd) {
		return super.resultOrThrow( impl().getUser(name, pwd));
	}

	@Override
	public User updateUser(String name, String pwd, User info) {
		return super.resultOrThrow( impl().updateUser(name, pwd, info));
	}

	@Override
	public User deleteUser(String name, String pwd) {
		return super.resultOrThrow( impl().deleteUser(name, pwd));
	}

	@Override
	public List<User> searchUsers(String name, String pwd, String pattern) {
		return super.resultOrThrow( impl().searchUsers(name, pwd, pattern));
	}

	@Override
	public Set<String> checkUsers(Set<String> names) {
		checkSecret();
		return super.resultOrThrow(((AdminUsers)impl).checkUsers(names));
	}
}
