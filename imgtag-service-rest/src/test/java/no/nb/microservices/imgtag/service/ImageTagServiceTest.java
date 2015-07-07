package no.nb.microservices.imgtag.service;

import com.mysema.query.types.expr.BooleanExpression;
import no.nb.microservices.imgtag.config.Constants;
import no.nb.microservices.imgtag.model.*;
import no.nb.microservices.imgtag.repository.ImageTagRepository;
import no.nb.nbsecurity.NBUserDetails;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;


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

    private void loginAsUser(String userId, String role) {
        List<GrantedAuthority> permissions = new ArrayList<GrantedAuthority>();
        permissions.add(new SimpleGrantedAuthority(role));
        NBUserDetails nbUserDetails = new NBUserDetails("sessionID1234", UUID.fromString(userId), "myusername", "mypassword", true, true, true, true, true, permissions);
        when(nbUserService.getNBUser()).thenReturn(nbUserDetails);
    }

    private List<ImageTag> getImageTags() {
        List<ImageTag> imageTags = new ArrayList<>();
        imageTags.add(new ImageTag() {{
            setTagId("809530cd-1e83-4293-b313-9583e80c50a");
            setUrn("URN:NBN:no-nb_foto_NF.W_50121");
            setUserId("f6355c31-76a3-48b1-9905-1400fc27be77");
            setUserDisplayName("Bob");
            setUserEmail("bob@example.com");
            setDateCreated(DateTime.parse("2015-07-06").toDate());
            setDateModified(DateTime.parse("2015-07-06").toDate());
            setType("item");
            setComment("This house was built in 1930.");
            setStatus(new Status("approved"));
            setPointPosition(new PointPosition(0, 271, 188));
            setTag(new Tag(new ItemTag("Bobs house")));
            setReports(new ArrayList<Report>(Arrays.asList(
                    new Report("e897ce0a-7881-4ceb-84c5-8dff75be606f", "Alice", "alice@example.com", "Spam"))));
        }});

        return imageTags;
    }

    @Test
    public void queryAsUser() {
        loginAsUser("9afd5142-23b0-11e5-b696-feff819cdc9f", Constants.USER_ROLE);

        QImageTag t = QImageTag.imageTag;
        BooleanExpression expression = t.urn.isNotNull().and(t.urn.eq("URN:NBN:no-nb_foto_NF.W_50121")).and(t.status.name.eq("approved"));
        PageRequest pageRequest = new PageRequest(0, 10, new Sort(Sort.Direction.DESC, "dateModified"));
        when(imageTagRepository.findAll(expression, pageRequest)).thenReturn(new PageImpl<ImageTag>(getImageTags()));

        ImageTagQuery query = new ImageTagQuery() {{
            setUrn("URN:NBN:no-nb_foto_NF.W_50121");
        }};

        Page<ImageTag> queriedImgtags = imageTagService.query(query, 0, 10, new String[]{"reports"});

        assertEquals(getImageTags().size(), queriedImgtags.getTotalElements());
        assertEquals("URN:NBN:no-nb_foto_NF.W_50121", queriedImgtags.getContent().get(0).getUrn());
        assertNull(queriedImgtags.getContent().get(0).getUserEmail());
        assertNull(queriedImgtags.getContent().get(0).getReports());
    }

    @Test
    public void queryAsAdmin() {
        loginAsUser("c6ef2cd7-4194-48ee-a225-0236f2b61ff9", Constants.ADMIN_ROLE);
        when(imageTagRepository.findAll(any(BooleanExpression.class), any(PageRequest.class))).thenReturn(new PageImpl<ImageTag>(getImageTags()));

        Page<ImageTag> queriedImgtags = imageTagService.query(
                new ImageTagQuery() {{
                    setUrn("URN:NBN:no-nb_foto_NF.W_50121");
                }},
                0, 10, new String[]{"reports"});

        assertEquals(getImageTags().size(), queriedImgtags.getTotalElements());
        assertEquals("URN:NBN:no-nb_foto_NF.W_50121", queriedImgtags.getContent().get(0).getUrn());
        assertNotNull(queriedImgtags.getContent().get(0).getUserEmail());
        assertNotNull(queriedImgtags.getContent().get(0).getReports().get(0));
    }

    @Test
    public void findOneAsUser() {
        loginAsUser("9afd5142-23b0-11e5-b696-feff819cdc9f", Constants.USER_ROLE);
        when(imageTagRepository.findOne(getImageTags().get(0).getTagId())).thenReturn(getImageTags().get(0));
        ImageTag imageTag = imageTagService.findOne(getImageTags().get(0).getTagId(), null);

        assertNull(imageTag.getUserEmail());
        assertNull(imageTag.getReports());
    }

    @Test
    public void findOneAsAdmin() {
        loginAsUser("c6ef2cd7-4194-48ee-a225-0236f2b61ff9", Constants.ADMIN_ROLE);
        when(imageTagRepository.findOne(getImageTags().get(0).getTagId())).thenReturn(getImageTags().get(0));
        ImageTag imageTag = imageTagService.findOne(getImageTags().get(0).getTagId(), null);

        assertNotNull(imageTag.getUserEmail());
        assertNotNull(imageTag.getReports());
    }

    @Test
    public void deleteAsUser() {
        loginAsUser("f6355c31-76a3-48b1-9905-1400fc27be77", Constants.USER_ROLE);
        when(imageTagRepository.findOne(getImageTags().get(0).getTagId())).thenReturn(getImageTags().get(0));
        imageTagService.delete(getImageTags().get(0).getTagId());
        verify(imageTagRepository, times(1)).save(any(ImageTag.class));
    }

    @Test(expected = NoSuchElementException.class)
    public void deleteNonExistingTagAsUser() {
        loginAsUser("f6355c31-76a3-48b1-9905-1400fc27be77", Constants.USER_ROLE);
        imageTagService.delete(getImageTags().get(0).getTagId());
    }

    @Test(expected = AccessDeniedException.class)
    public void deletAsNotOwner() {
        loginAsUser("9afd5142-23b0-11e5-b696-feff819cdc9f", Constants.USER_ROLE);
        when(imageTagRepository.findOne(getImageTags().get(0).getTagId())).thenReturn(getImageTags().get(0));
        imageTagService.delete(getImageTags().get(0).getTagId());
    }

    @Test
    public void deleteAsAdmin() {
        loginAsUser("c6ef2cd7-4194-48ee-a225-0236f2b61ff9", Constants.ADMIN_ROLE);
        when(imageTagRepository.findOne(getImageTags().get(0).getTagId())).thenReturn(getImageTags().get(0));
        imageTagService.delete(getImageTags().get(0).getTagId());
        verify(imageTagRepository, times(1)).save(any(ImageTag.class));
    }

    @Test
    public void saveAsUser() {
        loginAsUser("f6355c31-76a3-48b1-9905-1400fc27be77", Constants.USER_ROLE);
        when(imageTagRepository.save(any(ImageTag.class))).thenReturn(getImageTags().get(0));

        ImageTag imageTag = getImageTags().get(0);
        imageTag.setTagId(null);
        ImageTag savedImageTag = imageTagService.save(imageTag);

        verify(imageTagRepository, times(1)).save(any(ImageTag.class));
        assertNotNull(savedImageTag.getTagId());
    }

    @Test
    public void updateOwnTagAsUser() {
        loginAsUser("f6355c31-76a3-48b1-9905-1400fc27be77", Constants.USER_ROLE);
        when(imageTagRepository.findOne(eq(getImageTags().get(0).getTagId()))).thenReturn(getImageTags().get(0));
        ImageTag mockTag = getImageTags().get(0);
        mockTag.setPointPosition(new PointPosition(0, 10, 10));
        when(imageTagRepository.save(any(ImageTag.class))).thenReturn(mockTag);

        ImageTag inputTag = getImageTags().get(0);
        inputTag.setUrn("dummyurn");
        ImageTag updatedTag = imageTagService.update(inputTag);
        assertEquals(10, updatedTag.getPointPosition().getX());
        assertEquals(10, updatedTag.getPointPosition().getY());
        assertNotEquals("dummyurn", updatedTag.getUrn());
    }

    @Test
    public void updateTagAsAdmin() {
        loginAsUser("c6ef2cd7-4194-48ee-a225-0236f2b61ff9", Constants.ADMIN_ROLE);
        when(imageTagRepository.findOne(eq(getImageTags().get(0).getTagId()))).thenReturn(getImageTags().get(0));
        ImageTag mockTag = getImageTags().get(0);
        mockTag.setPointPosition(new PointPosition(0, 10, 10));
        mockTag.setUrn("dummyurn");
        when(imageTagRepository.save(any(ImageTag.class))).thenReturn(mockTag);

        ImageTag inputTag = getImageTags().get(0);
        inputTag.setUrn("dummyurn");
        ImageTag updatedTag = imageTagService.update(inputTag);
        assertEquals(10, updatedTag.getPointPosition().getX());
        assertEquals(10, updatedTag.getPointPosition().getY());
        assertEquals("dummyurn", updatedTag.getUrn());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateTagNullId() {
        ImageTag inputTag = getImageTags().get(0);
        inputTag.setTagId(null);
        ImageTag updatedTag = imageTagService.update(inputTag);
    }

    @Test(expected = AccessDeniedException.class)
    public void updateTagNoAccess() {
        loginAsUser("9afd5142-23b0-11e5-b696-feff819cdc9f", Constants.USER_ROLE);
        when(imageTagRepository.findOne(eq(getImageTags().get(0).getTagId()))).thenReturn(getImageTags().get(0));
        ImageTag updatedTag = imageTagService.update(getImageTags().get(0));
    }

    @Test
    public void reportAsUser() {
        ImageTag mockTag = getImageTags().get(0);
        mockTag.setReports(null);
        mockTag.setReported(false);
        loginAsUser("9afd5142-23b0-11e5-b696-feff819cdc9f", Constants.USER_ROLE);
        when(imageTagRepository.findOne(eq(mockTag.getTagId()))).thenReturn(mockTag);
        when(imageTagRepository.save(any(ImageTag.class))).then(returnsFirstArg());

        RequestInfo report = new RequestInfo("This tag is at the wrong place");
        ImageTag reportedTag = imageTagService.report(getImageTags().get(0).getTagId(), report);

        assertTrue(reportedTag.getReported());
        assertEquals(report.getComment(), reportedTag.getReports().get(0).getComment());
        assertEquals("9afd5142-23b0-11e5-b696-feff819cdc9f", reportedTag.getReports().get(0).getUserID());
    }

    @Test(expected = AccessDeniedException.class)
    public void reportAsUserTwice() {
        ImageTag mockTag = getImageTags().get(0);
        mockTag.setReports(null);
        mockTag.setReported(false);
        loginAsUser("9afd5142-23b0-11e5-b696-feff819cdc9f", Constants.USER_ROLE);
        when(imageTagRepository.findOne(eq(mockTag.getTagId()))).thenReturn(mockTag);
        when(imageTagRepository.save(any(ImageTag.class))).then(returnsFirstArg());

        RequestInfo report = new RequestInfo("This tag is at the wrong place");
        ImageTag reportedTag1 = imageTagService.report(getImageTags().get(0).getTagId(), report);
        ImageTag reportedTag2 = imageTagService.report(getImageTags().get(0).getTagId(), report);
    }


}