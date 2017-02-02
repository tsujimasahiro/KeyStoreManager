package com.kiririmode.vault.util;

import java.security.KeyStore;


public interface Vault {

	String retrieve(String key) throws VaultException;

	void entry(String alias, String secret, KeyStore ks) throws VaultException;

}
