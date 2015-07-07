package no.nb.microservices.imgtag.service;

import com.mysema.query.types.expr.BooleanExpression;
import no.nb.microservices.imgtag.config.Constants;
import no.nb.microservices.imgtag.model.*;
import no.nb.microservices.imgtag.repository.ImageTagRepository;
import no.nb.nbsecurity.NBUserDetails;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Created by andreasb on 26.06.15.
 */
@Service
public class ImageTagService implements IImageTagService {

    private static final Logger LOG = LoggerFactory.getLogger(ImageTagService.class);

    private final NBUserService nbUserService;
    private final ImageTagRepository imageTagRepository;

    @Autowired
    public ImageTagService(NBUserService nbUserService, ImageTagRepository imageTagRepository) {
        this.nbUserService = nbUserService;
        this.imageTagRepository = imageTagRepository;
    }

    @Override
    public Page<ImageTag> query(ImageTagQuery imageTagQuery, int page, int size, String[] expand) {

        QImageTag t = QImageTag.imageTag;
        BooleanExpression query = t.urn.isNotNull();

        if (StringUtils.isNotBlank(imageTagQuery.getUrn())) {
            query = query.and(t.urn.eq(imageTagQuery.getUrn()));
        }
        if (StringUtils.isNotBlank(imageTagQuery.getType())) {
            query = query.and(t.type.eq(imageTagQuery.getType()));
        }
        if (StringUtils.isNotBlank(imageTagQuery.getUserId())) {
            query = query.and(t.userId.eq(imageTagQuery.getUserId()));
        }
        if (nbUserService.getNBUser() == null || !nbUserService.getNBUser().getAuthorities().contains(new SimpleGrantedAuthority(Constants.ADMIN_ROLE))) {
            query = query.and(t.status.name.eq("approved"));
        } else if (StringUtils.isNotBlank(imageTagQuery.getStatus())) {
            query = query.and(t.status.name.eq(imageTagQuery.getStatus()));
        }
        if (imageTagQuery.getReported() != null) {
            query = query.and(t.reported.eq(imageTagQuery.getReported()));
        }

        PageRequest pageRequest = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "dateModified"));
        Page<ImageTag> pages = imageTagRepository.findAll(query, pageRequest);

        if (pages != null && (nbUserService.getNBUser() == null || !nbUserService.getNBUser().getAuthorities().contains(new SimpleGrantedAuthority(Constants.ADMIN_ROLE)))) {
            for (ImageTag imageTag : pages.getContent()) {
                imageTag.mask();
            }
        }

        return pages;
    }

    @Override
    public ImageTag findOne(String id, String[] expand) {
        ImageTag imageTag = imageTagRepository.findOne(id);

        if (imageTag != null) {
            if (!nbUserService.getNBUser().getAuthorities().contains(new SimpleGrantedAuthority(Constants.ADMIN_ROLE))) {
                if (!imageTag.getStatus().getName().equals("approved")) {
                    throw new AccessDeniedException("User do not have access to update this object");
                }

                imageTag.mask();
            }

            return imageTag;
        }
        else {
            throw new NoSuchElementException(Constants.IMGTAG_NOT_FOUND);
        }
    }

    @Override
    public void delete(String id) {
        ImageTag imageTag = imageTagRepository.findOne(id);
        NBUserDetails user = nbUserService.getNBUser();

        // Checks if the tag exists or is already deleted
        if (imageTag == null || (imageTag.getStatus() != null && imageTag.getStatus().getName().equals("deleted"))) {
            throw new NoSuchElementException(Constants.IMGTAG_NOT_FOUND);
        }

        // Checks if the user is admin or if it is his own tag
        if ((user.getAuthorities().contains(new SimpleGrantedAuthority(Constants.ADMIN_ROLE))
                || (imageTag.getUserId() != null && imageTag.getUserId().equals(user.getUserId().toString()))))
        {
            imageTag.setReported(false);
            imageTag.setStatus(new Status(user.getUserId().toString(), user.getDisplayName(), "deleted"));
            imageTagRepository.save(imageTag);
        }
        else {
            throw new AccessDeniedException("User do not have access to delete this object");
        }
    }

    @Override
    public ImageTag save(ImageTag imageTag) {
        NBUserDetails user = nbUserService.getNBUser();
        imageTag.setTagId(UUID.randomUUID().toString());
        imageTag.setUserId(user.getUserId().toString());
        imageTag.setUserDisplayName(user.getDisplayName());
        imageTag.setUserEmail(user.getEmail());
        imageTag.setDateCreated(new Date());
        imageTag.setDateModified(new Date());
        imageTag.setStatus(new Status("auto", "auto", "approved"));
        imageTag.setReported(false);
        imageTag.setReports(new ArrayList<Report>());

        ImageTag savedTag = imageTagRepository.save(imageTag);
        LOG.info("ImgTag saved: " + imageTag);

        return savedTag;
    }

    @Override
    public ImageTag update(ImageTag imageTag) {
        NBUserDetails user = nbUserService.getNBUser();

        if (imageTag.getTagId() == null) {
            throw new IllegalArgumentException("ImgTag id cannot be null");
        }

        ImageTag oldTag = imageTagRepository.findOne(imageTag.getTagId());

        // Checks if the user is admin or if it is his own tag
        if ((!user.getAuthorities().contains(new SimpleGrantedAuthority(Constants.ADMIN_ROLE)) && (!oldTag.getUserId().equals(user.getUserId().toString())))) {
            throw new AccessDeniedException("User do not have access to update this object");
        }

        if (user.getAuthorities().contains(new SimpleGrantedAuthority(Constants.ADMIN_ROLE))) {
            ImageTag updatedTag = imageTagRepository.save(imageTag);
            return updatedTag;
        }
        else {
            oldTag.setDateModified(new Date());
            oldTag.setComment((imageTag.getComment() != null) ? imageTag.getComment() : oldTag.getComment());
            oldTag.setPointPosition((imageTag.getPointPosition() != null) ? imageTag.getPointPosition() : oldTag.getPointPosition());
            ImageTag updatedTag = imageTagRepository.save(oldTag);
            return updatedTag;
        }
    }

    @Override
    public ImageTag report(String id, RequestInfo requestInfo) {
        ImageTag imageTag = imageTagRepository.findOne(id);

        if (imageTag == null) {
            throw new NoSuchElementException(Constants.IMGTAG_NOT_FOUND);
        }

        NBUserDetails user = nbUserService.getNBUser();
        Report markerReport = new Report(user.getUserId().toString(), user.getDisplayName(), user.getEmail(), requestInfo.getComment());

        if (imageTag.getReports() == null) {
            imageTag.setReports(new ArrayList<Report>());
        }

        // Checks if the user has already reported this tag
        for (Report report : imageTag.getReports()) {
            if (report.getUserID().equalsIgnoreCase(user.getUserId().toString())) {
                throw new AccessDeniedException("User have already reported this object");
            }
        }

        imageTag.getReports().add(markerReport);
        imageTag.setReported(true);
        ImageTag savedImageTag = imageTagRepository.save(imageTag);

        return savedImageTag;
    }
}
