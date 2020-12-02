package com.flint.si.models.yang_wrappers;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;

public class ListWrapper implements NormalizedNodeWrapperInterface<MapNode>, Iterable<ListEntryWrapper>{
    MapNode list;

    public ListWrapper(@NotNull MapNode list){
        this.list = requireNonNull(list);
    }

    public ListEntryWrapper getEntry(String key, Object value){
        QName name = QName.create(list.getNodeType().getModule(), key);
        NodeIdentifierWithPredicates id = NodeIdentifierWithPredicates.of(list.getNodeType(), name, value);
        return getEntry(id);
    }

    
    private ListEntryWrapper getEntry(NodeIdentifierWithPredicates id){
        Optional<MapEntryNode> node = list.getChild(id);
        return node.isPresent()?  new ListEntryWrapper(node.get()) : null;
    }

    public ListEntryWrapper getEntry(Map<String, Object> id){
        Map<QName, Object> entityId = new HashMap<>();
        for(String key : id.keySet()){
            entityId.put(QName.create(list.getNodeType().getModule(), key), id.get(key));
        }
        
        return getEntry(NodeIdentifierWithPredicates.of(list.getNodeType(), entityId));
    }

    @Override
    public MapNode unwrap() {
        return list;
    }

    @Override
    public Iterator<ListEntryWrapper> iterator() {
        return new ListWrapperIterator(list);
    }

    private class ListWrapperIterator implements Iterator<ListEntryWrapper>{
        Iterator<MapEntryNode> iterator;

        public ListWrapperIterator(MapNode node){
            iterator = node.getValue().iterator();
        }
        
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public ListEntryWrapper next() {
            return new ListEntryWrapper(iterator.next());
        }
    }
}
