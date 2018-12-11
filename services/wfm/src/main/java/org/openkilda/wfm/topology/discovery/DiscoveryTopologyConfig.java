/* Copyright 2018 Telstra Open Source
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.openkilda.wfm.topology.discovery;

import org.openkilda.wfm.topology.AbstractTopologyConfig;

import com.sabre.oss.conf4j.annotation.Configuration;
import com.sabre.oss.conf4j.annotation.Default;
import com.sabre.oss.conf4j.annotation.IgnoreKey;
import com.sabre.oss.conf4j.annotation.Key;

public interface DiscoveryTopologyConfig extends AbstractTopologyConfig {
    @IgnoreKey
    DiscoveryConfig getDiscoveryConfig();

    default int getScaleFactor() {
        return getDiscoveryConfig().getScaleFactor();
    }

    default int getDiscoveryInterval() {
        return getDiscoveryConfig().getDiscoveryInterval();
    }

    default int getDiscoveryPacketTtl() {
        return getDiscoveryConfig().getDiscoveryPacketTtl();
    }

    default int getDiscoveryTimeout() {
        return getDiscoveryConfig().getDiscoveryTimeout();
    }

    default int getSpeakerFailureTimeoutSeconds() {
        return getDiscoveryConfig().getSpeakerFailureTimeoutSeconds();
    }

    default int getDumpRequestTimeoutSeconds() {
        return getDiscoveryConfig().getDumpRequestTimeoutSeconds();
    }

    default String getTopoDiscoTopic() {
        return getKafkaTopics().getTopoDiscoTopic();
    }

    default String getKafkaSpeakerTopic() {
        return getKafkaTopics().getSpeakerTopic();
    }

    default String getKafkaTopoRerouteTopic() {
        return getKafkaTopics().getTopoRerouteTopic();
    }

    @Key("bfd.port.offset")
    @Default("200")
    int getBfdPortOffset();

    @Key("isl.cost.when.port.down")
    int getIslCostWhenPortDown();

    @Key("speaker.io.timeout.seconds")
    int getSpeakerIoTimeoutSeconds();

    @Configuration
    @Key("discovery")
    interface DiscoveryConfig {
        @Key("scale-factor")
        @Default("2")
        int getScaleFactor();

        @Key("interval")
        int getDiscoveryInterval();

        @Key("packet.ttl")
        int getDiscoveryPacketTtl();

        @Key("timeout")
        int getDiscoveryTimeout();

        @Key("speaker-failure-timeout")
        int getSpeakerFailureTimeoutSeconds();

        @Key("dump-request-timeout-seconds")
        int getDumpRequestTimeoutSeconds();
    }
}
