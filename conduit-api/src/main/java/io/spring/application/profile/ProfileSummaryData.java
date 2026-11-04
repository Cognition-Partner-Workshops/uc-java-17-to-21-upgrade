package io.spring.application.profile;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSummaryData {
  private String username;
  private int articlesCount;
  private int followersCount;
  private int favoritesGiven;
  private int favoritesReceived;
  private AuthoredArticle latestArticle;
  private List<String> activity;
  private String profileUrl;
}
