package com.jmunoz.hoaxify;

import com.jmunoz.hoaxify.configuration.AppConfiguration;
import com.jmunoz.hoaxify.user.UserRepository;
import com.jmunoz.hoaxify.user.UserService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FileUploadControllerTest {

    private static final String API_1_0_HOAXES_UPLOAD = "/api/1.0/hoaxes/upload";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    AppConfiguration appConfiguration;

    @BeforeEach
    void setUp() throws IOException {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    private boolean authenticate(String username) {
        return testRestTemplate
                .getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }

    private HttpEntity<MultiValueMap<String, Object>> geRequestEntity() {
        ClassPathResource imageResource = new ClassPathResource("profile.png");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", imageResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        return requestEntity;
    }

    public <T> ResponseEntity<T> uploadFile(HttpEntity<?> requestEntity, Class<T> responseType) {
        return testRestTemplate.exchange(API_1_0_HOAXES_UPLOAD, HttpMethod.POST, requestEntity, responseType);
    }

    @Test
    void uploadFile_withImageFromAuthorizedUser_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        ResponseEntity<Object> response = uploadFile(geRequestEntity(), Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
