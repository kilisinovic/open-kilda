package com.flint.si.models.yang_wrappers;

import java.util.Iterator;
import java.util.Objects;

import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;

public class LeafListWrapper implements NormalizedNodeWrapperInterface<LeafSetNode<Object>>, Iterable<LeafListEntryWrapper> {
    LeafSetNode<Object> leafList;

    public LeafListWrapper(LeafSetNode<Object> leafList) {
        this.leafList = Objects.requireNonNull(leafList);
    }

    public LeafListEntryWrapper getEntry(int index) {
        if (index > leafList.size() || index < 0) {
            return null;
        }
        int i = 0;
        for (LeafSetEntryNode<?> node : leafList.getValue()) {
            i++;
            if (i == leafList.size()) {
                return new LeafListEntryWrapper(node);
            }
        }
        return null;
    }
    // TODO implement iterable

    @Override
    public LeafSetNode<Object> unwrap() {
        return leafList;
    }

    @Override
    public Iterator<LeafListEntryWrapper> iterator() {
        return new LeafListWrapperIterator(leafList);
    }

    private class LeafListWrapperIterator implements Iterator<LeafListEntryWrapper>{
        Iterator<LeafSetEntryNode<Object>> list;
        
        public LeafListWrapperIterator(LeafSetNode<Object> node){
            this.list = node.getValue().iterator();
        }

        @Override
        public boolean hasNext() {
            return list.hasNext();
        }

        @Override
        public LeafListEntryWrapper next() {
            return new LeafListEntryWrapper(list.next());
        }
    }
}
