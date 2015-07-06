package no.nb.microservices.imgtag.service;

import no.nb.nbsecurity.NBUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by Andreas Bjørnådal (andreasb) on 19.08.14.
 */
@Service
public class NBUserService {

    private static final Logger LOG = LoggerFactory.getLogger(NBUserService.class);
    public static final String USER_ID = "b62eb09d-dbf2-495a-8872-7d16e6911296";

    public NBUserDetails getNBUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof NBUserDetails) {
                return (NBUserDetails)principal;
            }
            else {
                LOG.error("Principal is not instance of " + NBUserDetails.class.getName() + " was - " + principal.getClass() + "( " + principal + " )");
            }
        }
        return null;
    }
}


