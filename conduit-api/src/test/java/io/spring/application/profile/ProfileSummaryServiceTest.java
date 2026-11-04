package io.spring.application.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.ProfileSummaryReadService;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProfileSummaryServiceTest {
  private UserReadService userReadService;
  private ProfileSummaryReadService readService;
  private ProfileSummaryService service;

  @BeforeEach
  public void setUp() {
    userReadService = mock(UserReadService.class);
    readService = mock(ProfileSummaryReadService.class);
    service =
        new ProfileSummaryService(
            userReadService, readService, 2, 1, "https://conduit.example.com");
  }

  @AfterEach
  public void tearDown() {
    service.destroy();
  }

  @Test
  public void should_summarize_articles_followers_and_favorites() {
    when(userReadService.findByUsername(eq("author")))
        .thenReturn(new UserData("u1", "a@test.com", "author", "", ""));
    when(readService.authoredArticles(eq("u1")))
        .thenReturn(
            Arrays.asList(
                new AuthoredArticle("older", "Older", 1),
                new AuthoredArticle("newer", "Newer", 2)));
    when(readService.countFollowers(eq("u1"))).thenReturn(5);
    when(readService.countFavoritesGiven(eq("u1"))).thenReturn(7);

    Optional<ProfileSummaryData> result = service.summarize("author");

    assertTrue(result.isPresent());
    ProfileSummaryData summary = result.get();
    assertEquals("author", summary.getUsername());
    assertEquals(2, summary.getArticlesCount());
    assertEquals(5, summary.getFollowersCount());
    assertEquals(7, summary.getFavoritesGiven());
    assertEquals(3, summary.getFavoritesReceived());
    assertEquals("newer", summary.getLatestArticle().getSlug());
    assertEquals(
        Arrays.asList(
            "Wrote \"Older\" (1 favourites)",
            "Wrote \"Newer\" (2 favourites)",
            "Followed by 5 users",
            "Favourited 7 articles, received 3 favourites"),
        summary.getActivity());
    assertEquals("https://conduit.example.com/profiles/author", summary.getProfileUrl());
  }

  @Test
  public void should_have_no_latest_article_when_user_has_not_written() {
    when(userReadService.findByUsername(eq("reader")))
        .thenReturn(new UserData("u2", "r@test.com", "reader", "", ""));
    when(readService.authoredArticles(eq("u2"))).thenReturn(Collections.emptyList());

    ProfileSummaryData summary = service.summarize("reader").get();

    assertEquals(0, summary.getArticlesCount());
    assertNull(summary.getLatestArticle());
  }

  @Test
  public void should_return_empty_for_unknown_user() {
    assertFalse(service.summarize("nobody").isPresent());
  }

  @Test
  public void should_reject_bad_public_base_url() {
    ProfileSummaryService badUrlService =
        new ProfileSummaryService(userReadService, readService, 1, 0, "not-a-url");
    when(userReadService.findByUsername(eq("author")))
        .thenReturn(new UserData("u1", "a@test.com", "author", "", ""));
    when(readService.authoredArticles(eq("u1"))).thenReturn(Collections.emptyList());
    try {
      assertThrows(IllegalStateException.class, () -> badUrlService.summarize("author"));
    } finally {
      badUrlService.destroy();
    }
  }
}
