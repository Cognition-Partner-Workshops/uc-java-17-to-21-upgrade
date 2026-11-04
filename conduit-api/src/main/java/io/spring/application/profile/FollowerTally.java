package io.spring.application.profile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FollowerTally implements ProfileActivity {
  private int followers;
}
