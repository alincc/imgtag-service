package no.nb.microservices.imgtag.service;

import no.nb.microservices.imgtag.repository.ImageTagRepository;
import no.nb.nbsecurity.NBUserDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * Created by andreasb on 30.06.15.
 */

@RunWith(MockitoJUnitRunner.class)
public class ImageTagServiceTest {

    private ImageTagService imageTagService;

    @Mock
    private ImageTagRepository imageTagRepository;

    @Mock
    private NBUserService nbUserService;

    private static final double DELTA = 1e-15;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        imageTagService = new ImageTagService(nbUserService, imageTagRepository);
    }

    public void loginAsUser(String userId, String role) {
        List<GrantedAuthority> permissions = new ArrayList<GrantedAuthority>();
        permissions.add(new SimpleGrantedAuthority(role));
        NBUserDetails nbUserDetails = new NBUserDetails("sessionID1234", UUID.fromString(userId), "myusername", "mypassword", true, true, true, true, true, permissions);
        when(nbUserService.getNBUser()).thenReturn(nbUserDetails);
    }

    @Test
    public void queryTest() {

    }
}
