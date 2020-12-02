package com.flint.si.security;

public class CredentialManager {

	public static final String DATABASE = "sysrepo";

	public static Credentials getCredentials(String target) {
        if(target == DATABASE){
            return new Credentials("admin", "admin".getBytes());
        }
		return null;
	}
}
