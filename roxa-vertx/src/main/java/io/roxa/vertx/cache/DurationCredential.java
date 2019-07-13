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

import io.roxa.util.Datetimes;

/**
 * @author Steven Chen
 *
 */
public abstract class DurationCredential<T> implements CachedItem<T> {

	protected String id;
	protected String groupId;
	protected Integer durationInSecond;
	protected Integer expiredInSecond;
	protected String credential;
	protected String refreshCredential;

	/**
	 * 
	 */
	public DurationCredential() {
	}

	public DurationCredential(String id, String groupId, Integer durationInSecond, String credential) {
		super();
		this.id = id;
		this.groupId = groupId;
		this.durationInSecond = durationInSecond;
		this.credential = credential;
		this.expiredInSecond = Datetimes.currentTimeInSecond() + durationInSecond;
	}

	public DurationCredential(String id, String groupId, Integer durationInSecond, String credential,
			String refreshCredential) {
		this(id, groupId, durationInSecond, credential);
		this.refreshCredential = refreshCredential;
	}

	public String getId() {
		return this.id;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public Integer getDurationInSecond() {
		return this.durationInSecond;
	}

	public Integer getExpiredInSecond() {
		return this.expiredInSecond;
	}

	public String getCredential() {
		return this.credential;
	}

	public String getRefreshCredential() {
		return this.refreshCredential;
	}

	public boolean isNotExpired() {
		Integer currentTimeInSecond = Datetimes.currentTimeInSecond();
		return (currentTimeInSecond < expiredInSecond);
	}

	public boolean isExpired() {
		Integer currentTimeInSecond = Datetimes.currentTimeInSecond();
		return (currentTimeInSecond >= expiredInSecond);
	}

	public boolean isAlmostExpired() {
		Integer currentTimeInSecond = Datetimes.currentTimeInSecond();
		return (currentTimeInSecond > (expiredInSecond - 300));
	}

	@Override
	public String getCacheId() {
		return String.format("%s#%s", id, groupId);
	}

}
