/* Copyright 2017 Telstra Open Source
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

package org.openkilda.messaging.command.discovery;

import org.openkilda.messaging.command.CommandData;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 * Defines the payload payload of a Message representing an command for ISL discovery.
 */
@Value
public class DiscoverIslCommandData extends CommandData {
    /**
     * Serialization version number constant.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Switch id.
     */
    @JsonProperty("switch_id")
    private String switchId;

    /**
     * Port number.
     */
    @JsonProperty("port_no")
    private int portNo;

    /**
     * Instance constructor.
     *
     * @param switchId switch id
     * @param portNo   port number
     */
    @JsonCreator
    public DiscoverIslCommandData(@JsonProperty("switch_id") final String switchId,
                                  @JsonProperty("port_no") final int portNo) {
        this.switchId = switchId;
        this.portNo = portNo;
    }
}
