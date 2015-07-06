package no.nb.microservices.imgtag.config;

import no.nb.microservices.imgtag.service.NBUserService;
import no.nb.nbsecurity.NBUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by andreasb on 03.07.15.
 */
@Service
public class MockNBUserService extends NBUserService {

    public static final String USER_ID = "b62eb09d-dbf2-495a-8872-7d16e6911296";
    public static final String USER_ID_2 = "edcf81fd-26ce-43a5-abca-e30a01397b39";

    @Override
    public NBUserDetails getNBUser() {
        List<GrantedAuthority> permissions = new ArrayList<GrantedAuthority>();
        permissions.add(new SimpleGrantedAuthority("ROLE_TagsAdmin"));
        permissions.add(new SimpleGrantedAuthority("ROLE_USER"));
        NBUserDetails nbUserDetails = new NBUserDetails("sessionID1234", UUID.fromString(USER_ID), "myusername", "mypassword", true, true, true, true, true, permissions);
        return nbUserDetails;
    }
}
