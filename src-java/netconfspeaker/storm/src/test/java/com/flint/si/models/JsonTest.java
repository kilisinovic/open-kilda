package com.flint.si.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import com.flint.si.models.yang_wrappers.ContainerWrapper;
import com.flint.si.models.yang_wrappers.ListEntryWrapper;
import com.google.gson.stream.JsonReader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class JsonTest {
    static EffectiveModelContext SCHEMA_CONTEXT;
    static JSONCodecFactory lhotkaCodecFactory;
    static String input;
    static NormalizedNode<?,?> root;

    @BeforeClass
    public static void setup() {
        //this also verifies that yang is correct
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/yang");
        lhotkaCodecFactory = JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(SCHEMA_CONTEXT);
        input = getInput();    
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory);
        jsonParser.parse(new JsonReader(new StringReader(input)));
        root = result.getResult();
    }

    static String getInput() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (BufferedInputStream bfs = new BufferedInputStream(classloader.getResourceAsStream("request.json"))) {
            //return new String(bfs.readAllBytes());
        } catch (IOException e) {

        }
        return "";
    }

    //@Test
    public void testSingleKeyForListEntity() {    
        ContainerWrapper wrap = new ContainerWrapper((ContainerNode)root);

        HashMap<String, String> mymap = new HashMap<>();
        mymap.put("value", "123");
        ListEntryWrapper listE = wrap.list("l1").getEntry("value", Uint16.valueOf("123"));
        assertNotNull(listE);
        assertEquals("123", listE.leaf("value").getValue());
    }

    //@Test
    public void testMapKeyForListEntity() {
        ContainerWrapper wrap = new ContainerWrapper((ContainerNode) root);

        HashMap<String, Object> mymap = new HashMap<>();

        mymap.put("value", Uint16.valueOf("123"));
        ListEntryWrapper listE = wrap.list("l1").getEntry(mymap);

        assertNotNull(listE);
        assertEquals("123", listE.leaf("value").getValue());
    }

    //@Test
    public void testNodeForAugmentedEntity() {
        ContainerWrapper wrap = new ContainerWrapper((ContainerNode) root);
        assertNotNull(wrap.container("devices"));
    }

    //@Test
    public void testChainedCalls(){
        ContainerWrapper wrap = new ContainerWrapper((ContainerNode) root);
        assertNotNull(wrap.container("devices"));
        assertNotNull(wrap.container("devices").list("device"));
        assertNotNull(wrap.container("devices").list("device").getEntry("name", "device1"));
        assertNotNull(wrap.container("devices").list("device").getEntry("name", "device1").container("config"));
        assertNotNull(wrap.container("devices").list("device").getEntry("name", "device1").container("config").container("router"));
        assertNotNull(wrap.container("devices").list("device").getEntry("name", "device1").container("config").container("router").container("bgp"));
        assertNotNull(wrap.container("devices").list("device").getEntry("name", "device1").container("config").container("router").container("bgp").list("neighbors"));
        assertNotNull(wrap.container("devices").list("device").getEntry("name", "device1").container("config").container("router").container("bgp").list("neighbors").getEntry("id", "192.4.4.4"));
        assertNotNull(wrap.container("devices").list("device").getEntry("name", "device1").container("config").container("router").container("bgp").list("neighbors").getEntry("id", "192.4.4.4").leaf("remote-as"));
        assertNotNull(wrap.container("devices").list("device").getEntry("name", "device1").container("config").container("router").container("bgp").list("neighbors").getEntry("id", "192.4.4.4").leaf("remote-as").getValue());
    }
}
