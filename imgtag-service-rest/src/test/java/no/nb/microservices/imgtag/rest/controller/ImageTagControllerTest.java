package no.nb.microservices.imgtag.rest.controller;

import no.nb.microservices.imgtag.config.ApplicationSettings;
import no.nb.microservices.imgtag.model.*;
import no.nb.microservices.imgtag.repository.ImageTagRepository;
import no.nb.microservices.imgtag.rest.assembler.ImageTagPage;
import no.nb.microservices.imgtag.rest.assembler.ImageTagResourceAssembler;
import no.nb.microservices.imgtag.service.IImageTagService;
import no.nb.microservices.imgtag.service.NBUserService;
import no.nb.nbsecurity.NBUserDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.awt.*;
import java.util.*;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;;

@RunWith(MockitoJUnitRunner.class)
public class ImageTagControllerTest {

    @Mock
    private NBUserService nbUserService;

    @Mock
    private ImageTagRepository imageTagRepository;

    @Mock
    private IImageTagService imageTagService;

    @Mock
    private ImageTagResourceAssembler imageTagResourceAssembler;

    @Mock
    private ApplicationSettings applicationSettings;

    ImageTagController imageTagController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        imageTagController = new ImageTagController(imageTagRepository, imageTagService, imageTagResourceAssembler);
        mockMvc = MockMvcBuilders.standaloneSetup(imageTagController).build();
    }

    @Test
    public void helloWorldTest() throws Exception{
        mockMvc.perform(get("/imgtags"))
                .andExpect(status().isOk());
    }

}
