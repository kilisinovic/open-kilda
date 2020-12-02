package com.flint.si.models.yang_wrappers;

import java.util.Collection;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public abstract class DataContainerWrapper<T> implements NormalizedNodeWrapperInterface<T>{
    protected Collection<DataContainerChild<? extends PathArgument, ?>> value;

    protected LeafNode<Object> getLeafNode(String leafIdLocal){
        for(NormalizedNode<?,?> node : value){
            if(node instanceof AugmentationNode){
                NormalizedNode<?,?> result = getNodeFromAugmentation((AugmentationNode)node, leafIdLocal);
                if(result != null && result instanceof LeafNode<?>){
                    return (LeafNode<Object>)result;
                }
            } else if(node.getNodeType().getLocalName().equals(leafIdLocal) && node instanceof LeafNode){
                return (LeafNode<Object>)node;
            }
        }
        return null;
    }

    protected MapNode getListNode(String listIdLocal){
        for(NormalizedNode<?,?> node : value){
            if(node instanceof AugmentationNode){
                NormalizedNode<?,?> result = getNodeFromAugmentation((AugmentationNode)node, listIdLocal);
                if(result != null && result instanceof MapNode){
                    return (MapNode)result;
                }
            } else if(node.getNodeType().getLocalName().equals(listIdLocal) && node instanceof MapNode){
                return (MapNode)node;
            }
        }
        return null;
    }

    protected ContainerNode getContainerNode(String containerIdLocal){
        for(NormalizedNode<?,?> node : value){
            if(node instanceof AugmentationNode){
                NormalizedNode<?,?> result = getNodeFromAugmentation((AugmentationNode)node, containerIdLocal);
                if(result != null && result instanceof ContainerNode){
                    return (ContainerNode)result;
                }
            } else if(node.getNodeType().getLocalName().equals(containerIdLocal) && node instanceof ContainerNode){
                return (ContainerNode)node;
            }
        }
        return null;
    }

    protected LeafSetNode<Object> getLeafListNode(String leafListIdLocal){
        for(NormalizedNode<?,?> node : value){
            if(node instanceof AugmentationNode){
                NormalizedNode<?,?> result = getNodeFromAugmentation((AugmentationNode)node, leafListIdLocal);
                if(result != null && result instanceof LeafSetNode){
                    return (LeafSetNode<Object>)result;
                }
            } else if(node.getNodeType().getLocalName().equals(leafListIdLocal) && node instanceof LeafSetNode){
                return (LeafSetNode<Object>)node;
            }
        }
        return null;
    }

    protected NormalizedNode<?,?> getNodeFromAugmentation(AugmentationNode augment, String nodeIdLocal){
        for(NormalizedNode<?,?> node : augment.getValue()){
            if(node instanceof AugmentationNode){
                NormalizedNode<?,?> result = getNodeFromAugmentation((AugmentationNode)node, nodeIdLocal);
                if(result != null){
                    return result;
                }
            }
            else if(node.getNodeType().getLocalName().equals(nodeIdLocal)){
                return node;
            }
        }
        return null;
    }
    /* TODO: find out how to use reflection api so i don't have to copy code everywhere

    private NormalizedNode getNodeOfInstance(String id, Class<? extends NormalizedNode<?,?>> requiredClass){
        NormalizedNode<?,?> result = null;
        for(NormalizedNode<?,?> node : value){
            if(node instanceof AugmentationNode){
                result = getNodeFromAugmentation((AugmentationNode)node, id);
                if(result != null && requiredClass.isInstance(result)){
                    return result;
                }
            } else if(node.getNodeType().getLocalName().equals(id) && requiredClass.isInstance(result)){
                return result;
            }
        }
        return null;
    }
    */
}
