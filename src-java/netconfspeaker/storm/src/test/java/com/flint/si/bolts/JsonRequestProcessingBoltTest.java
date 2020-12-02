package com.flint.si.bolts;

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringReader;

import com.flint.si.models.yang_wrappers.ContainerWrapper;
import com.google.gson.stream.JsonReader;

import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRequestProcessingBoltTest {
    EffectiveModelContext SCHEMA_CONTEXT;
    JSONCodecFactory lhotkaCodecFactory;
    JsonRequestProcessingBolt bolt;
    Logger log = LoggerFactory.getLogger(JsonRequestProcessingBoltTest.class);
    public JsonRequestProcessingBoltTest(){
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/yang");
        lhotkaCodecFactory = JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(SCHEMA_CONTEXT);
    }

    @Nested
    public class AddDeviceTest {
        
        String input;
        NormalizedNode<?, ?> root;

        public AddDeviceTest() {
            // this also verifies that yang is correct
            input = getInput();
            log.info("INPUT " + input);

            final NormalizedNodeResult result = new NormalizedNodeResult();
            final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

            final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory);
            jsonParser.parse(new JsonReader(new StringReader(input)));
            root = result.getResult();
        }

        @BeforeEach
        void initBolt(){
            bolt = new JsonRequestProcessingBolt("1.1.1.1");
        }

        String getInput() {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            try (BufferedInputStream bfs = new BufferedInputStream(classloader.getResourceAsStream("add_device.json"))) {
                return new String(bfs.readAllBytes());
            } catch (IOException e) {
                System.out.println("dumbfuck");
            }
            return "";
        }

        @Test
        public void convertToXMLTest(){
            ContainerWrapper c =new ContainerWrapper((ContainerNode)root);
            String xml = bolt.generateXmlFromNode(c,null);
            System.out.println(xml);
            assertTrue(true);
        }


        @Test
        public void convertDevicesToXMLTest(){
            ContainerWrapper c =new ContainerWrapper((ContainerNode)root);
            String xml = bolt.generateXmlFromNode(c,"/request:request/dev3:devices/dev3:device[dev3:name=\"device1\"]");
            System.out.println(xml);
            assertTrue(true);
        }
    }
}
