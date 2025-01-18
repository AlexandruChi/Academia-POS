package pos.alexandruchi.academia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pos.alexandruchi.academia.exception.service.ServiceException;
import pos.alexandruchi.academia.model.Lecture;

@Service
public class CoursesService {
    private final IDMService idmService;
    private final RestTemplate restTemplate;
    private String authorization;

    private final String username;
    private final String password;

    @Value("${courses.host}")
    private String coursesHost;

    @Value("${courses.path}")
    private String coursesPath;

    public CoursesService(
            @Value("${academia.authorization.username}") String username,
            @Value("${academia.authorization.password}") String password,
            IDMService idmService
    ) {
        this.username = username;
        this.password = password;
        this.idmService = idmService;
        this.restTemplate = new RestTemplate();
        updateToken();
    }

    private void updateToken() {
        authorization = idmService.Authenticate(username, password);
        if (authorization == null) {
            throw new RuntimeException("Failed to authenticate with IDM service");
        }
    }

    public String getLecturePageCreate(Lecture lecture) {
        return coursesHost + coursesPath + "?code=" + lecture.getId();
    }

    public String getLecturePage(Lecture lecture) {
        int tries = 2;

        while(tries > 0) {
            tries--;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authorization);
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);

            String lecturePage = coursesHost + coursesPath + "/" + lecture.getId();

            try {
                @SuppressWarnings("HttpUrlsUsage") ResponseEntity<String> response = restTemplate.exchange(
                        "http://" + lecturePage,
                        HttpMethod.GET,
                        httpEntity,
                        String.class
                );

                if (response.getStatusCode() != HttpStatus.OK) {
                    break;
                }

                return lecturePage;

            } catch (HttpClientErrorException ex) {
                if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return null;
                }

                if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    updateToken();
                }
            }
        }

        throw new ServiceException();
    }
}
