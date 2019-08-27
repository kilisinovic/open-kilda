/*
 * Copyright 2019 Telstra Open Source
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

package org.openkilda.wfm.topology.switchmanager.bolt.speaker;

import org.openkilda.floodlight.api.request.FlowSegmentBlankGenericResolver;
import org.openkilda.floodlight.api.request.MetersDumpRequest;
import org.openkilda.floodlight.api.request.TableDumpRequest;
import org.openkilda.floodlight.api.response.SpeakerErrorResponse;
import org.openkilda.floodlight.api.response.SpeakerFlowSegmentSchemaResponse;
import org.openkilda.floodlight.api.response.SpeakerResponse;
import org.openkilda.floodlight.flow.response.FlowErrorResponse;
import org.openkilda.messaging.Message;
import org.openkilda.messaging.MessageContext;
import org.openkilda.model.SwitchId;
import org.openkilda.wfm.topology.switchmanager.model.FlowSegmentSchemaRequestResponse;
import org.openkilda.wfm.topology.switchmanager.model.SwitchOfMeterDump;
import org.openkilda.wfm.topology.switchmanager.model.SwitchOfTableDump;
import org.openkilda.wfm.topology.switchmanager.service.SpeakerWorkerCarrier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SchemaFetchHandler extends WorkerHandler {
    private final SpeakerWorkerCarrier carrier;
    private final MessageContext context;

    private final SwitchId switchId;

    private final Map<UUID, FlowSegmentBlankGenericResolver> requestBlanks = new HashMap<>();
    private final Map<UUID, FlowSegmentSchemaRequestResponse> ofSchema = new HashMap<>();

    private final Map<Integer, UUID> tableRequests = new HashMap<>();
    private final List<SwitchOfTableDump> tableDumps = new ArrayList<>();

    private UUID metersRequest = null;
    private SwitchOfMeterDump meterDump = null;

    public SchemaFetchHandler(
            SpeakerWorkerCarrier carrier, SwitchId switchId, List<FlowSegmentBlankGenericResolver> schemaRequests) {
        this.carrier = carrier;
        this.context = (new MessageContext(carrier.getCommandContext().getCorrelationId()))
                .fork("schema").fork(switchId.toString());

        this.switchId = switchId;

        for (FlowSegmentBlankGenericResolver entry : schemaRequests) {
            carrier.sendSpeakerCommand(entry.makeSchemaRequest());
            requestBlanks.put(entry.getCommandId(), entry);
        }

        // force table 0 dump (to get current system/default OF flows)
        requestOfTableDump(0);
    }

    @Override
    public void speakerResponse(Message response) {
        throw new UnsupportedOperationException(String.format(
                "Reject %s speaker response, ony %s based responses accepted/expected",
                response.getClass().getName(), SpeakerResponse.class.getName()));
    }

    @Override
    public void speakerResponse(SpeakerResponse response) {
        // TODO
        if (response instanceof SpeakerFlowSegmentSchemaResponse) {
            handleSpeakerResponse((SpeakerFlowSegmentSchemaResponse) response);
        } else if (response instanceof FlowErrorResponse) {
            handleSpeakerResponse((FlowErrorResponse) response);
        } else {
            throw new UnsupportedOperationException(String.format(
                    "Reject %s speaker response (unexpected/unsupported response type)",
                    response.getClass().getName()));
        }
    }

    @Override
    public void timeout() {
        carrier.sendHubValidationError(null);
    }

    @Override
    public boolean isCompleted() {
        if (requestBlanks.size() != ofSchema.size()) {
            return false;
        }
        if (tableRequests.size() != tableDumps.size()) {
            return false;
        }
        return metersRequest == null || meterDump != null;
    }

    private void handleSpeakerResponse(SpeakerFlowSegmentSchemaResponse schemaResponse) {
        // TODO
    }

    private void handleSpeakerResponse(FlowErrorResponse error) {
        carrier.sendHubValidationError(error);
    }

    private void requestOfTableDump(Integer tableId) {
        if (tableRequests.containsKey(tableId)) {
            return;
        }

        TableDumpRequest dumpRequest = new TableDumpRequest(
                context.fork(String.valueOf(tableId)), switchId, UUID.randomUUID(), tableId);
        carrier.sendSpeakerCommand(dumpRequest);

        tableRequests.put(tableId, dumpRequest.getCommandId());
    }

    private void requestMetersDump() {
        if (metersRequest != null) {
            return;
        }

        MetersDumpRequest dumpRequest = new MetersDumpRequest(context.fork("meters"), switchId, UUID.randomUUID());
        carrier.sendSpeakerCommand(dumpRequest);

        metersRequest = dumpRequest.getCommandId();
    }
}
