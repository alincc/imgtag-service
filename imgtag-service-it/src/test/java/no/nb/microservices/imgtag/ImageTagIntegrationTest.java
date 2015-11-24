package no.nb.microservices.imgtag;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import no.nb.microservices.imgtag.config.ApplicationSettings;
import no.nb.microservices.imgtag.config.WebConfig;
import no.nb.microservices.imgtag.model.*;
import no.nb.microservices.imgtag.service.NBUserService;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
@WebAppConfiguration
public class ImageTagIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Mock
    private NBUserService nbUserService;

    private ObjectMapper mapper;
    private MockMvc mockMvc;

    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    private static MongodExecutable _mongodExe;
    private static MongodProcess _mongod;
    private static MongoClient _mongo;
    private static boolean initDb = false;

    public ImageTagIntegrationTest() throws Exception{
        if (!initDb) {
            initDb = true;

            _mongodExe = starter.prepare(new MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .net(new Net("127.0.0.1", WebConfig.MONGO_PORT, Network.localhostIsIPv6()))
                    .build());
            _mongod = _mongodExe.start();
            _mongo = new MongoClient("127.0.0.1", WebConfig.MONGO_PORT);
        }
    }

    @AfterClass
    public static void finalTeardown() throws Exception {
        _mongod.stop();
        _mongodExe.cleanup();
    }

    @Before
    public void setupTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    @WithMockUser
    public void integrationTest1() throws Exception {
        ImageTag imgtag1 = new ImageTag();
        imgtag1.setUrn("URN:NBN:no-nb_digifoto_20140228_00110_NB_WF_EDK_129152");
        imgtag1.setDateCreated(new Date());
        imgtag1.setPointPosition(new PointPosition(1, 1553, 1969));
        imgtag1.setStatus(new Status("approved", "by Alice"));
        imgtag1.setTag(new Tag(new PersonTag("Bob", "Bobsy")));


        ImageTag imgtag2 = new ImageTag();
        imgtag2.setUrn("URN:NBN:no-nb_digifoto_20140228_00110_NB_WF_EDK_129152");
        imgtag2.setDateCreated(new Date());
        imgtag2.setPointPosition(new PointPosition(1, 2514, 1377));
        imgtag1.setTag(new Tag(new ItemTag("Bobs house")));


        GsonBuilder builder = new GsonBuilder();
//         Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        Gson gson = builder.create();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/imgtags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.convertValue(imgtag1, JsonNode.class).toString()))
                .andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();

        ImageTag tag1 = gson.fromJson(result.getResponse().getContentAsString(), ImageTag.class);

        result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/imgtags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.convertValue(imgtag2, JsonNode.class).toString()))
                .andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();

        ImageTag tag2 = gson.fromJson(result.getResponse().getContentAsString(), ImageTag.class);

        // Test get
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/imgtags"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.imageTagList", Matchers.hasSize(2)));

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/imgtags")
                .param("urn", "URN:NBN:no-nb_digifoto_20140228_00110_NB_WF_EDK_129152"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/imgtags")
                .param("urn", "dummyURN"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.page.totalElements", Is.is(0)));;

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/imgtags/{tagid}", tag1.getTagId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.urn", Is.is("URN:NBN:no-nb_digifoto_20140228_00110_NB_WF_EDK_129152")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tagId", Is.is(tag1.getTagId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.dateCreated", Matchers.notNullValue()));

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/imgtags/{tagid}", "dummyID"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        // Test reported
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/imgtags/{tagid}", tag1.getTagId()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/imgtags/{tagid}/report", "dummyid")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\": \"I report this in the name of the king!\"}"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/imgtags/{tagid}/report", tag1.getTagId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\": \"\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/imgtags/{tagid}/report", tag1.getTagId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\": \"I report this in the name of the king!\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/imgtags/{tagid}", tag1.getTagId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.reported", Is.is(true)));

        // Test delete
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/imgtags/{tagid}", "dummyID")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\": \"This is a dummy tag\"}"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/imgtags/{tagid}", tag1.getTagId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\": \"This is a OK tag!\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/imgtags/{tagid}", tag1.getTagId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status.name", Is.is("deleted")));

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/imgtags"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.imageTagList", Matchers.hasSize(2)));

        // Put imgtag back into database
        result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/imgtags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.convertValue(imgtag1, JsonNode.class).toString()))
                .andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();

        tag1 = gson.fromJson(result.getResponse().getContentAsString(), ImageTag.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/imgtags/{tagid}", tag1.getTagId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.urn", Is.is("URN:NBN:no-nb_digifoto_20140228_00110_NB_WF_EDK_129152")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tagId", Is.is(tag1.getTagId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.dateCreated").exists());

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/imgtags"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.imageTagList[0].urn").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.imageTagList[0].tagId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.imageTagList[0].userId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.imageTagList[0].dateCreated").exists());
    }

    @Configuration
    @EnableMongoRepositories
    static class MongoConfiguration extends AbstractMongoConfiguration {

        @Override
        protected String getDatabaseName() {
            return "tagdb";
        }

        @Override
        public Mongo mongo() throws UnknownHostException {
            return new MongoClient("127.0.0.1", WebConfig.MONGO_PORT);
        }

        @Override
        protected String getMappingBasePackage() {
            return "no.nb.microservices.imgtag.repository";
        }
    }

    @Configuration
    static class Config {
        @Bean
        public ApplicationSettings applicationSettings() {
            ApplicationSettings settings = new ApplicationSettings();
            settings.setNbsokContentUrl("http://www.nb.no/nbsok/nb/{sesamid}");
            settings.setFotoContentUrl("http://www.nb.no/foto/nb/{sesamid}");
            return settings;
        }
    }

}
