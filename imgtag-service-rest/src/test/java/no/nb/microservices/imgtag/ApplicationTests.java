package no.nb.microservices.imgtag;

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
import no.nb.microservices.imgtag.model.ImgTag;
import no.nb.microservices.imgtag.model.Person;
import no.nb.microservices.imgtag.model.Point;
import no.nb.microservices.imgtag.model.Status;
import no.nb.microservices.imgtag.repository.ImgTagRepository;
import no.nb.microservices.imgtag.rest.assembler.ImgTagResourceAssembler;
import no.nb.microservices.imgtag.rest.controller.ImgTagController;
import no.nb.microservices.imgtag.service.NBUserService;
import no.nb.nbsecurity.NBUserDetails;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class ApplicationTests {

    public static final String USER_ID = "b62eb09d-dbf2-495a-8872-7d16e6911296";
    public static final String USER_ID_2 = "edcf81fd-26ce-43a5-abca-e30a01397b39";

    @Autowired
    private ImgTagRepository imgTagRepository;

    @Autowired
    private ApplicationSettings applicationSettings;

    @Mock
    private NBUserService nbUserService;

    private ObjectMapper mapper;
    private ImgTagController imgTagController;
    private MockMvc mockMvc;

    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    private static MongodExecutable _mongodExe;
    private static MongodProcess _mongod;
    private static MongoClient _mongo;
    private static boolean initDb = false;

    public ApplicationTests() throws Exception{
        if (!initDb) {
            initDb = true;

            _mongodExe = starter.prepare(new MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .net(new Net("127.0.0.1" ,12345, Network.localhostIsIPv6()))
                    .build());
            _mongod = _mongodExe.start();
            _mongo = new MongoClient("127.0.0.1", 12345);
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
        imgTagController = new ImgTagController(nbUserService, imgTagRepository, new ImgTagResourceAssembler(applicationSettings), applicationSettings);
        mockMvc = MockMvcBuilders.standaloneSetup(imgTagController).build();
    }

    @After
    public void teardown() throws Exception {
        imgTagRepository.deleteAll();
    }

    @Test
    public void integrationTest1() throws Exception {
        List<GrantedAuthority> permissions = new ArrayList<GrantedAuthority>();
        permissions.add(new SimpleGrantedAuthority("ROLE_TagsAdmin"));
        NBUserDetails nbUserDetails = new NBUserDetails("sessionID1234", UUID.fromString(USER_ID), "myusername", "mypassword", true, true, true, true, true, permissions);
        when(nbUserService.getNBUser()).thenReturn(nbUserDetails);

        ImgTag imgtag1 = new ImgTag();
        imgtag1.setUrn("URN:NBN:no-nb_digifoto_20140228_00110_NB_WF_EDK_129152");
        imgtag1.setSesamId("41259a0179423d523dfbe98cd97a2797");
        imgtag1.setType("point");
        imgtag1.setDate(new Date());
        imgtag1.setPoint(new Point("Huset til alfred", 1, 1553, 1969));
        imgtag1.setStatus(new Status("approved", "by jesus"));

        ImgTag imgtag2 = new ImgTag();
        imgtag2.setUrn("URN:NBN:no-nb_digifoto_20140228_00110_NB_WF_EDK_129152");
        imgtag2.setSesamId("41259a0179423d523dfbe98cd97a2797");
        imgtag2.setType("person");
        imgtag2.setDate(new Date());
        imgtag2.setPoint(new Point("", 1, 2514, 1377));
        imgtag2.setPerson(new Person("Person", "", "", null, null));


        GsonBuilder builder = new GsonBuilder();
//         Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        Gson gson = builder.create();

        MvcResult result = mockMvc.perform(post("/imgtags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.convertValue(imgtag1, JsonNode.class).toString()))
                .andExpect(status().isCreated()).andReturn();

        ImgTag tag1 = gson.fromJson(result.getResponse().getContentAsString(), ImgTag.class);

        result = mockMvc.perform(post("/imgtags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.convertValue(imgtag2, JsonNode.class).toString()))
                .andExpect(status().isCreated()).andReturn();

        ImgTag tag2 = gson.fromJson(result.getResponse().getContentAsString(), ImgTag.class);

        // Test get
        mockMvc.perform(get("/imgtags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        mockMvc.perform(get("/imgtags")
                .param("urn", "URN:NBN:no-nb_digifoto_20140228_00110_NB_WF_EDK_129152"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/imgtags")
                .param("urn", "dummyURN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));;

        mockMvc.perform(get("/imgtags/{tagid}", tag1.getTagId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urn", is("URN:NBN:no-nb_digifoto_20140228_00110_NB_WF_EDK_129152")))
                .andExpect(jsonPath("$.id", is(tag1.getTagId())))
                .andExpect(jsonPath("$.userId", is(USER_ID)))
                .andExpect(jsonPath("$.date", notNullValue()));

        mockMvc.perform(get("/imgtags/{tagid}", "dummyID"))
                .andExpect(status().isNotFound());

        // Test reported
        mockMvc.perform(get("/imgtags/{tagid}", tag1.getTagId()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/imgtags/{tagid}/report", "dummyid")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\": \"I report this in the name of the king!\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/imgtags/{tagid}/report", tag1.getTagId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\": \" \"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/imgtags/{tagid}/report", tag1.getTagId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\": \"I report this in the name of the king!\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/imgtags/{tagid}", tag1.getTagId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reported", is(true)));

        // Test delete
        mockMvc.perform(delete("/imgtags/{tagid}", "dummyID")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\": \"This is a dummy tag\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/imgtags/{tagid}", tag1.getTagId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\": \"This is a OK tag!\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/imgtags/{tagid}", tag1.getTagId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.name", is("deleted")));

        mockMvc.perform(get("/imgtags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));


        // Change user
        permissions = new ArrayList<GrantedAuthority>();
        permissions.add(new SimpleGrantedAuthority("ROLE_USER"));
        nbUserDetails = new NBUserDetails("sessionID1234", UUID.fromString(USER_ID), "myusername", "mypassword", true, true, true, true, true, permissions);
        when(nbUserService.getNBUser()).thenReturn(nbUserDetails);

        // Put imgtag back into database
        result = mockMvc.perform(post("/imgtags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.convertValue(imgtag1, JsonNode.class).toString()))
                .andExpect(status().isCreated()).andReturn();

        tag1 = gson.fromJson(result.getResponse().getContentAsString(), ImgTag.class);

        mockMvc.perform(get("/imgtags/{tagid}", tag1.getTagId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urn", is("URN:NBN:no-nb_digifoto_20140228_00110_NB_WF_EDK_129152")))
                .andExpect(jsonPath("$.id", is(tag1.getTagId())))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.date").exists());

        mockMvc.perform(get("/imgtags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].urn").exists())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].userId").exists())
                .andExpect(jsonPath("$.content[0].date").exists());
    }

    @Configuration
    @EnableMongoRepositories
    @ComponentScan(basePackageClasses = { ImgTagRepository.class })
    static class MongoConfiguration extends AbstractMongoConfiguration {

        @Override
        protected String getDatabaseName() {
            return "tagdb";
        }

        @Override
        public Mongo mongo() throws UnknownHostException {
            return new MongoClient("127.0.0.1", 12345);
        }

        @Override
        protected String getMappingBasePackage() {
            return "no.nb.tag.repository";
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
