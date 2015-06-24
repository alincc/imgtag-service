package no.nb.microservices.imgtag.rest.controller;

import no.nb.microservices.imgtag.config.ApplicationSettings;
import no.nb.microservices.imgtag.repository.ImgTagRepository;
import no.nb.microservices.imgtag.rest.assembler.ImgTagResourceAssembler;
import no.nb.microservices.imgtag.service.NBUserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class ImgTagControllerTest {

    @Mock
    private NBUserService nbUserService;

    @Mock
    private ImgTagRepository imgTagRepository;

    @Mock
    private ImgTagResourceAssembler imgTagResourceAssembler;

    @Mock
    private ApplicationSettings applicationSettings;

    ImgTagController imgTagController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        imgTagController = new ImgTagController(nbUserService, imgTagRepository, imgTagResourceAssembler, applicationSettings);
        mockMvc = MockMvcBuilders.standaloneSetup(imgTagController).build();
    }

    @Test
    public void helloWorldTest() throws Exception{
        mockMvc.perform(get("/imgtags"))
                .andExpect(status().isOk());
    }

}
