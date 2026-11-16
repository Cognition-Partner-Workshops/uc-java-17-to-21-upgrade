package io.spring.application.profile;

import java.util.Locale;

public final class ActivityDescriber {
  private static final Locale UK = Locale.of("en", "GB");

  private ActivityDescriber() {}

  public static String describe(ProfileActivity activity) {
    return switch (activity) {
      case SummaryActivity summaryActivity -> describeSummaryActivity(summaryActivity);
      default ->
          throw new IllegalArgumentException(
              "Unknown activity type: " + activity.getClass().getName());
    };
  }

  private static String describeSummaryActivity(SummaryActivity activity) {
    return switch (activity) {
      case AuthoredArticle(String slug, String title, int favoritesCount) ->
          String.format(UK, "Wrote \"%s\" (%,d favourites)", title, favoritesCount);
      case FollowerTally(int followers) -> String.format(UK, "Followed by %,d users", followers);
      case FavoriteTally(int given, int received) ->
          String.format(UK, "Favourited %,d articles, received %,d favourites", given, received);
    };
  }
}
