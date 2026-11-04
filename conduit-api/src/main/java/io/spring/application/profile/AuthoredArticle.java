package io.spring.application.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthoredArticle implements ProfileActivity {
  private String slug;
  private String title;
  private int favoritesCount;
}
