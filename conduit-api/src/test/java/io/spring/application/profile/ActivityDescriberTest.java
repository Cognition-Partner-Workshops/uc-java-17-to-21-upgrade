package io.spring.application.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ActivityDescriberTest {

  @Test
  public void should_describe_authored_article_with_grouped_count() {
    assertEquals(
        "Wrote \"Hello\" (1,234 favourites)",
        ActivityDescriber.describe(new AuthoredArticle("hello", "Hello", 1234)));
  }

  @Test
  public void should_describe_follower_tally() {
    assertEquals("Followed by 2 users", ActivityDescriber.describe(new FollowerTally(2)));
  }

  @Test
  public void should_describe_favorite_tally() {
    assertEquals(
        "Favourited 3 articles, received 4 favourites",
        ActivityDescriber.describe(new FavoriteTally(3, 4)));
  }

  @Test
  public void should_reject_unknown_activity() {
    assertThrows(
        IllegalArgumentException.class, () -> ActivityDescriber.describe(new ProfileActivity() {}));
  }
}
