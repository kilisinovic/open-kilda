package com.flint.si.models.yang_wrappers;

import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;

public class ListEntryWrapper extends DataContainerWrapper<MapEntryNode> implements ContainerWrapperInterface{
    MapEntryNode entry;

	public ListEntryWrapper(MapEntryNode node) {
        this.entry = requireNonNull(node);
        this.value = this.entry.getValue();
    }

    @Override
    public LeafListWrapper leafList(String localId) {
        LeafSetNode<Object> node = this.getLeafListNode(localId);
        return node != null ? new LeafListWrapper(node): null;
    }

    @Override
    public LeafWrapper leaf(String localId) {
        LeafNode<Object> node = this.getLeafNode(localId);
        return node != null ? new LeafWrapper(node): null;
    }

    @Override
    public ListWrapper list(String localId) {
        MapNode node = this.getListNode(localId);
        return node != null ? new ListWrapper(node): null;
    }

    @Override
    public ContainerWrapper container(String localId) {
        ContainerNode node = this.getContainerNode(localId);
        return node != null ? new ContainerWrapper(node): null;
    }

    @Override
    public MapEntryNode unwrap() {
        return entry;
    }
}
