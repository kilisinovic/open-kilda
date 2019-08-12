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

package org.openkilda.wfm.topology.flowhs.fsm.reroute.actions;

import org.openkilda.floodlight.flow.request.GetInstalledRule;
import org.openkilda.floodlight.api.request.SpeakerTransitActRequest;
import org.openkilda.floodlight.api.request.AbstractSpeakerActRequest;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteContext;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteFsm;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteFsm.Event;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteFsm.State;

import lombok.extern.slf4j.Slf4j;
import org.squirrelframework.foundation.fsm.AnonymousAction;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class DumpNonIngressRulesAction extends
        AnonymousAction<FlowRerouteFsm, State, Event, FlowRerouteContext> {

    @Override
    public void execute(State from, State to,
                        Event event, FlowRerouteContext context, FlowRerouteFsm stateMachine) {
        log.debug("Validating installed non ingress rules for the flow {}",
                stateMachine.getFlowId());

        Map<UUID, SpeakerTransitActRequest> nonIngressCommands = stateMachine.getNonIngressCommands();

        if (nonIngressCommands.isEmpty()) {
            log.debug("No need to validate non ingress rules for one switch flow");

            stateMachine.fire(Event.RULES_VALIDATED);
        } else {
            Collection<GetInstalledRule> dumpFlowRules = nonIngressCommands.values().stream()
                    .map(command -> new GetInstalledRule(command.getMessageContext(), command.getCommandId(),
                            command.getFlowId(), command.getSwitchId(), command.getCookie()))
                    .collect(Collectors.toList());

            dumpFlowRules.forEach(command -> stateMachine.getCarrier().sendSpeakerRequest(command));
            Set<UUID> commandIds = dumpFlowRules.stream()
                    .map(AbstractSpeakerActRequest::getCommandId)
                    .collect(Collectors.toSet());
            stateMachine.setPendingCommands(commandIds);
        }
    }
}
