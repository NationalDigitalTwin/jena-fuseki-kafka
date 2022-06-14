/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.kafka;

import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.logging.Log;

/**
 * Details of a connector to Kafka.
 * <p>
 * The machinery is in {@link DeserializerActionFK} which reads from kafka, and
 * creates a {@link ActionFK}.
 * <p>
 * For Fuseki, the {@link ActionFK} is handled by {@code FKRequestProcessor} which
 * dispatches the request to the main Fuseki execution path (includes Fuseki logging).
 */
public class ConnectorFK {

    enum State { INIT, RUNNING, SHUTDOWN }

    // Source
    private final String topic;

    // Destination - this Fuseki server
    private final String fusekiDispatchPath;
    // URL to replay the the request to.
    private final String remoteEndpoint;

    private final boolean syncTopic;
    private final boolean replayTopic;
    // State tracking.
    private final String stateFile;

    // Kafka consumer setup.
    private final Properties kafkaProps;
    private State state = State.INIT;

    public ConnectorFK(String topic, String fusekiDispatchName, String remoteEndpoint, String stateFile,
                       boolean syncTopic, boolean replayTopic,
                       Properties kafkaProps) {
        this.topic = Objects.requireNonNull(topic, "topic");
        this.fusekiDispatchPath = fusekiDispatchName;
        this.remoteEndpoint = remoteEndpoint;
        this.syncTopic = syncTopic;
        this.replayTopic = replayTopic;
        this.stateFile = stateFile;
        this.kafkaProps = kafkaProps;
        this.state = State.INIT;

        boolean hasLocalFusekiService = StringUtils.isEmpty(fusekiDispatchName);
        boolean hasRemoteEndpoint = StringUtils.isEmpty(remoteEndpoint);

        if ( hasRemoteEndpoint && hasLocalFusekiService  )
            Log.warn(this, "ConnectorFK built with both Fuseki service name and remote endpoint URL");
        if ( ! hasRemoteEndpoint && ! hasLocalFusekiService )
            Log.warn(this, "ConnectorFK built with no Fuseki service name nor remote endpoint URL");
    }

    public void start() {
        this.state = State.RUNNING;
    }

    public String getTopic() {
        return topic;
    }

    /**
     * The destination of events on the Kafka topic.
     * <p>
     * Either they are dispatched to Fuseki, in the same JVM, and the connector
     * destination is given by {@link #getLocalDispatchPath} or replayed on to a remote
     * endpoint URL given by {@link #getRemoteEndpoint}.
     *
     * @return HTTP path for local dispatch.
     */
    public String getLocalDispatchPath() {
        return fusekiDispatchPath;
    }

    public boolean dispatchLocal() {
        return ! StringUtils.isEmpty(fusekiDispatchPath);
    }

    /**
     * The destination of events on the Kafka topic if remote.
     *
     * @return HTTP URL for remote dispatch.
     */
    public String getRemoteEndpoint() {
        return remoteEndpoint;
    }

    public boolean getSyncTopic() {
        return syncTopic;
    }

    public boolean getReplayTopic() {
        return replayTopic;
    }

    public String getStateFile() {
        return stateFile;
    }

    public Properties getKafkaProps() {
        return kafkaProps;
    }

    @Override
    public String toString() {
        return "ConnectorFK [topic=" + topic + ", fusekiDispatchName=" + fusekiDispatchPath + ", remoteEndpoint=" + remoteEndpoint + ", syncTopic="
               + syncTopic + ", replayTopic=" + replayTopic + ", stateFile=" + stateFile + ", kafkaProps=" + kafkaProps + ", state=" + state
               + "]";
    }
}
