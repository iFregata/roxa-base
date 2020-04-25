/**
 * The MIT License
 * 
 * Copyright (c) 2019-2022 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx.rx.jdbc;

import com.zaxxer.hikari.HikariDataSource;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;
import io.roxa.vertx.jdbc.DataSourceBuilder;
import io.roxa.vertx.rx.ResourceDeployer;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class JdbcDeployer extends ResourceDeployer {

	private BehaviorSubject<JdbcAgent> subject = BehaviorSubject.create();

	public JdbcDeployer(String resourceName) {
		super("jdbc", resourceName);

		// SingleSubject<T>

	}

	public Disposable subscribe(Consumer<JdbcAgent> consumer) {
		return subject.subscribe(consumer);
	}

	@Override
	protected Single<Verticle> getResourceAgent(JsonObject cfg) {
		return vertx.<Verticle>rxExecuteBlocking(execPromise -> {
			try {
				DataSourceBuilder builder = DataSourceBuilder.create(cfg);
				HikariDataSource hikariDataSource = builder.build();
				JdbcAgent jdbcAgent = new JdbcAgent(hikariDataSource);
				execPromise.complete(jdbcAgent);
			} catch (Throwable e) {
				execPromise.fail(e);
			}

		}).toSingle().doOnSuccess(v -> subject.onNext((JdbcAgent) v)).doOnError(t -> subject.onError(t));
	}
}
