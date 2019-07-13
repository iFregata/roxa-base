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
package io.roxa.fn;

/**
 * @author Steven Chen
 *
 */
public class Tuple2<V0, V1> {

	protected V0 val0;
	protected V1 val1;

	/**
	 * 
	 */
	public Tuple2(V0 val0, V1 val1) {
		this.val0 = val0;
		this.val1 = val1;
	}

	public V0 $0() {
		return val0;
	}

	public V1 $1() {
		return val1;
	}
}
