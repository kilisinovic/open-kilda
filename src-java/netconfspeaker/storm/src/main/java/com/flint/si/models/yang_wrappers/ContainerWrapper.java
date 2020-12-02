package com.flint.si.models.yang_wrappers;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;

public class ContainerWrapper extends DataContainerWrapper<ContainerNode> implements ContainerWrapperInterface {
    ContainerNode container;

    public ContainerWrapper(@NotNull ContainerNode container){
        Objects.requireNonNull(container);
        this.container = container;
        this.value = container.getValue();
    }

    @Override
    public ContainerNode unwrap() {
        return container;
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
}
