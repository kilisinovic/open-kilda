package com.flint.si.models.yang_wrappers;

import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;

//TODO
public class LeafWrapper implements NormalizedNodeWrapperInterface<LeafNode<?>>, SimpleNodeInterface{
    LeafNode<?> leaf;

	public LeafWrapper(LeafNode<?> leaf) {
        this.leaf = leaf;
	}

    @Override
    public LeafNode<?> unwrap() {
        return leaf;
    }

    @Override
    public String getValue() {
        return leaf.getValue().toString();
    }   
    
}
