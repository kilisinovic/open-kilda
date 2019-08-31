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

package org.openkilda.floodlight.feature;

import org.openkilda.messaging.model.SpeakerSwitchView;
import org.openkilda.messaging.model.SpeakerSwitchView.Feature;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.SwitchDescription;

import java.util.Optional;

// TODO(surabujin) must be replaces with OFTableFeatures check
public class NoviFlowCopyFieldFeature extends NoviflowSpecificFeature {
    @Override
    public Optional<SpeakerSwitchView.Feature> discover(IOFSwitch sw) {
        if (is100GbHw(sw)) {
            return Optional.of(Feature.NOVIFLOW_COPY_FIELD);
        }
        return Optional.empty();
    }
}
