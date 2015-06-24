package no.nb.microservices.imgtag.rest.controller;

import com.mysema.query.types.expr.BooleanExpression;
import no.nb.microservices.imgtag.config.ApplicationSettings;
import no.nb.microservices.imgtag.model.*;
import no.nb.microservices.imgtag.repository.ImgTagRepository;
import no.nb.microservices.imgtag.rest.assembler.ImgTagPage;
import no.nb.microservices.imgtag.rest.assembler.ImgTagResourceAssembler;
import no.nb.microservices.imgtag.service.NBUserService;
import no.nb.nbsecurity.NBUserDetails;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.Boolean;import java.lang.Exception;import java.lang.String;import java.lang.Void;import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


/**
 * Created by Andreas Bjørnådal (andreasb) on 19.08.14.
 */
@RestController
@RequestMapping("/imgtags")
public class ImgTagController {

    private final static String ADMIN_ROLE = "ROLE_TagsAdmin";

    final Logger LOG = LoggerFactory.getLogger(ImgTagController.class);

    private final NBUserService nbUserService;
    private final ImgTagRepository imgTagRepository;
    private final ImgTagResourceAssembler assembler;
    private final ApplicationSettings applicationSettings;

    @Autowired
    public ImgTagController(NBUserService nbUserService, ImgTagRepository imgTagRepository, ImgTagResourceAssembler assembler, ApplicationSettings applicationSettings) {
        this.nbUserService = nbUserService;
        this.imgTagRepository = imgTagRepository;
        this.assembler = assembler;
        this.applicationSettings = applicationSettings;
    }

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<PagedResources<ImgTag>> getImgTags(
            @RequestParam(value = "urn", required = false) String urn,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "user", required = false) String user,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "reported", required = false) Boolean reported,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size)
    {
        QImgTag t = new QImgTag("ImgTag");
        BooleanExpression query = t.urn.isNotNull();

        if (StringUtils.isNotBlank(urn)) {
            query = query.and(t.urn.eq(urn));
        }
        if (StringUtils.isNotBlank(type)) {
            query = query.and(t.type.eq(type));
        }
        if (StringUtils.isNotBlank(user)) {
            query = query.and(t.userId.eq(user));
        }
        if (nbUserService.getNBUser() == null || !nbUserService.getNBUser().getAuthorities().contains(new SimpleGrantedAuthority(ADMIN_ROLE))) {
            query = query.and(t.status.status.eq("approved"));
        }
        else if (StringUtils.isNotBlank(status)) {
            query = query.and(t.status.status.eq(status));
        }
        if (reported != null) {
            query = query.and(t.reported.eq(reported));
        }

        PageRequest pageRequest = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "date"));
        Page<ImgTag> pages = imgTagRepository.findAll(query, pageRequest);

        if (pages != null && (nbUserService.getNBUser() == null || !nbUserService.getNBUser().getAuthorities().contains(new SimpleGrantedAuthority(ADMIN_ROLE)))) {
            for (ImgTag imgTag : pages.getContent()) {
                imgTag.mask();
            }
        }

        PagedResources<ImgTag> pagedResources = assembler.toResource(new ImgTagPage(pages, urn, type, user, reported, status));
        return new ResponseEntity<PagedResources<ImgTag>>(pagedResources, HttpStatus.OK);
    }

    @RequestMapping(value = "/{imgTagID}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ImgTag> getById(@PathVariable String imgTagID) {
        ImgTag imgTag = imgTagRepository.findOne(imgTagID);

        if (imgTag != null) {
            if (!nbUserService.getNBUser().getAuthorities().contains(new SimpleGrantedAuthority(ADMIN_ROLE))) {
                if (!imgTag.getStatus().getStatus().equals("approved")) {
                    return new ResponseEntity<ImgTag>(HttpStatus.FORBIDDEN);
                }

                imgTag.mask();
            }

            imgTag.add(linkTo(methodOn(ImgTagController.class).getById(imgTagID)).withSelfRel());
            return new ResponseEntity<ImgTag>(imgTag, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<ImgTag>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImgTag> saveTag(@Valid @RequestBody ImgTag imgTag) {
    	if (imgTag.getTagId() == null || imgTag.getTagId().isEmpty()) {
    		imgTag.setTagId(UUID.randomUUID().toString());
    	}

        NBUserDetails user = nbUserService.getNBUser();
        imgTag.setUserId(user.getUserId().toString());
        imgTag.setUserDisplayName(user.getDisplayName());
        imgTag.setUserEmail(user.getEmail());
        imgTag.setDate(new Date());
        imgTag.setStatus(new Status(user.getUserId().toString(), user.getDisplayName(), "approved", ""));

        LOG.info("Saving tag" + imgTag);
        imgTagRepository.save(imgTag);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new UriTemplate("/imgtags/{imgtagid}").expand(imgTag.getTagId()));

        return new ResponseEntity<ImgTag>(imgTag, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ROLE_TagsAdmin')")
    @RequestMapping(value = "/{imgTagID}/restore", method = RequestMethod.PUT)
    public ResponseEntity<ImgTag> undelete(@PathVariable(value = "imgTagID") String id,
                                           @RequestBody RequestInfo requestInfo)
    {
        ImgTag imgTag = imgTagRepository.findOne(id);
        if (imgTag == null) {
            return new ResponseEntity<ImgTag>(HttpStatus.NOT_FOUND);
        }

        if (imgTag.getStatus() != null) {
            if (!imgTag.getStatus().getStatus().equalsIgnoreCase("deleted")) {
                return new ResponseEntity<ImgTag>(HttpStatus.BAD_REQUEST);
            }

            imgTag.getStatusHistory().add(imgTag.getStatus());
        }

        NBUserDetails user = nbUserService.getNBUser();
        imgTag.setStatus(new Status(user.getUserId().toString(), user.getDisplayName(), "approved", requestInfo.getComment()));
        imgTagRepository.save(imgTag);
        return new ResponseEntity<ImgTag>(imgTag, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/{imgTagID}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteTag(
            @PathVariable(value = "imgTagID") String id,
            @RequestBody RequestInfo requestInfo)
    {
        if (requestInfo.getComment() != null && requestInfo.getComment().length() > 160) {
            return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
        }

        ImgTag imgTag = imgTagRepository.findOne(id);
        NBUserDetails user = nbUserService.getNBUser();

        // Checks if the tag exists or is already deleted
        if (imgTag == null || (imgTag.getStatus() != null && imgTag.getStatus().getStatus().equals("deleted"))) {
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }

        // Checks if the user is admin or if it is his own tag
        if ((user.getAuthorities().contains(new SimpleGrantedAuthority(ADMIN_ROLE))
                || (imgTag.getUserId() != null && imgTag.getUserId().equals(user.getUserId().toString()))))
        {
            if (imgTag.getStatusHistory() == null) {
                imgTag.setStatusHistory(new ArrayList<Status>());
            }
            if (imgTag.getStatus() != null) {
                imgTag.getStatusHistory().add(imgTag.getStatus());
            }

            imgTag.setReported(false);
            imgTag.setStatus(new Status(user.getUserId().toString(), user.getDisplayName(), "deleted", requestInfo.getComment()));
            imgTagRepository.save(imgTag);

            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        else {
            return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/{imgTagID}/delete", method = RequestMethod.PUT)
    public ResponseEntity<Void> deleteTagPut(
            @PathVariable(value = "imgTagID") String id,
            @RequestBody RequestInfo requestInfo) {
        return deleteTag(id, requestInfo);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/{imgTagID}/report", method = { RequestMethod.PUT, RequestMethod.POST })
    public ResponseEntity<String> reportTag (
            @PathVariable(value = "imgTagID") String id,
            @RequestBody RequestInfo requestInfo)
    {
        if (StringUtils.isBlank(requestInfo.getComment()) || requestInfo.getComment().length() > 160) {
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }

        ImgTag imgTag = imgTagRepository.findOne(id);

        if (imgTag == null) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }

        NBUserDetails user = nbUserService.getNBUser();
        Report markerReport = new Report(user.getUserId().toString(), user.getDisplayName(), user.getEmail(), requestInfo.getComment());

        if (imgTag.getReports() == null) {
            imgTag.setReports(new ArrayList<Report>());
        }

        // Checks if the user has already reported this tag
        for (Report report : imgTag.getReports()) {
            if (report.getUserID().equalsIgnoreCase(user.getUserId().toString())) {
                return new ResponseEntity<String>(HttpStatus.ALREADY_REPORTED);
            }
        }

        imgTag.getReports().add(markerReport);
        imgTag.setReported(true);
        imgTagRepository.save(imgTag);

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_TagsAdmin')")
    @RequestMapping(value = "/{imgTagID}/approve", method = RequestMethod.PUT)
    public ResponseEntity<String> approveTag (@PathVariable(value = "imgTagID") String id)
    {
        ImgTag imgTag = imgTagRepository.findOne(id);

        if (imgTag == null) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }
        else if (imgTag != null && !imgTag.getReported()) {
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }

        imgTag.setReported(false);
        imgTagRepository.save(imgTag);

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "It looks like we have a internal error in our application. The error have been logged and will be looked at by our development team.")
    public void defaultHandler(HttpServletRequest req, Exception e) {

        // Build Header string
        StringBuilder headers = new StringBuilder();
        for (String headerKey : Collections.list(req.getHeaderNames())) {
            String headerValue = req.getHeader(headerKey);
            headers.append(headerKey + ": " + headerValue + ", ");
        }

        LOG.error("" +
                "Got an unexcepted exception.\n" +
                "Context Path: " + req.getContextPath() + "\n" +
                "Request URI: " + req.getRequestURI() + "\n" +
                "Query String: " + req.getQueryString() + "\n" +
                "Method: " + req.getMethod() + "\n" +
                "Headers: " + headers + "\n" +
                "Auth Type: " + req.getAuthType() + "\n" +
                "Remote User: " + req.getRemoteUser() + "\n" +
                "Username: " + ((req.getUserPrincipal()  != null) ? req.getUserPrincipal().getName() : "Anonymous") + "\n"
                , e);
    }
}
