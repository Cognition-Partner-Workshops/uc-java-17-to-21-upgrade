package io.spring.api;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.profile.AuthoredArticle;
import io.spring.application.profile.ProfileSummaryData;
import io.spring.application.profile.ProfileSummaryService;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileSummaryApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ProfileSummaryApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ProfileSummaryService profileSummaryService;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_profile_summary_without_auth() {
    ProfileSummaryData summary =
        new ProfileSummaryData(
            "jane",
            1,
            2,
            3,
            4,
            new AuthoredArticle("hello", "Hello", 4),
            Arrays.asList("Wrote \"Hello\" (4 favourites)"),
            "https://conduit.example.com/profiles/jane");
    when(profileSummaryService.summarize(eq("jane"))).thenReturn(Optional.of(summary));

    RestAssuredMockMvc.when()
        .get("/profiles/{username}/summary", "jane")
        .then()
        .statusCode(200)
        .body("summary.username", equalTo("jane"))
        .body("summary.followersCount", equalTo(2))
        .body("summary.latestArticle.slug", equalTo("hello"));
  }

  @Test
  public void should_404_for_unknown_profile() {
    when(profileSummaryService.summarize(eq("ghost"))).thenReturn(Optional.empty());

    RestAssuredMockMvc.when().get("/profiles/{username}/summary", "ghost").then().statusCode(404);
  }
}
