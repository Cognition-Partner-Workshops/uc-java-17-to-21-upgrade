package io.spring.application.profile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FavoriteTally implements ProfileActivity {
  private int given;
  private int received;
}
