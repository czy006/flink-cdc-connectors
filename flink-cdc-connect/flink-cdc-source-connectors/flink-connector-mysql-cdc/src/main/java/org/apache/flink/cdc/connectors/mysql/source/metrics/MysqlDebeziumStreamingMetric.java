/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.cdc.connectors.mysql.source.metrics;

import org.apache.flink.cdc.debezium.metric.DebeziumSourceReaderMetrics;
import org.apache.flink.metrics.Gauge;
import org.apache.flink.metrics.MetricGroup;

public class MysqlDebeziumStreamingMetric extends DebeziumSourceReaderMetrics {

    private volatile Long millisecondsSinceLastEvent = 0L;
    private volatile Long totalNumberOfEventsSeen = 0L;
    private volatile Long totalNumberOfCreateEventsSeen = 0L;
    private volatile Long totalNumberOfUpdateEventsSeen = 0L;
    private volatile Long totalNumberOfDeleteEventsSeen = 0L;
    private volatile Long numberOfErroneousEvents = 0L;
    private volatile Long numberOfEventsFiltered = 0L;
    private volatile Integer queueRemainingCapacity = 0;
    private volatile Long milliSecondsBehindSource = 0L;
    private volatile Long numberOfDisconnects = 0L;
    private volatile Long numberOfNotWellFormedTransactions = 0L;
    private volatile Long numberOfSkippedEvents = 0L;

    public MysqlDebeziumStreamingMetric(MetricGroup metricGroup) {
        super(metricGroup);
    }

    @Override
    public void registerMetrics() {
        MetricGroup streamingGroup = metricGroup.addGroup("mysql-debezium", "streaming");
        streamingGroup.gauge(
                "millisecondsSinceLastEvent", (Gauge<Long>) this::getMillisecondsSinceLastEvent);
        streamingGroup.gauge(
                "totalNumberOfEventsSeen", (Gauge<Long>) this::getTotalNumberOfEventsSeen);
        streamingGroup.gauge(
                "totalNumberOfCreateEventsSeen",
                (Gauge<Long>) this::getTotalNumberOfCreateEventsSeen);
        streamingGroup.gauge(
                "totalNumberOfUpdateEventsSeen",
                (Gauge<Long>) this::getTotalNumberOfUpdateEventsSeen);
        streamingGroup.gauge(
                "totalNumberOfDeleteEventsSeen",
                (Gauge<Long>) this::getTotalNumberOfDeleteEventsSeen);
        streamingGroup.gauge(
                "numberOfEventsFiltered", (Gauge<Long>) this::getNumberOfEventsFiltered);
        streamingGroup.gauge(
                "queueRemainingCapacity", (Gauge<Integer>) this::getQueueRemainingCapacity);
        streamingGroup.gauge(
                "milliSecondsBehindSource", (Gauge<Long>) this::getMilliSecondsBehindSource);
        streamingGroup.gauge("numberOfDisconnects", (Gauge<Long>) this::getNumberOfDisconnects);
        streamingGroup.gauge(
                "numberOfNotWellFormedTransactions",
                (Gauge<Long>) this::getNumberOfNotWellFormedTransactions);
        streamingGroup.gauge("numberOfSkippedEvents", (Gauge<Long>) this::getNumberOfSkippedEvents);
        streamingGroup.gauge(
                "numberOfErroneousEvents", (Gauge<Long>) this::getNumberOfErroneousEvents);
    }

    public Long getMillisecondsSinceLastEvent() {
        return millisecondsSinceLastEvent;
    }

    public void setMillisecondsSinceLastEvent(Long millisecondsSinceLastEvent) {
        this.millisecondsSinceLastEvent = millisecondsSinceLastEvent;
    }

    public Long getTotalNumberOfEventsSeen() {
        return totalNumberOfEventsSeen;
    }

    public void setTotalNumberOfEventsSeen(Long totalNumberOfEventsSeen) {
        this.totalNumberOfEventsSeen = totalNumberOfEventsSeen;
    }

    public Long getTotalNumberOfCreateEventsSeen() {
        return totalNumberOfCreateEventsSeen;
    }

    public void setTotalNumberOfCreateEventsSeen(Long totalNumberOfCreateEventsSeen) {
        this.totalNumberOfCreateEventsSeen = totalNumberOfCreateEventsSeen;
    }

    public Long getTotalNumberOfUpdateEventsSeen() {
        return totalNumberOfUpdateEventsSeen;
    }

    public void setTotalNumberOfUpdateEventsSeen(Long totalNumberOfUpdateEventsSeen) {
        this.totalNumberOfUpdateEventsSeen = totalNumberOfUpdateEventsSeen;
    }

    public Long getTotalNumberOfDeleteEventsSeen() {
        return totalNumberOfDeleteEventsSeen;
    }

    public void setTotalNumberOfDeleteEventsSeen(Long totalNumberOfDeleteEventsSeen) {
        this.totalNumberOfDeleteEventsSeen = totalNumberOfDeleteEventsSeen;
    }

    public Long getNumberOfErroneousEvents() {
        return numberOfErroneousEvents;
    }

    public void setNumberOfErroneousEvents(Long numberOfErroneousEvents) {
        this.numberOfErroneousEvents = numberOfErroneousEvents;
    }

    public Long getNumberOfEventsFiltered() {
        return numberOfEventsFiltered;
    }

    public void setNumberOfEventsFiltered(Long numberOfEventsFiltered) {
        this.numberOfEventsFiltered = numberOfEventsFiltered;
    }

    public Integer getQueueRemainingCapacity() {
        return queueRemainingCapacity;
    }

    public void setQueueRemainingCapacity(Integer queueRemainingCapacity) {
        this.queueRemainingCapacity = queueRemainingCapacity;
    }

    public Long getMilliSecondsBehindSource() {
        return milliSecondsBehindSource;
    }

    public void setMilliSecondsBehindSource(Long milliSecondsBehindSource) {
        this.milliSecondsBehindSource = milliSecondsBehindSource;
    }

    public Long getNumberOfDisconnects() {
        return numberOfDisconnects;
    }

    public void setNumberOfDisconnects(Long numberOfDisconnects) {
        this.numberOfDisconnects = numberOfDisconnects;
    }

    public Long getNumberOfNotWellFormedTransactions() {
        return numberOfNotWellFormedTransactions;
    }

    public void setNumberOfNotWellFormedTransactions(Long numberOfNotWellFormedTransactions) {
        this.numberOfNotWellFormedTransactions = numberOfNotWellFormedTransactions;
    }

    public Long getNumberOfSkippedEvents() {
        return numberOfSkippedEvents;
    }

    public void setNumberOfSkippedEvents(Long numberOfSkippedEvents) {
        this.numberOfSkippedEvents = numberOfSkippedEvents;
    }
}
