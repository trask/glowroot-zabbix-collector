/*
 * Copyright 2018 the original author or authors.
 *
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
package com.github.trask;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import io.github.hengyunabc.zabbix.sender.DataObject;
import io.github.hengyunabc.zabbix.sender.ZabbixSender;

import org.glowroot.agent.collector.Collector;
import org.glowroot.agent.shaded.org.glowroot.wire.api.model.AgentConfigOuterClass.AgentConfig;
import org.glowroot.agent.shaded.org.glowroot.wire.api.model.CollectorServiceOuterClass.Environment;
import org.glowroot.agent.shaded.org.glowroot.wire.api.model.CollectorServiceOuterClass.GaugeValue;
import org.glowroot.agent.shaded.org.glowroot.wire.api.model.CollectorServiceOuterClass.LogEvent;

public class ZabbixCollector implements org.glowroot.agent.collector.Collector {

    private final Collector delegate;

    private volatile ZabbixSender zabbixSender;

    public ZabbixCollector(Collector delegate) {
        this.delegate = delegate;
    }

    @Override
    public void init(File confDir, File sharedConfDir, Environment environment,
            AgentConfig agentConfig, AgentConfigUpdater agentConfigUpdater) throws Exception {
        delegate.init(confDir, sharedConfDir, environment, agentConfig, agentConfigUpdater);
        zabbixSender = new ZabbixSender("localhost", 12345);
    }

    @Override
    public void collectAggregates(AggregateReader aggregateReader) throws Exception {
        delegate.collectAggregates(aggregateReader);
    }

    @Override
    public void collectGaugeValues(List<GaugeValue> gaugeValues) throws Exception {
        delegate.collectGaugeValues(gaugeValues);
        List<DataObject> dataObjects = new ArrayList<DataObject>();
        for (GaugeValue gaugeValue : gaugeValues) {
            DataObject dataObject = new DataObject();
            dataObject.setHost(InetAddress.getLocalHost().getHostAddress());
            dataObject.setKey(gaugeValue.getGaugeName());
            dataObject.setValue(Double.toString(gaugeValue.getValue()));
            dataObject.setClock(gaugeValue.getCaptureTime() / 1000);
            dataObjects.add(dataObject);
        }
        zabbixSender.send(dataObjects);
    }

    @Override
    public void collectTrace(TraceReader traceReader) throws Exception {
        delegate.collectTrace(traceReader);
    }

    @Override
    public void log(LogEvent logEvent) throws Exception {
        delegate.log(logEvent);
    }
}
