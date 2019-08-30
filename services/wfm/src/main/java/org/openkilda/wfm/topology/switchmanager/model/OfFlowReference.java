/* Copyright 2019 Telstra Open Source
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

package org.openkilda.wfm.topology.switchmanager.model;

import org.openkilda.floodlight.api.FlowSegmentSchema;
import org.openkilda.floodlight.api.OfFlowSchema;
import org.openkilda.messaging.info.rule.FlowEntry;
import org.openkilda.model.SwitchId;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class OfFlowReference {
    private final int tableId;
    private final long cookie;
    private final SwitchId datapath;

    public OfFlowReference(FlowSegmentSchema schema, OfFlowSchema entry) {
        this(entry.getTableId(), entry.getCookie().getValue(), schema.getDatapath());
    }

    public OfFlowReference(SwitchId datapath, FlowEntry entry) {
        this((int) entry.getTableId(), entry.getCookie(), datapath);
    }
}