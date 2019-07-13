/**
 * The MIT License
 * 
 * Copyright (c) 2016-2018 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx.cache;

/**
 * @author Steven Chen
 *
 */
public class DurationAccessToken extends DurationCredential<DurationAccessToken> {

	public static DurationAccessToken example(String id, String groupId) {
		DurationAccessToken inst = new DurationAccessToken();
		inst.id = id;
		inst.groupId = groupId;
		return inst;
	}

	public static DurationAccessToken example(String id) {
		return example(id, id);
	}

	public DurationAccessToken() {
		super();
	}

	/**
	 * 
	 * @param id
	 * @param groupId
	 * @param durationInSecond
	 * @param credential
	 * @param refreshCredential
	 */
	public DurationAccessToken(String id, String groupId, Integer durationInSecond, String credential,
			String refreshCredential) {
		super(id, groupId, durationInSecond, credential, refreshCredential);
	}

	/**
	 * 
	 * @param id
	 * @param groupId
	 * @param durationInSecond
	 * @param credential
	 */
	public DurationAccessToken(String id, String groupId, Integer durationInSecond, String credential) {
		super(id, groupId, durationInSecond, credential);
	}

	public DurationAccessToken item() {
		return this;
	}

}
