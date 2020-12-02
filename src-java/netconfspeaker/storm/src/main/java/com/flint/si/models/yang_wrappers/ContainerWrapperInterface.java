package com.flint.si.models.yang_wrappers;

public interface ContainerWrapperInterface {
    LeafListWrapper leafList(String localId);
    
    LeafWrapper leaf(String localId);
    
    ListWrapper list(String localId);
    
    ContainerWrapper container(String localId);
}
