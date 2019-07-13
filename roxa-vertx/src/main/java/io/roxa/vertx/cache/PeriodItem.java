/**
 * The MIT License
 * 
 * Copyright (c) 2018-2020 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx.cache;

import io.roxa.util.Datetimes;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class PeriodItem {

	private Integer expired;
	private JsonObject item;

	public static PeriodItem of(JsonObject data) {
		JsonObject attrs = data.getJsonObject("attrs", null);
		PeriodItem periodItem = new PeriodItem();
		if (attrs != null) {
			periodItem.expired = attrs.getInteger("expired", null);
		}
		periodItem.item = data.getJsonObject("item");
		return periodItem;
	}

	public PeriodItem() {

	}

	public PeriodItem(JsonObject item) {
		this.item = item;
	}

	public PeriodItem(Integer period, JsonObject item) {
		this.expired = Datetimes.currentTimeInSecond() + period;
		this.item = item;
	}

	public JsonObject getItem() {
		return this.item;
	}

	public boolean isNotPeriod() {
		return expired == null;
	}

	public boolean isExpired() {
		int currentTimeInSecond = Datetimes.currentTimeInSecond();
		return (currentTimeInSecond >= expired);
	}

	public JsonObject asJson() {
		JsonObject data = new JsonObject().put("item", item);
		if (expired != null)
			data.put("attrs", new JsonObject().put("expired", expired));
		return data;
	}

}
