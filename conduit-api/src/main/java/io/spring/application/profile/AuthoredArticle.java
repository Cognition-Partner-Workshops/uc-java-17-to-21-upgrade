package io.spring.application.profile;

public record AuthoredArticle(String slug, String title, int favoritesCount)
    implements SummaryActivity {

  /** JavaBean-style accessor kept for existing callers. */
  public String getSlug() {
    return slug;
  }
}
