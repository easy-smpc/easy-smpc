/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bihealth.mi.easysmpc.nogui;

import java.util.concurrent.atomic.AtomicLong;

import org.bihealth.mi.easybus.PerformanceListener;

public class PerformanceTracker implements PerformanceListener {

	/** Total number of messages started to receive */
	private static final AtomicLong numberMessagesReceived    = new AtomicLong();
	/** Total attachment size of messages started to receive */
	private static final AtomicLong totalSizeMessagesReceived = new AtomicLong();
	/** Total number of messages sent */
	private static final AtomicLong numberMessagesSent        = new AtomicLong();
	/** Total attachment size of messages sent */
	private static final AtomicLong totalSizeMessagesSent     = new AtomicLong();
    
    /**
     * Resets the statistics
     */
    public void resetStatistics() {
        numberMessagesReceived.set(0);
        totalSizeMessagesReceived.set(0);
        numberMessagesSent.set(0);
        totalSizeMessagesSent.set(0);
    }

	@Override
	public void messageReceived(long size) {
		numberMessagesReceived.incrementAndGet();
		totalSizeMessagesReceived.addAndGet(size);
	}

	@Override
	public void messageSent(long size) {
		numberMessagesSent.incrementAndGet();
		totalSizeMessagesSent.addAndGet(size);
	}

	/**
	 * Returns indicators
	 * @return
	 */
	public long getNumberMessagesReceived() {
		return numberMessagesReceived.longValue();
	}

	/**
	 * Returns indicators
	 * @return
	 */
	public long getTotalSizeMessagesReceived() {
		return totalSizeMessagesReceived.longValue();
	}

	/**
	 * Returns indicators
	 * @return
	 */
	public long getNumberMessagesSent() {
		return numberMessagesSent.longValue();
	}

	/**
	 * Returns indicators
	 * @return
	 */
	public long getTotalsizeMessagesSent() {
		return totalSizeMessagesSent.longValue();
	}
}
