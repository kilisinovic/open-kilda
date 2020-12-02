package com.flint.si.models.yang_wrappers;

import java.util.Objects;

import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;

public class LeafListEntryWrapper implements NormalizedNodeWrapperInterface<LeafSetEntryNode<?>>, SimpleNodeInterface {
    LeafSetEntryNode<?> node;

	public LeafListEntryWrapper(LeafSetEntryNode<?> node) {
        this.node = Objects.requireNonNull(node);
    }
    
    public String getValue(){
        return node.getValue().toString();
    }

    @Override
    public LeafSetEntryNode<?> unwrap() {
        return node;
    }
}
